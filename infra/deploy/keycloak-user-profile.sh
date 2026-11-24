#!/usr/bin/env bash
# NEXUS — una tantum, sul server già installato (US-116): nel realm "nexus"
#   - lo username si può correggere, ma solo dall'amministrazione (la pagina «Dati» dell'ADMIN in NEXUS);
#   - username, email, nome e cognome li modifica solo l'amministrazione: dalla pagina «il mio account» di Keycloak
#     l'utente li vede soltanto. Prima un utente poteva cambiarli lì, senza passare da NEXUS.
# Le nuove installazioni nascono già così (infra/deploy/keycloak/nexus-realm.json). Idempotente: si può rieseguire.
# Vale con qualunque versione di NEXUS: si può lanciare prima o dopo il deploy.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<ref>/infra/deploy/keycloak-user-profile.sh
#   sudo bash keycloak-user-profile.sh
# Nessun segreto: usa la password della console già presente nel container Keycloak. Mostra solo conteggi, nessun dato
# personale.
set -euo pipefail

# NEXUS_BASE solo per la prova della CI (tools/ci/keycloak-user-profile-e2e.sh)
readonly BASE="${NEXUS_BASE:-/opt/nexus}"
readonly ENV_FILE="$BASE/.env"
readonly KCADM="/opt/keycloak/bin/kcadm.sh"
readonly KCCONFIG="/tmp/kcadm-nexus.config"
readonly ATTRIBUTES="username email firstName lastName"

step() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { echo "ERRORE: $*" >&2; exit 1; }

[[ $EUID -eq 0 || -n ${NEXUS_BASE:-} ]] || fail "eseguire con sudo"
[[ -f $ENV_FILE ]] || fail "$ENV_FILE non trovato"
command -v python3 >/dev/null || fail "serve python3 (sul server: dnf install python3)"
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

step "Utenti con dati mancanti"
# con il profilo in sola lettura un utente senza email, nome o cognome non potrebbe completarli da solo: li corregge
# l'ADMIN dalla pagina «Dati» di NEXUS
kc "$KCADM get users --config $KCCONFIG -r nexus --fields username,email,firstName,lastName --limit 10000" \
  | python3 -I -c '
import json, sys
users = [u for u in json.load(sys.stdin) if not u.get("username", "").startswith("service-account-")]
missing = sum(1 for u in users if not (u.get("email") and u.get("firstName") and u.get("lastName")))
print(f"{len(users)} utenti, {missing} senza email, nome o cognome")
if missing:
    print("ATTENZIONE: correggili dalla pagina «Dati» di NEXUS, altrimenti al prossimo accesso restano bloccati")'

step "Username correggibile dall'amministrazione"
kc "$KCADM update realms/nexus --config $KCCONFIG -s editUsernameAllowed=true"
echo "attivo"

step "Profilo utente: $ATTRIBUTES modificabili solo dall'amministrazione"
profile="$(kc "$KCADM get users/profile --config $KCCONFIG -r nexus")"
updated="$(python3 -I -c '
import json, sys
profile = json.loads(sys.argv[1])
names = set(sys.argv[2].split())
found = set()
for attribute in profile["attributes"]:
    if attribute["name"] in names:
        attribute.setdefault("permissions", {})["edit"] = ["admin"]
        found.add(attribute["name"])
if found != names:
    sys.exit("attributi mancanti nel profilo: " + " ".join(sorted(names - found)))
print(json.dumps(profile))' "$profile" "$ATTRIBUTES")" || fail "profilo utente non riconosciuto"
printf '%s' "$updated" | kc "$KCADM update users/profile --config $KCCONFIG -r nexus -f - >/dev/null"

step "Controllo"
# prima tutta la risposta, poi il controllo: con pipefail, `kc … | grep -q` falliva a caso quando grep si fermava
# prima della fine dell'output (SIGPIPE), anche con il valore giusto
realm="$(kc "$KCADM get realms/nexus --config $KCCONFIG --fields editUsernameAllowed")"
grep -q '"editUsernameAllowed" : true' <<<"$realm" || fail "editUsernameAllowed non è attivo"
kc "$KCADM get users/profile --config $KCCONFIG -r nexus" | python3 -I -c '
import json, sys
names = sys.argv[1].split()
attributes = {a["name"]: a for a in json.load(sys.stdin)["attributes"]}
for name in names:
    edit = attributes[name].get("permissions", {}).get("edit")
    if edit != ["admin"]:
        sys.exit(f"{name}: modificabile da {edit}")
    print(f"{name}: modificabile solo dall'"'"'amministrazione")' "$ATTRIBUTES" || fail "profilo utente non aggiornato"

echo
echo "Fatto: l'ADMIN corregge i dati degli utenti dalla pagina «Dati» di NEXUS; dalla pagina «il mio account» di"
echo "Keycloak nessuno può più cambiare username, email, nome o cognome."
