#!/usr/bin/env bash
# NEXUS — passaggio di PostgreSQL a una nuova versione principale (EN-10), sul server, lanciato dal PO con sudo.
#   mkdir -p ~/aggiornamento && cd ~/aggiornamento
#   for f in infra/deploy/aggiorna-postgresql.sh backup/ricrea-database.sql backup/conteggi.sql; do
#     curl -fsSLO "https://raw.githubusercontent.com/maubernardi/nexus/<commit>/$f"; done
#   sudo bash aggiorna-postgresql.sh prepara    PRIMA del deploy della nuova versione; poi il deploy dalle Actions
#   sudo bash aggiorna-postgresql.sh verifica   DOPO il deploy: stessi numeri e un backup sulla nuova versione
#   sudo bash aggiorna-postgresql.sh pulisci    quando non serve più tornare indietro: toglie il volume vecchio
# La nuova versione non legge la cartella dei dati della precedente (l'immagine si rifiuta di partire): i dati passano
# con un backup e un ripristino in un volume nuovo, che porta la versione principale nel nome. Passi di «prepara»:
#   1. controlli;
#   2. fermo del sito (dopo la tua conferma);
#   3. backup su IONOS;
#   4. copia dei due database e riepilogo dei numeri;
#   5. PostgreSQL nuova temporanea sul volume nuovo;
#   6. ripristino, come nel README («Ripristino sul server»);
#   7. confronto dei numeri con l'origine.
# Il volume della versione precedente non si tocca: il deploy del commit precedente riparte da lì. Se un passo fallisce,
# lo script toglie il volume nuovo e riavvia il sito sulla versione precedente. Durante «prepara» nessun deploy può
# partire (stesso blocco di nexus-deploy). Nel terminale solo numeri e codici: nessun dato personale.
set -euo pipefail
umask 077

readonly NEW_IMAGE="postgres:19.0-alpine" # come infra/deploy/compose.yaml (controllo EN-6)
readonly NEW_VOLUME="postgres19-data"     # come infra/deploy/compose.yaml (controllo EN-6)
# NEXUS_BASE solo per la prova della CI (tools/ci/aggiorna-postgresql-e2e.sh)
readonly BASE="${NEXUS_BASE:-/opt/nexus}"
readonly ENV_FILE="$BASE/.env"
readonly WORK="$BASE/aggiornamento-postgresql"
readonly STATE="$WORK/stato"
readonly TEMP="nexus-aggiornamento-postgresql"
readonly USAGE="Uso: sudo bash $0 prepara | verifica | pulisci"
HERE="$(cd "$(dirname "$0")" && pwd)"
readonly HERE

step() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { echo "ERRORE: $*" >&2; exit 1; }
# la risposta si legge dal canale 3 (il terminale): i comandi di docker non leggono l'input e non possono «mangiarla»
exec 3<&0 </dev/null
confirm() {
  local answer=""
  read -rp "$1 Scrivi SI per continuare: " answer <&3 || true
  [[ $answer == SI ]] || fail "interrotto da te: non ho cambiato niente"
}
# file accanto allo script (sul server, scaricati con curl) oppure nella cartella backup del repository
sql_file() {
  local d
  for d in "$HERE" "$HERE/../../backup"; do
    [[ -f $d/$1 ]] && { echo "$d/$1"; return; }
  done
  fail "manca $1 accanto allo script (README, «Aggiornare PostgreSQL»)"
}
env_value() {
  local v
  v=$(grep -E "^$1=" "$ENV_FILE" | tail -n1 | cut -d= -f2- || true)
  v=${v%\"}; v=${v#\"}; v=${v%\'}; v=${v#\'}
  printf '%s' "$v"
}
state_value() { grep -E "^$1=" "$STATE" 2>/dev/null | tail -n1 | cut -d= -f2- || true; }

[[ -r $ENV_FILE ]] || fail "$ENV_FILE non leggibile: lancia lo script con sudo"
docker info >/dev/null 2>&1 || fail "Docker non risponde: lancia lo script con sudo"
release="$(readlink -f "$BASE/current" 2>/dev/null || true)"
[[ -n $release && -f $release/compose.yaml ]] || fail "nessuna release in esecuzione in $BASE/current"

compose() {
  docker compose --project-directory "$release" -f "$release/compose.yaml" --env-file "$ENV_FILE" \
    --env-file "$release/release.env" "$@"
}
# psql come superutente: nel PostgreSQL del compose e in quello temporaneo
pg() { compose exec -T postgres psql -X -q -v ON_ERROR_STOP=1 -U postgres "$@"; }
tmp_pg() { docker exec -i "$TEMP" psql -X -q -v ON_ERROR_STOP=1 -U postgres "$@"; }
major_of() { "$1" -d postgres -At -c 'SHOW server_version_num' | awk '{ print int($1 / 10000) }'; }
postgres_container() { compose ps -q postgres 2>/dev/null || true; }
data_volume() { # $1: container
  docker inspect -f '{{range .Mounts}}{{if eq .Destination "/var/lib/postgresql"}}{{.Name}}{{end}}{{end}}' "$1"
}

# righe per tabella, trigger, ultima migrazione, utenti di Keycloak per realm: solo numeri e nomi delle tabelle
riepilogo() { # $1: pg o tmp_pg
  local q=$1
  "$q" -d nexus -At -F ': ' <"$(sql_file conteggi.sql)" | sed 's/^/righe di /'
  echo "trigger dello schema nexus: $("$q" -d nexus -At -c "SELECT count(*) FROM pg_trigger t
    JOIN pg_class c ON c.oid = t.tgrelid JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'nexus' AND NOT t.tgisinternal")"
  if [[ $("$q" -d nexus -At -c "SELECT to_regclass('nexus.flyway_schema_history') IS NOT NULL") == t ]]; then
    echo "ultima migrazione: V$("$q" -d nexus -At -c "SELECT version FROM nexus.flyway_schema_history
      WHERE success AND version IS NOT NULL ORDER BY installed_rank DESC LIMIT 1")"
  fi
  if [[ $("$q" -d keycloak -At -c "SELECT to_regclass('public.user_entity') IS NOT NULL") == t ]]; then
    "$q" -d keycloak -At -F ': ' -c "SELECT 'utenti di Keycloak nel realm ' || r.name, count(u.id) FROM realm r
      LEFT JOIN user_entity u ON u.realm_id = r.id GROUP BY r.name ORDER BY r.name"
  else
    echo "tabelle del database keycloak: $("$q" -d keycloak -At -c "SELECT count(*) FROM pg_tables
      WHERE schemaname = 'public'")"
  fi
}
sintesi() { # $1: file del riepilogo
  awk -F': ' '/^righe di / { t++; r += $2 } !/^righe di / { print "  " $0 }
    END { printf "  %d tabelle dello schema nexus, %d righe in tutto\n", t, r }' "$1"
}

# --- prepara -----------------------------------------------------------------------------------------------------
site_stopped=false
volume_created=false
old_major=""
new_volume_name=""
on_exit_prepara() {
  local rc=$?
  docker rm -f "$TEMP" >/dev/null 2>&1 || true
  rm -f -- "$WORK/nexus.dump" "$WORK/keycloak.dump" "$WORK/pg.env"
  ((rc == 0)) && return
  echo >&2
  if [[ $volume_created == true ]] && docker volume rm "$new_volume_name" >/dev/null 2>&1; then
    echo "Ho tolto il volume nuovo $new_volume_name: si può riprovare da capo." >&2
  fi
  if [[ $site_stopped == true ]]; then
    echo "Riavvio il sito sulla PostgreSQL $old_major, che non è cambiata…" >&2
    if compose up -d --wait --wait-timeout 420 >/dev/null 2>&1; then
      echo "Il sito è di nuovo in esecuzione, com'era prima." >&2
    else
      echo "Il sito non riparte da solo. Comando:" >&2
      echo "  cd $release && sudo docker compose --env-file $ENV_FILE --env-file release.env up -d" >&2
    fi
  fi
}

cmd_prepara() {
  local container project old_volume new_major size need avail path existing=false users
  local ricrea
  ricrea=$(sql_file ricrea-database.sql)
  sql_file conteggi.sql >/dev/null
  trap on_exit_prepara EXIT

  step "1. Controlli"
  # lo stesso blocco di nexus-deploy: nessun deploy finché lo script lavora sui dati
  exec 9>"$BASE/.deploy.lock"
  flock -n 9 || fail "è in corso un deploy: riprova quando è finito"
  container=$(postgres_container)
  [[ -n $container ]] || fail "PostgreSQL non è in esecuzione"
  old_major=$(major_of pg)
  new_major=${NEW_IMAGE#postgres:}; new_major=${new_major%%.*}
  ((old_major < new_major)) || fail "PostgreSQL è già alla $old_major: niente da aggiornare"
  project=$(docker inspect -f '{{index .Config.Labels "com.docker.compose.project"}}' "$container")
  old_volume=$(data_volume "$container")
  new_volume_name="${project}_$NEW_VOLUME"
  [[ -n $old_volume && $old_volume != "$new_volume_name" ]] || fail "volume dei dati inatteso: '$old_volume'"
  if docker volume inspect "$new_volume_name" >/dev/null 2>&1; then
    users=$(docker ps -a --filter "volume=$new_volume_name" --format '{{.Names}}')
    [[ -z $users ]] || fail "il volume $new_volume_name è usato da: $users"
    existing=true
  fi
  [[ -n $(env_value NEXUS_BACKUP_S3_BUCKET) ]] || fail "backup non configurato (README, «Backup del database»)"
  size=$(pg -d postgres -At -c "SELECT sum(pg_database_size(datname)) FROM pg_database")
  need=$((2 * size + 1024 * 1024 * 1024))
  for path in "$BASE" "$(docker info -f '{{.DockerRootDir}}')"; do
    [[ -d $path ]] || continue
    avail=$(df -B1 --output=avail "$path" | tail -n1)
    ((avail > need)) \
      || fail "spazio libero insufficiente in $path: $((avail / 1048576)) MB, ne servono $((need / 1048576))"
  done
  echo "Scarico l'immagine $NEW_IMAGE (il sito è ancora acceso)…"
  docker pull -q "$NEW_IMAGE" >/dev/null || fail "non riesco a scaricare $NEW_IMAGE"
  echo "PostgreSQL: dalla $old_major alla $new_major"
  echo "Volume dei dati: $old_volume resta com'è; la $new_major va in $new_volume_name$([[ $existing == true ]] \
    && echo " (c'è già, da un tentativo precedente: lo ricreo)")"
  echo "Database: $((size / 1048576)) MB in tutto. Release in esecuzione: $(basename "$release")"
  echo
  echo "Da qui il sito resta fermo fino alla fine del deploy della nuova versione (circa 10-15 minuti)."
  confirm "Fermo il sito e preparo la PostgreSQL $new_major?"

  step "2. Fermo il sito (Caddy, backend e Keycloak)"
  site_stopped=true
  compose stop web backend keycloak

  step "3. Backup su IONOS (il punto a cui tornare se tutto il resto andasse storto)"
  compose exec -T backup nexus-backup backup || fail "backup non riuscito"

  step "4. Copia dei due database dalla PostgreSQL $old_major"
  install -d -m 700 "$WORK"
  local db
  for db in nexus keycloak; do
    compose exec -T postgres pg_dump -U postgres -Fc -d "$db" >"$WORK/$db.dump" || fail "copia di $db non riuscita"
    [[ -s $WORK/$db.dump ]] || fail "copia di $db vuota"
  done
  riepilogo pg >"$WORK/riepilogo-$old_major.txt"
  sintesi "$WORK/riepilogo-$old_major.txt"
  compose stop backup postgres
  {
    echo "versione_precedente=$old_major"
    echo "versione_nuova=$new_major"
    echo "volume_precedente=$old_volume"
    echo "volume_nuovo=$new_volume_name"
    echo "release_precedente=$(basename "$release")"
    echo "preparato=$(date '+%d/%m/%Y %H:%M')"
  } >"$STATE"

  step "5. PostgreSQL $new_major temporanea sul volume nuovo"
  [[ $existing == true ]] && docker volume rm "$new_volume_name" >/dev/null
  # con le etichette di compose: il deploy lo adotta come se l'avesse creato lui
  docker volume create --label "com.docker.compose.project=$project" \
    --label "com.docker.compose.volume=$NEW_VOLUME" "$new_volume_name" >/dev/null
  volume_created=true
  {
    echo "POSTGRES_PASSWORD=$(env_value POSTGRES_SUPERUSER_PASSWORD)"
    echo "NEXUS_OWNER_PASSWORD=$(env_value NEXUS_OWNER_PASSWORD)"
    echo "NEXUS_APP_PASSWORD=$(env_value NEXUS_APP_PASSWORD)"
    echo "KEYCLOAK_DB_PASSWORD=$(env_value KEYCLOAK_DB_PASSWORD)"
  } >"$WORK/pg.env"
  docker run -d --name "$TEMP" --network none --env-file "$WORK/pg.env" \
    -v "$new_volume_name:/var/lib/postgresql" -v "$release/postgres/init:/docker-entrypoint-initdb.d:ro" \
    "$NEW_IMAGE" >/dev/null
  rm -f -- "$WORK/pg.env"
  # al primo avvio PostgreSQL ascolta solo sul socket finché non ha creato ruoli e database: si attende il TCP
  local i
  for i in $(seq 1 90); do
    docker exec "$TEMP" pg_isready -q -h 127.0.0.1 -U postgres -d nexus 2>/dev/null && break
    ((i == 90)) && fail "la PostgreSQL $new_major non parte: docker logs $TEMP"
    sleep 1
  done
  echo "PostgreSQL $(major_of tmp_pg) pronta, con i ruoli del server."

  step "6. Ripristino nella PostgreSQL $new_major"
  tmp_pg -d postgres <"$ricrea"
  for db in nexus keycloak; do
    docker exec -i "$TEMP" pg_restore --exit-on-error -U postgres -d "$db" <"$WORK/$db.dump" \
      || fail "ripristino di $db non riuscito"
  done
  rm -f -- "$WORK/nexus.dump" "$WORK/keycloak.dump"
  # pg_restore non ricrea le statistiche: senza, le prime interrogazioni sarebbero lente
  docker exec "$TEMP" vacuumdb -U postgres --all --analyze-only --quiet
  echo "Ripristinati nexus e keycloak."

  step "7. Confronto con la PostgreSQL $old_major"
  riepilogo tmp_pg >"$WORK/riepilogo-$new_major.txt"
  if ! diff "$WORK/riepilogo-$old_major.txt" "$WORK/riepilogo-$new_major.txt" >&2; then
    fail "i numeri non coincidono (sopra: < prima, > dopo)"
  fi
  echo "Stessi numeri:"
  sintesi "$WORK/riepilogo-$new_major.txt"
  docker stop -t 60 "$TEMP" >/dev/null
  docker rm "$TEMP" >/dev/null
  volume_created=false
  site_stopped=false
  echo "pronto=$(date '+%d/%m/%Y %H:%M')" >>"$STATE"

  step "Fatto: la PostgreSQL $new_major è pronta"
  cat <<EOF
Il sito è fermo. Ora il deploy di main dalle Actions («Deploy»): parte la $new_major sul volume nuovo.
Dopo il deploy:  sudo bash $0 verifica
Per annullare invece, e tornare alla $old_major com'era:
  cd $release && sudo docker compose --env-file $ENV_FILE --env-file release.env up -d
EOF
}

# --- verifica ----------------------------------------------------------------------------------------------------
cmd_verifica() {
  local container old_major new_major volume
  [[ -n $(state_value pronto) ]] || fail "prima: sudo bash $0 prepara"
  old_major=$(state_value versione_precedente)
  new_major=$(state_value versione_nuova)
  container=$(postgres_container)
  [[ -n $container ]] || fail "PostgreSQL non è in esecuzione: il deploy è finito?"
  volume=$(data_volume "$container")
  [[ $(major_of pg) == "$new_major" && $volume == "$(state_value volume_nuovo)" ]] \
    || fail "in esecuzione c'è la PostgreSQL $(major_of pg) sul volume $volume: manca il deploy della nuova versione"

  step "1. PostgreSQL $new_major sul volume $volume: confronto con la $old_major"
  riepilogo pg >"$WORK/riepilogo-verifica.txt"
  local same=true
  if diff "$WORK/riepilogo-$old_major.txt" "$WORK/riepilogo-verifica.txt" >&2; then
    echo "Stessi numeri di prima dell'aggiornamento:"
  else
    same=false
    echo "ATTENZIONE: numeri diversi (sopra: < prima, > adesso). È normale solo se nel frattempo qualcuno ha lavorato"
    echo "o è girato un avviso automatico; altrimenti fermati e chiedi."
  fi
  sintesi "$WORK/riepilogo-verifica.txt"

  step "2. Backup sulla PostgreSQL $new_major"
  compose exec -T backup nexus-backup backup || fail "backup non riuscito: guarda i log in «Stato del server»"
  compose exec -T backup nexus-backup stato | grep -E "^Ultimo backup riuscito|^Blocco" || true
  echo "verificato=$(date '+%d/%m/%Y %H:%M')" >>"$STATE"

  step "Fatto"
  [[ $same == true ]] && echo "La PostgreSQL $new_major funziona, con gli stessi dati della $old_major."
  cat <<EOF
I dati della $old_major restano nel volume $(state_value volume_precedente): il deploy del commit
$(state_value release_precedente) riparte da lì, ma senza quello che si fa da adesso in poi.
Quando non serve più tornare indietro (dopo il passaggio in produzione):  sudo bash $0 pulisci
EOF
}

# --- pulisci -----------------------------------------------------------------------------------------------------
cmd_pulisci() {
  local container old_volume users new_major
  [[ -n $(state_value verificato) ]] || fail "prima: sudo bash $0 verifica"
  new_major=$(state_value versione_nuova)
  container=$(postgres_container)
  [[ -n $container && $(data_volume "$container") == "$(state_value volume_nuovo)" ]] \
    || fail "la PostgreSQL $new_major non è in esecuzione sul volume nuovo: non tolgo niente"
  [[ $(major_of pg) == "$new_major" ]] || fail "versione in esecuzione inattesa"
  old_volume=$(state_value volume_precedente)
  if ! docker volume inspect "$old_volume" >/dev/null 2>&1; then
    echo "Il volume $old_volume non c'è più."
  else
    users=$(docker ps -a --filter "volume=$old_volume" --format '{{.Names}}')
    [[ -z $users ]] || fail "il volume $old_volume è ancora usato da: $users"
    echo "Il volume $old_volume contiene i dati della PostgreSQL $(state_value versione_precedente) del" \
      "$(state_value preparato). Senza, non si torna più indietro (resta il backup su IONOS, per 30 giorni)."
    confirm "Cancello il volume $old_volume?"
    docker volume rm "$old_volume" >/dev/null
    echo "Volume $old_volume cancellato."
  fi
  rm -rf -- "$WORK"
  echo "Fatto: resta solo la PostgreSQL $new_major."
}

case "${1:-}" in
  prepara) cmd_prepara ;;
  verifica) cmd_verifica ;;
  pulisci) cmd_pulisci ;;
  *) echo "$USAGE" >&2; exit 2 ;;
esac
