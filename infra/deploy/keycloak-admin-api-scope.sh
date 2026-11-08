#!/usr/bin/env bash
# NEXUS — una tantum, sul server già installato, SUBITO DOPO il deploy di Keycloak 26.8: da questa versione l'API di
# amministrazione accetta solo token con lo scope kc-admin-api (guida di aggiornamento di Keycloak 26.8). Keycloak lo dà
# da solo ai suoi client (admin-cli, security-admin-console), non al client tecnico di NEXUS: finché manca, creazione
# degli utenti, cambio email, disattivazione e registro degli accessi rispondono «non disponibile». Idempotente.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<ref>/infra/deploy/keycloak-admin-api-scope.sh
#   sudo bash keycloak-admin-api-scope.sh
# Nessun segreto: usa la password della console già presente nel container Keycloak.
set -euo pipefail

readonly BASE="/opt/nexus"
readonly ENV_FILE="$BASE/.env"
readonly CLIENT="nexus-backend-admin"
readonly SCOPE="kc-admin-api"
readonly KCADM="/opt/keycloak/bin/kcadm.sh"
readonly KCCONFIG="/tmp/kcadm-nexus.config"

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

step "Scope $SCOPE del realm nexus"
scope_id="$(kc "$KCADM get client-scopes -r nexus --config $KCCONFIG --fields id,name --format csv --noquotes" \
  | awk -F, -v s="$SCOPE" '$2 == s { print $1 }')"
[[ -n $scope_id ]] || fail "lo scope $SCOPE non c'è: Keycloak è almeno alla 26.8? Fai prima il deploy"
echo "trovato"

step "Scope predefinito del client $CLIENT"
client_id="$(kc "$KCADM get clients -r nexus --config $KCCONFIG -q clientId=$CLIENT --fields id --format csv --noquotes")"
[[ -n $client_id ]] || fail "il client $CLIENT non c'è (keycloak-backend-client.sh)"
kc "$KCADM update clients/$client_id/default-client-scopes/$scope_id -r nexus --config $KCCONFIG"
echo "assegnato: i token del backend valgono di nuovo per l'API di amministrazione"

echo
echo "Fatto. Prova: come admin, Utenti → Accessi di un utente (deve mostrare il registro, non «non disponibile»)."
