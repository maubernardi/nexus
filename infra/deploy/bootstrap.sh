#!/usr/bin/env bash
# NEXUS — preparazione una tantum del server (Rocky Linux 9). Idempotente: si può rieseguire.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<ref>/infra/deploy/bootstrap.sh
#   sudo bash bootstrap.sh "ssh-ed25519 AAAA... nexus-deploy"      # chiave PUBBLICA di deploy
# Installa Docker CE (versioni esatte), crea l'utente `deploy` (solo forced command), /opt/nexus con i segreti
# generati in .env (mai in git) e apre le porte web su firewalld.
set -euo pipefail

readonly REF="${NEXUS_REF:-main}"
readonly REPO="maubernardi/nexus"
readonly NEXUS_DOMAIN="${NEXUS_DOMAIN:-portalenexus.it}"
readonly BASE="/opt/nexus"
readonly DOCKER_VERSION="29.8.1-1.el9"
readonly COMPOSE_VERSION="5.5.1-1.el9"
readonly DEPLOY_PUBKEY="${1:-}"

step() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { echo "ERRORE: $*" >&2; exit 1; }

[[ $EUID -eq 0 ]] || fail "eseguire con sudo"
grep -q 'ID="rocky"' /etc/os-release && grep -q 'VERSION_ID="9' /etc/os-release || fail "previsto Rocky Linux 9"
[[ "$DEPLOY_PUBKEY" =~ ^ssh-ed25519\ [A-Za-z0-9+/=]+(\ .*)?$ ]] || fail "passare la chiave PUBBLICA ed25519 di deploy come argomento"

step "Docker CE $DOCKER_VERSION e Compose $COMPOSE_VERSION"
if ! rpm -q "docker-ce-$DOCKER_VERSION" >/dev/null 2>&1; then
  dnf -y remove podman buildah runc >/dev/null 2>&1 || true
  dnf -y install dnf-plugins-core
  dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
  dnf -y install "docker-ce-$DOCKER_VERSION" "docker-ce-cli-$DOCKER_VERSION" containerd.io \
    "docker-compose-plugin-$COMPOSE_VERSION"
fi
mkdir -p /etc/docker
cat >/etc/docker/daemon.json <<'JSON'
{
  "log-driver": "json-file",
  "log-opts": { "max-size": "10m", "max-file": "3" },
  "live-restore": true
}
JSON
systemctl enable --now docker
systemctl restart docker
docker --version && docker compose version

step "Utente deploy (solo forced command)"
id deploy >/dev/null 2>&1 || useradd --create-home --shell /bin/bash deploy
passwd -l deploy >/dev/null
usermod -aG docker deploy
install -d -m 700 -o deploy -g deploy /home/deploy/.ssh
printf 'restrict,command="/usr/local/bin/nexus-deploy" %s\n' "$DEPLOY_PUBKEY" >/home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys && chmod 600 /home/deploy/.ssh/authorized_keys
restorecon -R /home/deploy/.ssh
curl -fsSL "https://raw.githubusercontent.com/$REPO/$REF/infra/deploy/nexus-deploy" -o /usr/local/bin/nexus-deploy
chown root:root /usr/local/bin/nexus-deploy && chmod 755 /usr/local/bin/nexus-deploy
# SSH: se l'accesso è limitato con AllowUsers (hardening), aggiunge deploy
hardening=/etc/ssh/sshd_config.d/00-hardening.conf
if [[ -f $hardening ]] && grep -qE '^AllowUsers' $hardening && ! grep -qE '^AllowUsers.*\bdeploy\b' $hardening; then
  sed -i -E 's/^(AllowUsers.*)$/\1 deploy/' $hardening
fi
sshd -t && systemctl reload sshd

step "Directory $BASE e segreti"
install -d -m 750 -o deploy -g deploy "$BASE" "$BASE/releases"
gen() { openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c "${1:-32}"; }
if [[ ! -f $BASE/.env ]]; then
  demo_tutor1="Demo-$(gen 14)"; demo_tutor2="Demo-$(gen 14)"; demo_op="Demo-$(gen 14)"; demo_admin="Demo-$(gen 14)"
  kc_admin="$(gen 24)"
  umask 077
  cat >"$BASE/.env" <<ENV
# Segreti NEXUS generati da bootstrap.sh il $(date -u +%Y-%m-%dT%H:%MZ). Non copiare in git.
NEXUS_DOMAIN=$NEXUS_DOMAIN
NEXUS_AUTH_DOMAIN=auth.$NEXUS_DOMAIN
POSTGRES_SUPERUSER_PASSWORD=$(gen)
NEXUS_OWNER_PASSWORD=$(gen)
NEXUS_APP_PASSWORD=$(gen)
KEYCLOAK_DB_PASSWORD=$(gen)
KEYCLOAK_ADMIN_PASSWORD=$kc_admin
NEXUS_DEMO_PASSWORD_TUTOR1=$demo_tutor1
NEXUS_DEMO_PASSWORD_TUTOR2=$demo_tutor2
NEXUS_DEMO_PASSWORD_OPERATORE=$demo_op
NEXUS_DEMO_PASSWORD_ADMIN=$demo_admin
ENV
  chown deploy:deploy "$BASE/.env" && chmod 600 "$BASE/.env"
  cat <<INFO

  Credenziali generate (mostrate SOLO ora; restano in $BASE/.env, leggibile con sudo):
    App demo  https://$NEXUS_DOMAIN
      tutor1        $demo_tutor1
      tutor2        $demo_tutor2
      operatore.cc  $demo_op
      admin         $demo_admin
    Console Keycloak (tunnel: ssh -L 8081:127.0.0.1:8081 <server>, poi http://localhost:8081/admin)
      admin         $kc_admin
  Salvale nel tuo gestore di password.
INFO
else
  echo "$BASE/.env già presente: segreti invariati"
fi

step "Firewall: HTTP, HTTPS, HTTP/3"
firewall-cmd --permanent --add-service=http --add-service=https --add-port=443/udp >/dev/null
firewall-cmd --reload >/dev/null
firewall-cmd --list-services

step "Fatto"
echo "Il server è pronto: il primo deploy si avvia da GitHub (Actions -> Deploy demo -> Run workflow)."
