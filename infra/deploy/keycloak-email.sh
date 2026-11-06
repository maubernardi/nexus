#!/usr/bin/env bash
# NEXUS — una tantum, sui server installati prima di US-103: imposta nel realm "nexus" di Keycloak il server di posta
# (SMTP IONOS, lo stesso delle notifiche), con cui Keycloak invia ai nuovi utenti l'email per impostare la password.
# Idempotente: si può rieseguire, per esempio dopo aver cambiato NEXUS_SMTP_PASSWORD nel .env.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<ref>/infra/deploy/keycloak-email.sh
#   sudo bash keycloak-email.sh
# La password non viene mai mostrata: la legge da NEXUS_SMTP_PASSWORD in /opt/nexus/.env.
set -euo pipefail

readonly BASE="/opt/nexus"
readonly ENV_FILE="$BASE/.env"
readonly KCADM="/opt/keycloak/bin/kcadm.sh"
readonly KCCONFIG="/tmp/kcadm-nexus.config"

step() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { echo "ERRORE: $*" >&2; exit 1; }

[[ $EUID -eq 0 ]] || fail "eseguire con sudo"
[[ -f $ENV_FILE ]] || fail "$ENV_FILE non trovato"
release="$(readlink -f "$BASE/current" 2>/dev/null || true)"
[[ -n $release && -f $release/compose.yaml ]] || fail "nessuna release in esecuzione in $BASE/current"

env_value() { grep -E "^$1=" "$ENV_FILE" | tail -n1 | cut -d= -f2-; }
compose() {
  docker compose --project-directory "$release" -f "$release/compose.yaml" --env-file "$ENV_FILE" \
    --env-file "$release/release.env" "$@"
}
# comando nel container Keycloak: la password admin è già lì (KC_BOOTSTRAP_ADMIN_PASSWORD), quella SMTP arriva da
# stdin; nessuna delle due compare tra gli argomenti dei comandi sul server
kc() { printf '%s\n' "$SMTP_PASSWORD" | compose exec -T keycloak bash -c "read -r SMTP_PASSWORD; $1"; }

step "Password SMTP dal .env"
SMTP_PASSWORD="$(env_value NEXUS_SMTP_PASSWORD)"
[[ -n $SMTP_PASSWORD ]] || fail "NEXUS_SMTP_PASSWORD mancante in $ENV_FILE: aggiungila prima"
echo "presente"

step "Accesso alla API di amministrazione di Keycloak"
kc "$KCADM config credentials --config $KCCONFIG --server http://localhost:8080 --realm master --user admin \
  --password \"\$KC_BOOTSTRAP_ADMIN_PASSWORD\" >/dev/null" \
  || fail "accesso come admin non riuscito: la password della console è ancora quella di KEYCLOAK_ADMIN_PASSWORD?"

step "Server di posta del realm nexus"
kc "$KCADM update realms/nexus --config $KCCONFIG \
  -s smtpServer.host=smtp.ionos.it -s smtpServer.port=587 \
  -s smtpServer.from=noreply@portalenexus.it -s smtpServer.fromDisplayName=NEXUS \
  -s smtpServer.starttls=true -s smtpServer.ssl=false -s smtpServer.auth=true \
  -s smtpServer.user=admin@portalenexus.it -s \"smtpServer.password=\$SMTP_PASSWORD\""
kc "rm -f $KCCONFIG"
echo "impostato: smtp.ionos.it:587 (STARTTLS), mittente NEXUS <noreply@portalenexus.it>"

echo
echo "Fatto: Keycloak invia le email per impostare la password. Prova: crea un utente con un tuo indirizzo da NEXUS (Utenti)."
