#!/usr/bin/env bash
# NEXUS — una tantum, sui server installati prima di US-1308: attiva nel realm "nexus" di Keycloak il recupero della
# password («Password dimenticata?» nella pagina di accesso), l'email di sicurezza quando la password cambia e il tema
# delle email di NEXUS (firma «Portale Nexus»). Il collegamento arriva per email con l'SMTP impostato da
# keycloak-email.sh (da eseguire prima: è quello che fa arrivare anche gli inviti) ed è valido 30 minuti.
# Idempotente: si può rieseguire.
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

step "Recupero della password"
kc "$KCADM update realms/nexus --config $KCCONFIG \
  -s resetPasswordAllowed=true -s actionTokenGeneratedByUserLifespan=$LINK_VALIDITY_SECONDS"
echo "attivo: collegamento valido $((LINK_VALIDITY_SECONDS / 60)) minuti"

step "Email di sicurezza al cambio della password"
kc "$KCADM update realms/nexus --config $KCCONFIG -s 'eventsListeners=[\"jboss-logging\",\"email\"]'"
echo "attiva (solo al cambio della password: l'immagine di Keycloak filtra gli altri eventi)"

step "Tema delle email (testi di NEXUS e firma «Portale Nexus»)"
kc "$KCADM update realms/nexus --config $KCCONFIG -s emailTheme=nexus"
kc "rm -f $KCCONFIG"
echo "attivo"

echo
echo "Fatto: nella pagina di accesso c'è «Password dimenticata?» e a ogni cambio di password parte un'email di"
echo "sicurezza. Prova: chiedi il collegamento per un tuo utente e imposta una nuova password."
