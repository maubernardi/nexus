#!/usr/bin/env bash
# NEXUS — passaggio dalla demo alla produzione (US-1306), sul server, una volta sola. Decisione del PO del 20/11: si
# riporta tutto dalla demo tranne segnalazioni, utenti e beneficiari; le aziende vengono dal database attuale.
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<commit>/infra/deploy/passaggio-in-produzione.sh
#   curl -fsSLO https://raw.githubusercontent.com/maubernardi/nexus/<commit>/infra/deploy/pulizia-demo.sql
#   sudo bash passaggio-in-produzione.sh
# Prima (README, «Passaggio in produzione»): il deploy di un commit con US-1306, il file Access in
# /opt/nexus/import/nexus.mdb, i dati dell'ADMIN nel .env (NEXUS_ADMIN_*) e il backup configurato (US-1302).
# Passi; si ferma al primo errore e chiede conferma prima di toccare i dati:
#   1. controlli;
#   2. backup della demo su IONOS: è il punto a cui tornare;
#   3. ferma backend e sito: durante il passaggio nessuno lavora;
#   4. pulizia del database (pulizia-demo.sql), in una sola transazione;
#   5. utenti di Keycloak: via tutti quelli del realm nexus, resta l'account tecnico del backend;
#   6. importazione del database attuale (US-1309), con il resoconto;
#   7. .env senza dati dimostrativi, riavvio: il backend crea l'ADMIN e gli manda l'invito;
#   8. primo backup della produzione; poi, se confermi, cancellazione del file Access.
# Nel terminale vanno solo numeri e codici: nessun dato personale.
set -euo pipefail

readonly BASE="/opt/nexus"
readonly ENV_FILE="$BASE/.env"
readonly IMPORT_DIR="$BASE/import"
readonly ACCESS_FILE="$IMPORT_DIR/nexus.mdb"
readonly DONE_FILE="$BASE/.passaggio-in-produzione"
readonly KCADM="/opt/keycloak/bin/kcadm.sh"
readonly KCCONFIG="/tmp/kcadm-nexus.config"
SQL="$(cd "$(dirname "$0")" && pwd)/pulizia-demo.sql"
readonly SQL

step() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { echo "ERRORE: $*" >&2; exit 1; }
confirm() {
  local answer
  read -rp "$1 Scrivi SI per continuare: " answer
  [[ $answer == SI ]] || fail "interrotto da te, prima di questo passo"
}

[[ $EUID -eq 0 ]] || fail "eseguire con sudo"
[[ -f $ENV_FILE ]] || fail "$ENV_FILE non trovato"
[[ ! -f $DONE_FILE ]] || fail "il passaggio è già stato fatto: $(cat "$DONE_FILE")"
release="$(readlink -f "$BASE/current" 2>/dev/null || true)"
[[ -n $release && -f $release/compose.yaml ]] || fail "nessuna release in esecuzione in $BASE/current"

compose() {
  docker compose --project-directory "$release" -f "$release/compose.yaml" --env-file "$ENV_FILE" \
    --env-file "$release/release.env" "$@"
}
kc() { compose exec -T keycloak bash -c "$1"; }
env_value() { grep -E "^$1=" "$ENV_FILE" | tail -n1 | cut -d= -f2- || true; }

# passo in corso: se lo script si ferma dopo aver toccato qualcosa, dice come tornare alla demo
current_step=0
on_exit() {
  local rc=$?
  kc "rm -f $KCCONFIG" >/dev/null 2>&1 || true
  ((rc == 0 || current_step < 3 || current_step > 7)) && return
  echo >&2
  if ((current_step <= 4)); then
    echo "I dati non sono cambiati (la pulizia è una transazione). Per riavviare la demo:" >&2
  else
    echo "Per tornare alla demo: ripristina il backup del passo 2 (README, «Backup del database», ripristino sul" >&2
    echo "server) e togli dal .env la riga NEXUS_BACKEND_PROFILES=keycloak, se c'è. Poi riavvia:" >&2
  fi
  echo "  cd $release && sudo docker compose --env-file $ENV_FILE --env-file release.env up -d" >&2
}
trap on_exit EXIT
step_n() { current_step=$1; step "$1. $2"; }

step_n 1 "Controlli"
[[ -f $SQL ]] || fail "manca pulizia-demo.sql accanto allo script (secondo curl del README)"
[[ -f $ACCESS_FILE ]] || fail "manca il file Access: $ACCESS_FILE (README: scp del file)"
for v in NEXUS_ADMIN_USERNAME NEXUS_ADMIN_FIRST_NAME NEXUS_ADMIN_LAST_NAME NEXUS_ADMIN_EMAIL NEXUS_BACKUP_S3_BUCKET; do
  [[ -n $(env_value "$v") ]] || fail "manca $v nel .env"
done
compose exec -T backend true 2>/dev/null || fail "il backend non è in esecuzione"
# shellcheck disable=SC2016 # la variabile si legge dentro il container del backend
compose exec -T backend sh -c 'test -n "${NEXUS_ADMIN_EMAIL:-}"' \
  || fail "la release in esecuzione non passa i dati dell'ADMIN al backend: fai prima il deploy di US-1306"
kc "$KCADM config credentials --config $KCCONFIG --server http://localhost:8080 --realm master --user admin \
  --password \"\$KC_BOOTSTRAP_ADMIN_PASSWORD\" >/dev/null" \
  || fail "accesso a Keycloak come admin non riuscito: la password della console è ancora KEYCLOAK_ADMIN_PASSWORD?"
users=$(kc "$KCADM get users/count -r nexus --config $KCCONFIG")
echo "Release $(basename "$release"); file Access di $(stat -c %s "$ACCESS_FILE") byte; $users utenti nel realm nexus"

step_n 2 "Backup della demo su IONOS (il punto a cui tornare)"
compose exec -T backup nexus-backup backup || fail "backup non riuscito: niente è cambiato"
compose exec -T backup nexus-backup stato | grep -E "^Ultimo backup riuscito|^Blocco"

echo
echo "Da qui la demo cambia: si tolgono segnalazioni, beneficiari, utenti (anche in Keycloak) e aziende della demo,"
echo "poi entrano le aziende e le chiamate del database attuale. Il sito resta fermo per qualche minuto."
confirm "Procedo con il passaggio in produzione?"

step_n 3 "Fermo il backend e il sito"
compose stop backend web

step_n 4 "Pulizia del database (una transazione: o tutto o niente)"
compose exec -T postgres psql -X -v ON_ERROR_STOP=1 -U nexus_owner -d nexus <"$SQL" \
  || fail "pulizia non riuscita: la transazione è annullata e il database è come prima"

step_n 5 "Utenti di Keycloak (resta l'account tecnico del backend)"
removed=0
for id in $(kc "$KCADM get users -r nexus --config $KCCONFIG -q max=10000 --fields id --format csv --noquotes"); do
  kc "$KCADM delete users/$id -r nexus --config $KCCONFIG"
  removed=$((removed + 1))
done
left=$(kc "$KCADM get users/count -r nexus --config $KCCONFIG")
[[ $left == 0 ]] || fail "in Keycloak restano $left utenti"
client=$(kc "$KCADM get clients -r nexus --config $KCCONFIG -q clientId=nexus-backend-admin --fields id --format csv \
  --noquotes")
kc "$KCADM get clients/$client/service-account-user -r nexus --config $KCCONFIG --fields username --format csv \
  --noquotes" | grep -q '^service-account-nexus-backend-admin$' || fail "l'account tecnico del backend non c'è più"
echo "tolti $removed utenti; l'account tecnico del backend resta"

step_n 6 "Importazione del database attuale (US-1309)"
# il log resta accanto al file, leggibile solo con sudo: ha solo conteggi, ma non deve finire altrove
import_log="$IMPORT_DIR/importazione.log"
if ! compose run --rm --no-deps --user "$(stat -c %u:%g "$IMPORT_DIR")" -v "$IMPORT_DIR:/import" \
  -e PROFILES_ACTIVE=keycloak,legacy-import -e NEXUS_LEGACY_IMPORT_FILE=/import/nexus.mdb backend \
  >"$import_log" 2>&1; then
  tail -n 30 "$import_log" >&2
  fail "importazione non riuscita (log in $import_log)"
fi
grep -E "Importazione dal database attuale" "$import_log" | sed 's/^.* - //'
echo "Resoconto da controllare: $IMPORT_DIR/nexus.mdb-resoconto.txt"

step_n 7 "Produzione: niente dati dimostrativi, riavvio"
if grep -qE '^NEXUS_BACKEND_PROFILES=' "$ENV_FILE"; then
  sed -i -E 's/^NEXUS_BACKEND_PROFILES=.*/NEXUS_BACKEND_PROFILES=keycloak/' "$ENV_FILE"
else
  printf '\n# US-1306: produzione, senza i dati dimostrativi\nNEXUS_BACKEND_PROFILES=keycloak\n' >>"$ENV_FILE"
fi
compose up -d --wait --wait-timeout 420 || fail "la produzione non parte"
date '+%d/%m/%Y %H:%M' >"$DONE_FILE"
current_step=8
echo "In esecuzione senza il profilo seed. Entro un paio di minuti il backend crea l'ADMIN e gli manda l'invito."

step_n 8 "Primo backup della produzione"
compose exec -T backup nexus-backup backup || echo "ATTENZIONE: backup non riuscito, rilancialo dalle Actions"

echo
read -rp "Hai controllato il resoconto? Cancello il file Access dal server (dati personali)? Scrivi SI: " answer
if [[ $answer == SI ]]; then
  shred -u "$ACCESS_FILE"
  echo "File Access cancellato. Il resoconto resta in $IMPORT_DIR."
else
  echo "Il file Access resta in $ACCESS_FILE: cancellalo dopo il controllo con sudo shred -u $ACCESS_FILE"
fi

echo
echo "Fatto: NEXUS è in produzione. Ora: invito per email all'ADMIN, accesso, controllo della configurazione e delle"
echo "voci segnalate al passo 4, account degli utenti (README, «Passaggio in produzione»)."
