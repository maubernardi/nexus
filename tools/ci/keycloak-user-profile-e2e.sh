#!/usr/bin/env bash
# US-116: prova dello script una tantum infra/deploy/keycloak-user-profile.sh, su un server finto con il realm di
# produzione di prima di US-116 (senza «Edit username», profilo utente predefinito). Prova:
#   - prima dello script un utente creato dalla API (come fa NEXUS) cambia i propri dati dalla pagina dell'account;
#   - lo script, eseguito due volte, avvisa degli utenti senza email, nome o cognome e configura il realm;
#   - dopo, dalla pagina dell'account i cambi sono rifiutati, e l'amministrazione corregge lo username.
# Gira nella CI (job «Tema di Keycloak», dopo la build dell'immagine) e in locale: richiede solo Docker e python3.
#   tools/ci/keycloak-user-profile-e2e.sh [immagine di Keycloak con il tema, predefinita nexus-keycloak:ci]
set -euo pipefail
cd "$(dirname "$0")/../.."

readonly IMAGE="${1:-nexus-keycloak:ci}"
readonly PROJECT=nexus-e2e-kcprofilo
readonly PORT=18180 # mai la 8081 (tunnel del PO) né le porte del collaudo
readonly URL="http://localhost:$PORT"
readonly ADMIN_PASSWORD="prova-e2e-admin"
readonly USER_PASSWORD="prova-e2e-utente-2026"
readonly SCRIPT="$PWD/infra/deploy/keycloak-user-profile.sh"

step() { printf '\n==> %s\n' "$*"; }
ok() { echo "OK: $*"; }
ko() { echo "::error::Prova di keycloak-user-profile.sh: $*"; exit 1; }

tmp=$(mktemp -d)
export NEXUS_BASE="$tmp/nexus"
release="$NEXUS_BASE/releases/$PROJECT"
compose() {
  docker compose --project-directory "$release" -f "$release/compose.yaml" --env-file "$NEXUS_BASE/.env" \
    --env-file "$release/release.env" "$@"
}
cleanup() {
  local ids
  mapfile -t ids < <(docker ps -aq --filter "label=com.docker.compose.project=$PROJECT")
  ((${#ids[@]} == 0)) || docker rm -f -v "${ids[@]}" >/dev/null 2>&1 || true
  docker network rm "${PROJECT}_default" >/dev/null 2>&1 || true
  rm -rf -- "$tmp"
}
trap cleanup EXIT

step "Server finto: release in $NEXUS_BASE/current, realm di produzione di prima di US-116"
mkdir -p "$release"
ln -s "$release" "$NEXUS_BASE/current"
printf 'KEYCLOAK_ADMIN_PASSWORD=%s\n' "$ADMIN_PASSWORD" >"$NEXUS_BASE/.env"
echo "NEXUS_VERSION=e2e" >"$release/release.env"
python3 -I - infra/deploy/keycloak/nexus-realm.json "$release/nexus-realm.json" <<'EOF'
import json, sys
realm = json.load(open(sys.argv[1], encoding="utf-8"))
realm.pop("editUsernameAllowed", None)
realm.pop("components", None)
json.dump(realm, open(sys.argv[2], "w", encoding="utf-8"), ensure_ascii=False)
EOF
cat >"$release/compose.yaml" <<EOF
services:
  keycloak:
    image: $IMAGE
    command: ["start-dev", "--import-realm"]
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: \${KEYCLOAK_ADMIN_PASSWORD}
      KC_HEALTH_ENABLED: "true"
      NEXUS_DOMAIN: localhost
      NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET: prova
      NEXUS_SMTP_PASSWORD: ""
    ports:
      - "127.0.0.1:$PORT:8080"
    volumes:
      - ./nexus-realm.json:/opt/keycloak/data/import/nexus-realm.json:ro
    healthcheck:
      test: ["CMD-SHELL", "exec 3<>/dev/tcp/127.0.0.1/9000 && printf 'GET /health/ready HTTP/1.1\\\\r\\\\nHost: x\\\\r\\\\nConnection: close\\\\r\\\\n\\\\r\\\\n' >&3 && grep -q UP <&3"]
      interval: 5s
      retries: 60
EOF
compose up -d --wait --wait-timeout 240 keycloak >/dev/null || ko "Keycloak di prova non parte"
ok "Keycloak di prova avviato"

kcadm() { compose exec -T keycloak /opt/keycloak/bin/kcadm.sh "$@" --config /tmp/kc-e2e.config; }
kcadm config credentials --server http://localhost:8080 --realm master --user admin --password "$ADMIN_PASSWORD" \
  >/dev/null
# un utente come quelli creati da NEXUS (ruoli predefiniti del realm, quindi la pagina dell'account) e uno senza nome
kcadm create users -r nexus -s username=utente.prova -s email=utente.prova@nexus.local -s firstName=Utente \
  -s lastName=Prova -s emailVerified=true -s enabled=true >/dev/null
kcadm set-password -r nexus --username utente.prova --new-password "$USER_PASSWORD"
kcadm create users -r nexus -s username=senza.nome -s email=senza.nome@nexus.local -s enabled=true >/dev/null

step "Prima dello script: l'utente cambia i propri dati dalla pagina dell'account"
python3 -I tools/ci/keycloak-account-e2e.py "$URL" utente.prova "$USER_PASSWORD" accettato
# la prova gli ha cambiato nome ed email: si rimettono quelli di partenza
id=$(kcadm get users -r nexus -q username=utente.prova -q exact=true --fields id --format csv --noquotes | tr -d '\r')
[[ -n $id ]] || ko "utente di prova non trovato dopo il cambio"
kcadm update "users/$id" -r nexus -s email=utente.prova@nexus.local -s firstName=Utente

step "Script, due volte"
for run in 1 2; do
  out=$(bash "$SCRIPT" 2>&1) || { echo "$out"; ko "esecuzione $run non riuscita"; }
  grep -q "2 utenti, 1 senza email, nome o cognome" <<<"$out" || { echo "$out"; ko "conteggio degli utenti senza dati"; }
  grep -q "lastName: modificabile solo dall'amministrazione" <<<"$out" || { echo "$out"; ko "controllo finale"; }
  ok "esecuzione $run"
done
echo "$out" | tail -n 12

step "Dopo lo script"
python3 -I tools/ci/keycloak-account-e2e.py "$URL" utente.prova "$USER_PASSWORD" rifiutato
kcadm update "users/$id" -r nexus -s username=utente.corretto
[[ $(kcadm get "users/$id" -r nexus --fields username --format csv --noquotes | tr -d '\r') == utente.corretto ]] \
  || ko "l'amministrazione non corregge lo username"
ok "l'amministrazione corregge lo username"
echo
echo "Prova di keycloak-user-profile.sh riuscita."
