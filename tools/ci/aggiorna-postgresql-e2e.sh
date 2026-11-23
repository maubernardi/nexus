#!/usr/bin/env bash
# EN-10: prova dello script di aggiornamento di PostgreSQL (infra/deploy/aggiorna-postgresql.sh) su uno stack ridotto,
# con lo stesso servizio postgres e gli stessi volumi del server. Servizi finti al posto di backend, Keycloak, sito e
# backup. Prova:
#   - i rifiuti: deploy in corso, risposta diversa da SI, backup che fallisce, ripristino che fallisce;
#   - l'aggiornamento e il deploy della nuova versione;
#   - la verifica, il ritorno alla versione precedente e la pulizia.
# Gira nella CI (job «Backup e ripristino») e in locale: richiede solo Docker.
set -euo pipefail
cd "$(dirname "$0")/../.."

readonly PROJECT=nexus-e2e-pg
readonly OLD_IMAGE="postgres:18.6-alpine" # la versione da cui si parte (quella del server prima di EN-10)
readonly OLD_KEY=postgres-data
NEW_IMAGE=$(grep -oE 'postgres:[0-9.]+-alpine' infra/deploy/compose.yaml | head -n1)
NEW_KEY=$(grep -oE 'postgres[0-9]*-data:/var/lib/postgresql' infra/deploy/compose.yaml | head -n1 | cut -d: -f1)
readonly NEW_IMAGE NEW_KEY
readonly SCRIPT="$PWD/infra/deploy/aggiorna-postgresql.sh"

tmp=$(mktemp -d)
export NEXUS_BASE="$tmp/nexus"
remove_stack() {
  local ids
  mapfile -t ids < <(docker ps -aq --filter "label=com.docker.compose.project=$PROJECT")
  ((${#ids[@]} == 0)) || docker rm -f -v "${ids[@]}" >/dev/null 2>&1 || true
  docker rm -f -v nexus-aggiornamento-postgresql >/dev/null 2>&1 || true
  docker volume rm "${PROJECT}_$OLD_KEY" "${PROJECT}_$NEW_KEY" >/dev/null 2>&1 || true
}
cleanup() {
  remove_stack
  docker network rm "${PROJECT}_default" >/dev/null 2>&1 || true
  rm -rf -- "$tmp"
}
trap cleanup EXIT

step() { printf '\n==> %s\n' "$*"; }
ok() { echo "OK: $*"; }
ko() { echo "::error::Prova dell'aggiornamento di PostgreSQL: $*"; exit 1; }

[[ -n $NEW_IMAGE && -n $NEW_KEY ]] || ko "immagine o volume di PostgreSQL non trovati in infra/deploy/compose.yaml"
remove_stack
# A37: i volumi che esistono già; alla fine non ne deve restare nessuno della prova
volumes_before=$(docker volume ls -q | sort)

step "Server finto in $NEXUS_BASE: release «vecchia» ($OLD_IMAGE) e «nuova» ($NEW_IMAGE, volume $NEW_KEY)"
pw="prova-$(head -c 16 /dev/urandom | base64 | tr -dc 'A-Za-z0-9')"
mkdir -p "$tmp/registro"
chmod 777 "$tmp/registro"
cat >"$tmp/nexus-backup" <<'EOF'
#!/bin/sh
# nexus-backup finto: registra le chiamate; fallisce se esiste /registro/fallisci
case "$1" in
  backup) [ -e /registro/fallisci ] && { echo "backup finto: errore"; exit 1; }
          echo backup >>/registro/chiamate; echo "backup finto riuscito" ;;
  stato) printf 'OK\nUltimo backup riuscito: adesso\nBlocco: GOVERNANCE fino al domani\n' ;;
esac
EOF
chmod 755 "$tmp/nexus-backup"
release() { # $1 nome, $2 immagine, $3 chiave del volume
  local dir="$NEXUS_BASE/releases/$1"
  mkdir -p "$dir/postgres/init"
  cp infra/deploy/postgres/init/10-create-roles.sh "$dir/postgres/init/"
  chmod 755 "$dir/postgres/init" "$dir/postgres/init/10-create-roles.sh"
  echo "NEXUS_VERSION=$1" >"$dir/release.env"
  cat >"$dir/compose.yaml" <<EOF
name: $PROJECT
x-finto: &finto
  image: $NEW_IMAGE
  entrypoint: ["tail", "-f", "/dev/null"]
  init: true
  healthcheck: { test: ["CMD", "true"], interval: 1s, retries: 3 }
services:
  postgres:
    image: $2
    environment:
      POSTGRES_PASSWORD: \${POSTGRES_SUPERUSER_PASSWORD:?}
      NEXUS_OWNER_PASSWORD: \${NEXUS_OWNER_PASSWORD:?}
      NEXUS_APP_PASSWORD: \${NEXUS_APP_PASSWORD:?}
      KEYCLOAK_DB_PASSWORD: \${KEYCLOAK_DB_PASSWORD:?}
    volumes:
      - $3:/var/lib/postgresql
      - ./postgres/init:/docker-entrypoint-initdb.d:ro
    healthcheck: { test: ["CMD-SHELL", "pg_isready -U postgres -d nexus"], interval: 1s, retries: 60 }
  keycloak: { <<: *finto, depends_on: { postgres: { condition: service_healthy } } }
  backend: { <<: *finto, depends_on: { postgres: { condition: service_healthy } } }
  web: { <<: *finto, depends_on: { backend: { condition: service_healthy }, keycloak: { condition: service_healthy } } }
  backup:
    <<: *finto
    volumes:
      - $tmp/nexus-backup:/usr/local/bin/nexus-backup:ro
      - $tmp/registro:/registro
    depends_on: { postgres: { condition: service_healthy } }
volumes:
  $3:
EOF
}
release vecchia "$OLD_IMAGE" "$OLD_KEY"
release nuova "$NEW_IMAGE" "$NEW_KEY"
cat >"$NEXUS_BASE/.env" <<EOF
POSTGRES_SUPERUSER_PASSWORD=$pw
NEXUS_OWNER_PASSWORD=$pw
NEXUS_APP_PASSWORD=$pw
KEYCLOAK_DB_PASSWORD=$pw
NEXUS_BACKUP_S3_BUCKET=prova
EOF
dc() { # $1 release, poi il comando di compose (come nexus-deploy)
  local dir="$NEXUS_BASE/releases/$1"
  docker compose --project-directory "$dir" -f "$dir/compose.yaml" --env-file "$NEXUS_BASE/.env" \
    --env-file "$dir/release.env" "${@:2}"
}
deploy() { # come nexus-deploy: release corrente, poi avvio con attesa dei servizi sani
  ln -sfn "$NEXUS_BASE/releases/$1" "$NEXUS_BASE/current"
  dc "$1" up -d --remove-orphans --wait --wait-timeout 180 2>&1
}
psql_in() { # $1 release, $2 utente, poi psql
  dc "$1" exec -T postgres psql -X -q -v ON_ERROR_STOP=1 -U "$2" "${@:3}"
}
counts() { psql_in "$1" postgres -d nexus -At -F ': ' <backup/conteggi.sql; }
running() { [[ -n $(dc "$1" ps -q --status running "$2") ]]; }
calls() { if [[ -f $tmp/registro/chiamate ]]; then wc -l <"$tmp/registro/chiamate"; else echo 0; fi; }
upgrade() { # $1 risposta, poi lo script e le sue opzioni
  local answer=$1; shift
  printf '%s\n' "$answer" | "$@" >"$tmp/log" 2>&1
}

deploy vecchia >/dev/null || ko "la release vecchia non parte"
psql_in vecchia nexus_owner -d nexus <<'SQL'
CREATE TABLE nexus.prova (id bigint PRIMARY KEY, testo text NOT NULL);
INSERT INTO nexus.prova SELECT g, 'riga ' || g FROM generate_series(1, 5000) g;
CREATE TABLE nexus.registro (id bigint PRIMARY KEY);
CREATE FUNCTION nexus.registro_rifiuta() RETURNS trigger LANGUAGE plpgsql AS
$$ BEGIN RAISE EXCEPTION 'registro in sola aggiunta'; END $$;
CREATE TRIGGER trg_registro_sola_aggiunta BEFORE UPDATE OR DELETE ON nexus.registro
    FOR EACH ROW EXECUTE FUNCTION nexus.registro_rifiuta();
INSERT INTO nexus.registro SELECT generate_series(1, 10);
CREATE TABLE nexus.flyway_schema_history (installed_rank int PRIMARY KEY, version text, success boolean NOT NULL);
INSERT INTO nexus.flyway_schema_history VALUES (1, '1', true), (2, '51', true);
SQL
psql_in vecchia keycloak -d keycloak <<'SQL'
CREATE TABLE realm (id text PRIMARY KEY, name text NOT NULL);
CREATE TABLE user_entity (id text PRIMARY KEY, realm_id text NOT NULL REFERENCES realm (id));
INSERT INTO realm VALUES ('r1', 'nexus'), ('r2', 'master');
INSERT INTO user_entity VALUES ('u1', 'r1'), ('u2', 'r1'), ('u3', 'r1'), ('u4', 'r2');
SQL
counts vecchia >"$tmp/conteggi-origine.txt"
[[ $(psql_in vecchia postgres -d postgres -At -c 'SHOW server_version_num') == 18* ]] \
  || ko "la release vecchia non è sulla 18"
ok "release vecchia in esecuzione sulla 18, con i dati di prova"

step "Rifiuto: deploy in corso"
exec 8>"$NEXUS_BASE/.deploy.lock"
flock 8
if upgrade SI "$SCRIPT" prepara; then ko "con un deploy in corso lo script deve fermarsi"; fi
exec 8>&-
grep -q "è in corso un deploy" "$tmp/log" || ko "deploy in corso: $(cat "$tmp/log")"
running vecchia backend || ko "il sito si è fermato"
ok "si ferma senza toccare niente"

step "Rifiuto: risposta diversa da SI"
if upgrade no "$SCRIPT" prepara; then ko "senza SI lo script deve fermarsi"; fi
grep -q "interrotto da te" "$tmp/log" || ko "risposta no: $(cat "$tmp/log")"
running vecchia backend || ko "il sito si è fermato"
docker volume inspect "${PROJECT}_$NEW_KEY" >/dev/null 2>&1 && ko "il volume nuovo non doveva esserci"
[[ $(calls) == 0 ]] || ko "il backup non doveva partire"
ok "niente è cambiato"

step "Errore al backup: il sito riparte sulla 18"
touch "$tmp/registro/fallisci"
if upgrade SI "$SCRIPT" prepara; then ko "con il backup fallito lo script deve fermarsi"; fi
rm -f "$tmp/registro/fallisci"
grep -q "backup non riuscito" "$tmp/log" || ko "backup fallito: $(cat "$tmp/log")"
grep -q "di nuovo in esecuzione" "$tmp/log" || ko "il sito non è ripartito: $(cat "$tmp/log")"
{ running vecchia backend && running vecchia web; } || ko "il sito non è in esecuzione"
ok "il sito è ripartito da solo"

step "Errore al ripristino: via il volume nuovo, il sito riparte sulla 18"
mkdir -p "$tmp/rotto"
cp "$SCRIPT" backup/conteggi.sql "$tmp/rotto/"
echo "SELECT * FROM tabella_che_non_esiste;" >"$tmp/rotto/ricrea-database.sql"
if upgrade SI bash "$tmp/rotto/aggiorna-postgresql.sh" prepara; then ko "con il ripristino fallito deve fermarsi"; fi
grep -q "Ho tolto il volume nuovo" "$tmp/log" || ko "volume nuovo non tolto: $(cat "$tmp/log")"
docker volume inspect "${PROJECT}_$NEW_KEY" >/dev/null 2>&1 && ko "il volume nuovo è rimasto"
running vecchia backend || ko "il sito non è ripartito"
diff "$tmp/conteggi-origine.txt" <(counts vecchia) || ko "i dati della 18 sono cambiati"
ls "$NEXUS_BASE"/aggiornamento-postgresql/*.dump >/dev/null 2>&1 && ko "le copie dei database sono rimaste"
ok "volume nuovo tolto, sito e dati della 18 come prima"

step "Aggiornamento"
before=$(calls)
upgrade SI "$SCRIPT" prepara || { cat "$tmp/log"; ko "aggiornamento non riuscito"; }
cat "$tmp/log"
grep -q "Stessi numeri" "$tmp/log" || ko "manca il confronto"
grep -qi "$pw" "$tmp/log" && ko "il log contiene la password"
[[ $(calls) == $((before + 1)) ]] || ko "manca il backup prima dell'aggiornamento"
running vecchia postgres && ko "la 18 è ancora accesa"
running vecchia backend && ko "il sito è ancora acceso"
docker ps -a --format '{{.Names}}' | grep -qx nexus-aggiornamento-postgresql && ko "il PostgreSQL temporaneo è rimasto"
ls "$NEXUS_BASE"/aggiornamento-postgresql/*.dump >/dev/null 2>&1 && ko "le copie dei database sono rimaste"
[[ $(docker volume inspect -f '{{index .Labels "com.docker.compose.project"}}' "${PROJECT}_$NEW_KEY") == "$PROJECT" ]] \
  || ko "il volume nuovo è senza le etichette di compose"
ok "volume nuovo pronto, sito fermo"

step "Rifiuto: verifica prima del deploy"
if upgrade SI "$SCRIPT" verifica; then ko "la verifica prima del deploy deve fermarsi"; fi
grep -q "manca il deploy" "$tmp/log" || grep -q "non è in esecuzione" "$tmp/log" || ko "verifica: $(cat "$tmp/log")"
ok "la verifica aspetta il deploy"

step "Deploy della nuova versione"
out=$(deploy nuova) || { echo "$out"; ko "la release nuova non parte"; }
# compose avvisa se il volume non è suo, e chiede di ricrearlo se la configurazione non coincide
if grep -qiE "volume .*(already exists|doesn't match)" <<<"$out"; then
  echo "$out"
  ko "compose non ha adottato il volume"
fi
[[ $(psql_in nuova postgres -d postgres -At -c 'SHOW server_version_num') == "${NEW_IMAGE:9:2}"* ]] \
  || ko "non è partita la nuova versione"
diff "$tmp/conteggi-origine.txt" <(counts nuova) || ko "righe diverse dopo il deploy"
if psql_in nuova nexus_app -d nexus -c 'DELETE FROM nexus.registro' 2>/dev/null; then
  ko "il trigger di sola aggiunta non funziona"
fi
[[ $(psql_in nuova nexus_app -d nexus -At -c 'SELECT count(*) FROM nexus.prova') == 5000 ]] \
  || ko "l'utente dell'app non legge i dati"
ok "la ${NEW_IMAGE:9:2} parte sul volume ripristinato, con trigger e permessi"

step "Verifica"
before=$(calls)
upgrade SI "$SCRIPT" verifica || { cat "$tmp/log"; ko "verifica non riuscita"; }
cat "$tmp/log"
grep -q "Stessi numeri di prima" "$tmp/log" || ko "la verifica non trova gli stessi numeri"
[[ $(calls) == $((before + 1)) ]] || ko "manca il backup della verifica"
ok "stessi numeri e backup"

step "Ritorno indietro: deploy della release vecchia"
deploy vecchia >/dev/null || ko "la 18 non riparte"
[[ $(psql_in vecchia postgres -d postgres -At -c 'SHOW server_version_num') == 18* ]] || ko "non è la 18"
diff "$tmp/conteggi-origine.txt" <(counts vecchia) || ko "i dati della 18 non sono più quelli"
if upgrade SI "$SCRIPT" pulisci; then ko "la pulizia con la 18 in esecuzione deve fermarsi"; fi
grep -q "non tolgo niente" "$tmp/log" || ko "pulizia con la 18: $(cat "$tmp/log")"
ok "la 18 riparte con i suoi dati, e la pulizia si rifiuta"

step "Pulizia"
deploy nuova >/dev/null || ko "la release nuova non riparte"
if upgrade no "$SCRIPT" pulisci; then ko "senza SI la pulizia deve fermarsi"; fi
docker volume inspect "${PROJECT}_$OLD_KEY" >/dev/null 2>&1 || ko "il volume vecchio è sparito senza SI"
upgrade SI "$SCRIPT" pulisci || { cat "$tmp/log"; ko "pulizia non riuscita"; }
docker volume inspect "${PROJECT}_$OLD_KEY" >/dev/null 2>&1 && ko "il volume vecchio è rimasto"
[[ -d $NEXUS_BASE/aggiornamento-postgresql ]] && ko "la cartella dell'aggiornamento è rimasta"
diff "$tmp/conteggi-origine.txt" <(counts nuova) || ko "righe diverse dopo la pulizia"
ok "volume della 18 cancellato, la ${NEW_IMAGE:9:2} con i dati"

step "Nessun volume lasciato sul disco (A37)"
remove_stack
left=$(comm -13 <(printf '%s\n' "$volumes_before") <(docker volume ls -q | sort))
[[ -z $left ]] || ko "restano volumi della prova: $(tr '\n' ' ' <<<"$left")"
ok "nessun volume della prova è rimasto"

printf "\nProva dell'aggiornamento di PostgreSQL riuscita.\n"
