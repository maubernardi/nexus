#!/usr/bin/env bash
# NEXUS — una tantum, sui server installati prima di US-1308: attiva nel realm "nexus" di Keycloak il recupero della
# password («Password dimenticata?» nella pagina di accesso). Il collegamento arriva per email con l'SMTP già impostato
# da keycloak-email.sh ed è valido 30 minuti. Idempotente: si può rieseguire.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<ref>/infra/deploy/keycloak-password-reset.sh
#   sudo bash keycloak-password-reset.sh
# Nessun segreto: usa la password della console già presente nel container Keycloak.
set -euo pipefail

readonly BASE="/opt/nexus"
readonly ENV_FILE="$BASE/.env"
readonly KCADM="/opt/keycloak/bin/kcadm.sh"
readonly KCCONFIG="/tmp/kcadm-nexus.config"
readonly LINK_VALIDITY_SECONDS=1800

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

step "Accesso alla API di amministrazione di Keycloak"
kc "$KCADM config credentials --config $KCCONFIG --server http://localhost:8080 --realm master --user admin \
  --password \"\$KC_BOOTSTRAP_ADMIN_PASSWORD\" >/dev/null" \
  || fail "accesso come admin non riuscito: la password della console è ancora quella di KEYCLOAK_ADMIN_PASSWORD?"

step "Server di posta del realm nexus"
host="$(kc "$KCADM get realms/nexus --config $KCCONFIG --fields smtpServer" | grep -o '"host" *: *"[^"]*"' || true)"
[[ -n $host ]] || { kc "rm -f $KCCONFIG"; fail "posta non impostata: esegui prima keycloak-email.sh"; }
echo "impostato ($host)"

step "Recupero della password"
kc "$KCADM update realms/nexus --config $KCCONFIG \
  -s resetPasswordAllowed=true -s actionTokenGeneratedByUserLifespan=$LINK_VALIDITY_SECONDS"
kc "rm -f $KCCONFIG"
echo "attivo: collegamento valido $((LINK_VALIDITY_SECONDS / 60)) minuti"

echo
echo "Fatto: nella pagina di accesso c'è «Password dimenticata?». Prova: chiedi il collegamento per un tuo utente."
