#!/usr/bin/env bash
# NEXUS — una tantum, sui server installati prima di US-113: attiva nel realm "nexus" di Keycloak il registro degli
# accessi (riusciti e falliti, con data, ora e indirizzo IP), conservato 12 mesi e poi cancellato da Keycloak, e dà al
# client tecnico nexus-backend-admin il permesso di leggerlo (view-events). Idempotente: si può rieseguire.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<ref>/infra/deploy/keycloak-access-log.sh
#   sudo bash keycloak-access-log.sh
# Nessun segreto: usa la password della console già presente nel container Keycloak.
set -euo pipefail

readonly BASE="/opt/nexus"
readonly ENV_FILE="$BASE/.env"
readonly KCADM="/opt/keycloak/bin/kcadm.sh"
readonly KCCONFIG="/tmp/kcadm-nexus.config"
readonly RETENTION_SECONDS=31536000 # 12 mesi (decisione del PO, 06/11)

step() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { echo "ERRORE: $*" >&2; exit 1; }

[[ $EUID -eq 0 ]] || fail "eseguire con sudo"
[[ -f $ENV_FILE ]] || fail "$ENV_FILE non trovato"
release="$(readlink -f "$BASE/current" 2>/dev/null || true)"
[[ -n $release && -f $release/compose.yaml ]] || fail "nessuna release in esecuzione in $BASE/current"

compose() {
  docker compose --project-directory "$release" -f "$release/compose.yaml" --env-file "$ENV_FILE" \
    --env-file "$release/release.env" "$@"
}
kc() { compose exec -T keycloak bash -c "$1"; }
trap 'kc "rm -f $KCCONFIG" >/dev/null 2>&1 || true' EXIT

step "Accesso alla API di amministrazione di Keycloak"
kc "$KCADM config credentials --config $KCCONFIG --server http://localhost:8080 --realm master --user admin \
  --password \"\$KC_BOOTSTRAP_ADMIN_PASSWORD\" >/dev/null" \
  || fail "accesso come admin non riuscito: la password della console è ancora quella di KEYCLOAK_ADMIN_PASSWORD?"

step "Registro degli accessi del realm nexus"
kc "$KCADM update realms/nexus --config $KCCONFIG -s eventsEnabled=true -s eventsExpiration=$RETENTION_SECONDS \
  -s 'enabledEventTypes=[\"LOGIN\",\"LOGIN_ERROR\"]'"
echo "attivo: accessi riusciti e falliti, conservati $((RETENTION_SECONDS / 86400)) giorni"

step "Permesso di lettura per il client tecnico di NEXUS"
kc "$KCADM add-roles --config $KCCONFIG -r nexus --uusername service-account-nexus-backend-admin \
  --cclientid realm-management --rolename view-events" \
  || fail "permesso non assegnato: il client nexus-backend-admin c'è? (keycloak-backend-client.sh)"
echo "assegnato: view-events a nexus-backend-admin"

echo
echo "Fatto: gli accessi da ora in poi compaiono in NEXUS (Utenti → Accessi, Il mio profilo). Quelli precedenti a"
echo "questo momento non sono stati registrati."
