#!/usr/bin/env bash
# NEXUS — una tantum, sul server già installato, DOPO il deploy di EN-7: aggiunge alla policy delle password del realm
# "nexus" il rifiuto delle password comuni (elenco nell'immagine nexus-keycloak). Le password già impostate restano;
# vale dal prossimo cambio. Idempotente: si può rieseguire.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<ref>/infra/deploy/keycloak-password-policy.sh
#   sudo bash keycloak-password-policy.sh
# Nessun segreto: usa la password della console già presente nel container Keycloak.
set -euo pipefail

readonly BASE="/opt/nexus"
readonly ENV_FILE="$BASE/.env"
readonly KCADM="/opt/keycloak/bin/kcadm.sh"
readonly KCCONFIG="/tmp/kcadm-nexus.config"
readonly LIST="nexus-common-passwords.txt"
readonly POLICY="length(12) and notUsername and notEmail and passwordBlacklist($LIST)"

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

step "Elenco delle password comuni nell'immagine"
kc "test -s /opt/keycloak/data/password-blacklists/$LIST" \
  || fail "elenco non trovato: fai prima il deploy della versione con EN-7"
echo "presente"

step "Accesso alla API di amministrazione di Keycloak"
kc "$KCADM config credentials --config $KCCONFIG --server http://localhost:8080 --realm master --user admin \
  --password \"\$KC_BOOTSTRAP_ADMIN_PASSWORD\" >/dev/null" \
  || fail "accesso come admin non riuscito: la password della console è ancora quella di KEYCLOAK_ADMIN_PASSWORD?"

step "Policy delle password del realm nexus"
kc "$KCADM update realms/nexus --config $KCCONFIG -s 'passwordPolicy=$POLICY'"
echo "attiva: $POLICY"

echo
echo "Fatto: da ora una password tra le più comuni (es. password1234) viene rifiutata con «Password troppo comune»."
