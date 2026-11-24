#!/usr/bin/env bash
# NEXUS — una tantum (US-1306, controllo ASVS sulla produzione del 24/11): l'accesso degli amministratori di Keycloak
# (realm master) passa solo dal tunnel SSH. Imposta l'indirizzo del realm master su http://localhost:8081, lo stesso
# della console (KC_HOSTNAME_ADMIN in compose.yaml): così la console, aperta dal tunnel, fa accedere l'amministratore
# senza passare dal dominio pubblico, dove Caddy blocca /realms/master. Senza questo la console dal tunnel si ferma con
# «Timeout when waiting for 3rd party check iframe message».
# Sul server già installato: PRIMA del deploy che blocca /realms/master; su un server nuovo: dopo il primo deploy.
# Idempotente: si può rieseguire.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<ref>/infra/deploy/keycloak-admin-tunnel.sh
#   sudo bash keycloak-admin-tunnel.sh
# Nessun segreto: usa la password della console già presente nel container Keycloak. Il realm nexus non cambia.
set -euo pipefail

readonly BASE="/opt/nexus"
readonly ENV_FILE="$BASE/.env"
readonly KCADM="/opt/keycloak/bin/kcadm.sh"
readonly KCCONFIG="/tmp/kcadm-nexus.config"
readonly ADMIN_URL="http://localhost:8081" # KC_HOSTNAME_ADMIN e porta del tunnel (127.0.0.1:8081 sul server)

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

step "Indirizzo del realm master: $ADMIN_URL"
kc "$KCADM update realms/master --config $KCCONFIG -s 'attributes.frontendUrl=$ADMIN_URL'"

# dopo il cambio il token di kcadm non vale più (l'emittente è cambiato): si controlla dall'indirizzo del tunnel
issuer="$(curl -fsS "$ADMIN_URL/realms/master/.well-known/openid-configuration" | grep -o '"issuer":"[^"]*"' || true)"
[[ $issuer == "\"issuer\":\"$ADMIN_URL/realms/master\"" ]] || fail "indirizzo non impostato (letto: ${issuer:-niente})"
echo "impostato: il realm master risponde come $ADMIN_URL"

echo
echo "Fatto: la console si apre solo dal tunnel (ssh -L 8081:127.0.0.1:8081 <server>, poi $ADMIN_URL/admin)."
echo "L'accesso a NEXUS (realm nexus) non cambia."
