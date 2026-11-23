#!/usr/bin/env bash
# US-1302: prova completa del backup, dal database al servizio S3 e poi al ripristino, con l'immagine nexus-backup,
# PostgreSQL della produzione, VersityGW (S3 compatibile, con il blocco degli oggetti) e una chiave age generata al
# momento. Gira nella CI (job «Backup») e in locale: richiede Docker; age sul PC oppure, se manca, quello dell'immagine.
set -euo pipefail
cd "$(dirname "$0")/../.."

readonly NET=nexus-backup-e2e PG=nexus-backup-e2e-pg S3=nexus-backup-e2e-s3 STATE=nexus-backup-e2e-stato
readonly IMAGE=nexus-backup:e2e S3_IMAGE=versity/versitygw:v1.11.0 BUCKET=nexus-backup-prova
readonly S3_KEY=provakey S3_SECRET=provasecret-0123456789
PG_IMAGE=$(grep -oE 'postgres:[0-9.]+-alpine' infra/deploy/compose.yaml | head -n1)
readonly PG_IMAGE
export NEXUS_RIPRISTINO_PORT=54330

tmp=$(mktemp -d)
chmod 700 "$tmp"
cleanup() {
  docker rm -f -v "$PG" "$S3" nexus-ripristino >/dev/null 2>&1 || true
  docker volume rm "$STATE" >/dev/null 2>&1 || true
  docker network rm "$NET" >/dev/null 2>&1 || true
  rm -rf -- "$tmp"
}
trap cleanup EXIT
cleanup_start() { docker rm -f -v "$PG" "$S3" nexus-ripristino >/dev/null 2>&1 || true; }

step() { printf '\n==> %s\n' "$*"; }
ok() { echo "OK: $*"; }
ko() { echo "::error::Prova del backup: $*"; exit 1; }

cleanup_start
step "Immagine $IMAGE"
docker build -q -t "$IMAGE" backup >/dev/null
docker network create "$NET" >/dev/null

step "PostgreSQL ($PG_IMAGE) con i ruoli del server e dati di prova"
pw="prova-$(head -c 16 /dev/urandom | base64 | tr -dc 'A-Za-z0-9')"
docker run -d --name "$PG" --network "$NET" -e POSTGRES_PASSWORD="$pw" -e NEXUS_OWNER_PASSWORD="$pw" \
  -e NEXUS_APP_PASSWORD="$pw" -e KEYCLOAK_DB_PASSWORD="$pw" \
  -v "$PWD/infra/deploy/postgres/init:/docker-entrypoint-initdb.d:ro" "$PG_IMAGE" >/dev/null
for i in $(seq 1 60); do
  docker exec "$PG" pg_isready -q -h 127.0.0.1 -U postgres -d nexus 2>/dev/null && break
  [[ $i -eq 60 ]] && ko "PostgreSQL non parte"
  sleep 1
done
docker exec -i "$PG" psql -X -q -v ON_ERROR_STOP=1 -U nexus_owner -d nexus <<'SQL'
CREATE TABLE nexus.prova (id bigint PRIMARY KEY, testo text NOT NULL, dati bytea NOT NULL);
INSERT INTO nexus.prova SELECT g, 'DATO-IN-CHIARO-' || g, decode(md5(g::text), 'hex') FROM generate_series(1, 5000) g;
-- in sola aggiunta, come l'audit: il trigger deve tornare con il ripristino
CREATE TABLE nexus.registro (id bigint PRIMARY KEY);
CREATE FUNCTION nexus.registro_rifiuta() RETURNS trigger LANGUAGE plpgsql AS
$$ BEGIN RAISE EXCEPTION 'registro in sola aggiunta'; END $$;
CREATE TRIGGER trg_registro_sola_aggiunta BEFORE UPDATE OR DELETE ON nexus.registro
    FOR EACH ROW EXECUTE FUNCTION nexus.registro_rifiuta();
INSERT INTO nexus.registro SELECT generate_series(1, 10);
SQL
docker exec -i "$PG" psql -X -q -v ON_ERROR_STOP=1 -U keycloak -d keycloak <<'SQL'
CREATE TABLE realm (id text PRIMARY KEY, name text NOT NULL);
CREATE TABLE user_entity (id text PRIMARY KEY, realm_id text NOT NULL REFERENCES realm (id));
INSERT INTO realm VALUES ('r1', 'nexus'), ('r2', 'master');
INSERT INTO user_entity VALUES ('u1', 'r1'), ('u2', 'r1'), ('u3', 'r1'), ('u4', 'r2');
SQL
ok "database di prova pronti"

step "S3 di prova ($S3_IMAGE) con il blocco degli oggetti"
docker run -d --name "$S3" --network "$NET" -e ROOT_ACCESS_KEY="$S3_KEY" -e ROOT_SECRET_KEY="$S3_SECRET" \
  --entrypoint sh "$S3_IMAGE" -c 'mkdir -p /s3/dati /s3/versioni /s3/meta &&
    exec versitygw posix --versioning-dir /s3/versioni --sidecar /s3/meta /s3/dati' >/dev/null
aws() {
  docker run --rm --network "$NET" -u "$(id -u):$(id -g)" -e HOME=/tmp \
    -e AWS_ACCESS_KEY_ID="$S3_KEY" -e AWS_SECRET_ACCESS_KEY="$S3_SECRET" \
    -e AWS_REQUEST_CHECKSUM_CALCULATION=when_required -e AWS_RESPONSE_CHECKSUM_VALIDATION=when_required \
    -e AWS_EC2_METADATA_DISABLED=true -v "$tmp:/prova" --entrypoint aws "$IMAGE" \
    --endpoint-url "http://$S3:7070" --region us-east-1 --output json "$@"
}
for i in $(seq 1 30); do
  aws s3api list-buckets >/dev/null 2>&1 && break
  [[ $i -eq 30 ]] && ko "il servizio S3 di prova non parte"
  sleep 1
done
aws s3api create-bucket --bucket "$BUCKET" --object-lock-enabled-for-bucket >/dev/null
aws s3api put-object-lock-configuration --bucket "$BUCKET" --object-lock-configuration \
  '{"ObjectLockEnabled":"Enabled","Rule":{"DefaultRetention":{"Mode":"GOVERNANCE","Days":1}}}' >/dev/null
ok "contenitore $BUCKET con blocco GOVERNANCE di 1 giorno, come su IONOS"

step "Chiave age di prova"
docker run --rm --entrypoint age-keygen "$IMAGE" >"$tmp/chiave.txt" 2>/dev/null
recipient=$(docker run --rm -i --entrypoint age-keygen "$IMAGE" -y <"$tmp/chiave.txt")
[[ $recipient == age1* ]] || ko "chiave pubblica non valida"
ok "chiave pubblica ${recipient:0:12}…"

base_env=(-e PGHOST="$PG" -e NEXUS_VERSION=e2e -e NEXUS_OWNER_PASSWORD="$pw" -e KEYCLOAK_DB_PASSWORD="$pw")
s3_env=(-e NEXUS_BACKUP_S3_ENDPOINT="http://$S3:7070" -e NEXUS_BACKUP_ALLOW_HTTP=true
  -e NEXUS_BACKUP_S3_REGION=us-east-1 -e NEXUS_BACKUP_S3_BUCKET="$BUCKET" -e NEXUS_BACKUP_S3_ACCESS_KEY="$S3_KEY"
  -e NEXUS_BACKUP_AGE_RECIPIENT="$recipient")
nb() { # $@: opzioni di docker run, poi -- e il comando di nexus-backup
  local opts=()
  while [[ $1 != -- ]]; do opts+=("$1"); shift; done
  shift
  docker run --rm --network "$NET" -v "$STATE:/var/lib/postgresql" "${opts[@]}" "$IMAGE" "$@"
}

step "Stato prima del primo backup"
if out=$(nb -- stato); then ko "lo stato deve fallire senza backup"; fi
grep -q "nessun backup riuscito" <<<"$out" || ko "stato senza backup: $out"
ok "stato 1: nessun backup riuscito"

step "Backup senza configurazione"
if nb "${base_env[@]}" -- backup >"$tmp/log" 2>&1; then ko "senza configurazione il backup deve fallire"; fi
out=$(nb "${base_env[@]}" -- stato || true)
grep -q "non configurato, manca NEXUS_BACKUP_S3_ENDPOINT" <<<"$out" || ko "esito senza configurazione: $out"
ok "non configurato, con la variabile mancante"
if nb "${base_env[@]}" "${s3_env[@]}" -e NEXUS_BACKUP_ALLOW_HTTP=false -e NEXUS_BACKUP_S3_SECRET_KEY="$S3_SECRET" \
  -- backup >"$tmp/log" 2>&1; then ko "un endpoint HTTP deve essere rifiutato"; fi
grep -q "deve cominciare con https://" "$tmp/log" || ko "endpoint HTTP: $(cat "$tmp/log")"
ok "endpoint senza HTTPS rifiutato"

step "Caricamento rifiutato (chiave segreta sbagliata)"
if nb "${base_env[@]}" "${s3_env[@]}" -e NEXUS_BACKUP_S3_SECRET_KEY=sbagliata -- backup >"$tmp/log" 2>&1; then
  ko "con la chiave sbagliata il backup deve fallire"
fi
out=$(nb -- stato || true)
grep -q "errore, passo «caricamento»" <<<"$out" || ko "esito del caricamento fallito: $out"
ok "errore al passo «caricamento»"

step "Backup"
nb "${base_env[@]}" "${s3_env[@]}" -e NEXUS_BACKUP_S3_SECRET_KEY="$S3_SECRET" -- backup | tee "$tmp/log"
out=$(nb -- stato) || ko "stato dopo il backup: $out"
echo "$out"
grep -q "^OK$" <<<"$out" || ko "stato senza OK"
grep -q "Blocco: GOVERNANCE fino al" <<<"$out" || ko "il backup risulta senza blocco"
grep -qi "DATO-IN-CHIARO\|$pw" "$tmp/log" && ko "il log contiene dati o password"
# VersityGW non gestisce il ciclo di vita: si prova così l'avviso che chiede la regola dalla console
grep -q "regola di conservazione non impostata" <<<"$out" || ko "manca l'avviso sulla regola di conservazione"
ok "backup riuscito e protetto"

step "Un backup alla volta"
docker run -d --name nexus-backup-e2e-lock --network "$NET" -v "$STATE:/var/lib/postgresql" --entrypoint flock \
  "$IMAGE" /var/lib/postgresql/nexus-backup/.lock sleep 20 >/dev/null
sleep 2
out=$(nb "${base_env[@]}" "${s3_env[@]}" -e NEXUS_BACKUP_S3_SECRET_KEY="$S3_SECRET" -- backup 2>&1) \
  || ko "il secondo backup deve uscire senza errore"
docker rm -f -v nexus-backup-e2e-lock >/dev/null
grep -q "già in corso" <<<"$out" || ko "backup concorrente: $out"
ok "il secondo backup non parte"

step "Archivio su S3: cifrato e non cancellabile"
key=$(aws s3api list-objects-v2 --bucket "$BUCKET" --query 'Contents[0].Key' --output text)
[[ $key =~ ^nexus/[0-9]{4}/[0-9]{2}/nexus-[0-9]{8}-[0-9]{6}Z\.tar\.age$ ]] || ko "nome dell'oggetto inatteso: $key"
aws s3api get-object --bucket "$BUCKET" --key "$key" /prova/archivio.tar.age >/dev/null
head -c 21 "$tmp/archivio.tar.age" | grep -q "age-encryption.org/v1" || ko "l'archivio non è un file age"
grep -qa "DATO-IN-CHIARO" "$tmp/archivio.tar.age" && ko "l'archivio contiene dati in chiaro"
version=$(aws s3api list-object-versions --bucket "$BUCKET" --prefix "$key" --query 'Versions[0].VersionId' \
  --output text)
if aws s3api delete-object --bucket "$BUCKET" --key "$key" --version-id "$version" >/dev/null 2>&1; then
  ko "una versione bloccata è stata cancellata"
fi
ok "$key: cifrato, e la versione bloccata non si cancella"

step "Ripristino di prova"
if ! command -v age >/dev/null; then
  # in locale, senza age installato: quello dell'immagine, con /tmp condivisa (archivio, chiave e cartella di lavoro)
  printf '#!/bin/sh\nexec docker run --rm -i -u "%s" -v /tmp:/tmp --entrypoint age %s "$@"\n' "$(id -u)" "$IMAGE" \
    >"$tmp/age"
  chmod 755 "$tmp/age"
  export AGE="$tmp/age"
fi
backup/ripristino-di-prova.sh --tieni "$tmp/archivio.tar.age" "$tmp/chiave.txt" | tee "$tmp/ripristino.log"
counts() { docker exec -i "$1" psql -X -q -U postgres -d nexus -At -F ': ' <backup/conteggi.sql; }
diff <(counts "$PG") <(counts nexus-ripristino) || ko "righe diverse tra origine e ripristino"
grep -q "nexus: 3" "$tmp/ripristino.log" || ko "utenti di Keycloak non ritrovati"
grep -q "Trigger dello schema nexus: 1" "$tmp/ripristino.log" || ko "trigger non ripristinato"
if docker exec nexus-ripristino psql -X -q -U nexus_app -d nexus -c 'DELETE FROM nexus.registro' 2>/dev/null; then
  ko "il trigger di sola aggiunta non funziona dopo il ripristino"
fi
[[ $(docker exec nexus-ripristino psql -X -U nexus_app -d nexus -At -c 'SELECT count(*) FROM nexus.prova') == 5000 ]] \
  || ko "l'utente dell'app non legge i dati ripristinati"
docker rm -f -v nexus-ripristino >/dev/null
ok "righe uguali, trigger e permessi dell'app ripristinati"

step "Archivio alterato e chiave sbagliata"
cp "$tmp/archivio.tar.age" "$tmp/alterato.tar.age"
size=$(stat -c %s "$tmp/alterato.tar.age")
printf 'X' | dd of="$tmp/alterato.tar.age" bs=1 seek=$((size / 2)) conv=notrunc status=none
if backup/ripristino-di-prova.sh "$tmp/alterato.tar.age" "$tmp/chiave.txt" >"$tmp/log" 2>&1; then
  ko "un archivio alterato è stato ripristinato"
fi
grep -q "non riesco a decifrare" "$tmp/log" || ko "archivio alterato: $(cat "$tmp/log")"
docker run --rm --entrypoint age-keygen "$IMAGE" >"$tmp/altra-chiave.txt" 2>/dev/null
if backup/ripristino-di-prova.sh "$tmp/archivio.tar.age" "$tmp/altra-chiave.txt" >"$tmp/log" 2>&1; then
  ko "l'archivio è stato aperto con un'altra chiave"
fi
grep -q "non riesco a decifrare" "$tmp/log" || ko "chiave sbagliata: $(cat "$tmp/log")"
ok "archivio alterato e chiave sbagliata rifiutati"

printf '\nProva del backup riuscita.\n'
