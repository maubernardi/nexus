#!/usr/bin/env bash
# NEXUS — una tantum, sui server installati prima di US-110 (cambio email): crea in Keycloak il client tecnico
# nexus-backend-admin, con cui il backend aggiorna l'email degli utenti, e il suo segreto nel .env. Idempotente.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<ref>/infra/deploy/keycloak-backend-client.sh
#   sudo bash keycloak-backend-client.sh
# Prima serve il deploy di una versione con US-110. Il segreto non viene mai mostrato: resta solo in /opt/nexus/.env.
set -euo pipefail

readonly BASE="/opt/nexus"
readonly ENV_FILE="$BASE/.env"
readonly CLIENT="nexus-backend-admin"
readonly KCADM="/opt/keycloak/bin/kcadm.sh"
readonly KCCONFIG="/tmp/kcadm-nexus.config"

step() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { echo "ERRORE: $*" >&2; exit 1; }

[[ $EUID -eq 0 ]] || fail "eseguire con sudo"
[[ -f $ENV_FILE ]] || fail "$ENV_FILE non trovato"
release="$(readlink -f "$BASE/current" 2>/dev/null || true)"
[[ -n $release && -f $release/compose.yaml ]] || fail "nessuna release in esecuzione in $BASE/current"
grep -q 'KEYCLOAK_ADMIN_CLIENT_SECRET' "$release/compose.yaml" \
  || fail "la release in esecuzione è precedente a US-110: fai prima il deploy, poi riesegui lo script"

env_value() { grep -E "^$1=" "$ENV_FILE" | tail -n1 | cut -d= -f2-; }
compose() {
  docker compose --project-directory "$release" -f "$release/compose.yaml" --env-file "$ENV_FILE" \
    --env-file "$release/release.env" "$@"
}
# comando nel container Keycloak: la password admin è già lì (KC_BOOTSTRAP_ADMIN_PASSWORD), il segreto arriva da stdin;
# nessuno dei due compare tra gli argomenti dei comandi sul server
kc() { printf '%s\n' "$KC_SECRET" | compose exec -T keycloak bash -c "read -r KC_SECRET; $1"; }

step "Segreto del client nel .env"
if [[ -z "$(env_value NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET)" ]]; then
  secret="$(openssl rand -base64 64 | tr -dc 'A-Za-z0-9' | head -c 40)"
  sed -i '/^NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET=/d' "$ENV_FILE"
  printf 'NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET=%s\n' "$secret" >>"$ENV_FILE"
  echo "generato e aggiunto a $ENV_FILE"
else
  echo "già presente in $ENV_FILE"
fi
KC_SECRET="$(env_value NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET)"

step "Accesso alla API di amministrazione di Keycloak"
kc "$KCADM config credentials --config $KCCONFIG --server http://localhost:8080 --realm master --user admin \
  --password \"\$KC_BOOTSTRAP_ADMIN_PASSWORD\" >/dev/null" \
  || fail "accesso come admin non riuscito: la password della console è ancora quella di KEYCLOAK_ADMIN_PASSWORD?"

step "Client $CLIENT"
id="$(kc "$KCADM get clients --config $KCCONFIG -r nexus -q clientId=$CLIENT --fields id --format csv --noquotes" \
  | tr -d '\r')"
if [[ -z $id ]]; then
  kc "$KCADM create clients --config $KCCONFIG -r nexus -s clientId=$CLIENT \
    -s 'name=NEXUS backend (amministrazione utenti)' -s enabled=true -s publicClient=false \
    -s clientAuthenticatorType=client-secret -s serviceAccountsEnabled=true -s standardFlowEnabled=false \
    -s directAccessGrantsEnabled=false -s implicitFlowEnabled=false -s \"secret=\$KC_SECRET\"" >/dev/null
  echo "creato"
else
  kc "$KCADM update clients/$id --config $KCCONFIG -r nexus -s \"secret=\$KC_SECRET\"" >/dev/null
  echo "già presente: segreto riallineato al .env"
fi

step "Permessi del client: solo view-users e manage-users"
kc "$KCADM add-roles --config $KCCONFIG -r nexus --uusername service-account-$CLIENT \
  --cclientid realm-management --rolename view-users --rolename manage-users"
kc "rm -f $KCCONFIG"

step "Riavvio del backend con il nuovo segreto"
compose up -d --wait --wait-timeout 180 backend
echo
echo "Fatto: dal profilo di NEXUS ogni utente può cambiare il proprio indirizzo email."
