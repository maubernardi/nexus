#!/usr/bin/env bash
# NEXUS — ripristino di prova di un backup (US-1302), sul PC del PO: Linux o WSL, con Docker e age.
#   backup/ripristino-di-prova.sh [--tieni] <archivio.tar.age> <file della chiave privata age>
# - Decifra l'archivio in una cartella temporanea leggibile solo da te, e controlla le impronte.
# - Ripristina i due database in un PostgreSQL temporaneo, con la stessa immagine e gli stessi ruoli del server e la
#   stessa procedura del README (ricrea-database.sql, poi pg_restore).
# - Stampa solo numeri: righe per tabella, ultima migrazione, trigger, utenti di Keycloak.
# Con --tieni il database resta acceso su 127.0.0.1:5433 (NEXUS_RIPRISTINO_PORT), per avviarci il backend in locale;
# senza, alla fine si cancella tutto. La chiave privata non viene copiata né stampata.
set -euo pipefail
cd "$(dirname "$0")/.."

readonly IMAGE="postgres:18.6-alpine" # come infra/deploy/compose.yaml (controllo EN-6)
readonly NAME="nexus-ripristino"
readonly PORT="${NEXUS_RIPRISTINO_PORT:-5433}"
readonly AGE="${AGE:-age}"

step() { printf '\n==> %s\n' "$*"; }
fail() { echo "ERRORE: $*" >&2; exit 1; }

keep=false
if [[ ${1:-} == --tieni ]]; then keep=true; shift; fi
[[ $# -eq 2 ]] || { echo "Uso: $0 [--tieni] <archivio.tar.age> <file della chiave privata age>" >&2; exit 2; }
archive=$1
identity=$2
[[ -f $archive ]] || fail "archivio non trovato: $archive"
[[ -f $identity ]] || fail "chiave privata non trovata: $identity"
command -v "$AGE" >/dev/null || fail "manca age: sudo apt install age"
command -v docker >/dev/null || fail "manca Docker"
# con Docker Desktop spento, in WSL il comando c'è ma non risponde (21/11)
docker info >/dev/null 2>&1 || fail "Docker non risponde: avvia Docker Desktop e riprova"

work=$(mktemp -d)
chmod 700 "$work"
cleanup() {
  rm -rf -- "$work"
  if [[ $keep != true ]]; then docker rm -f "$NAME" >/dev/null 2>&1 || true; fi
}
trap cleanup EXIT

step "Decifratura"
if ! "$AGE" --decrypt --identity "$identity" --output "$work/archivio.tar" "$archive" 2>"$work/age.err"; then
  sed 's/^/  age: /' "$work/age.err" >&2
  fail "non riesco a decifrare l'archivio: la chiave non è quella giusta, o l'archivio è stato alterato"
fi
tar -C "$work" -xf "$work/archivio.tar" MANIFEST.txt SHA256SUMS nexus.dump keycloak.dump \
  || fail "l'archivio non contiene i file attesi"
rm -f -- "$work/archivio.tar"
(cd "$work" && sha256sum --check --quiet SHA256SUMS) || fail "le impronte non coincidono: archivio alterato"
echo "Impronte corrette."
sed 's/^/  /' "$work/MANIFEST.txt"

step "PostgreSQL temporaneo ($IMAGE, 127.0.0.1:$PORT)"
docker rm -f "$NAME" >/dev/null 2>&1 || true
password=$(head -c 32 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24)
docker run -d --name "$NAME" -p "127.0.0.1:$PORT:5432" \
  -e POSTGRES_PASSWORD="$password" -e NEXUS_OWNER_PASSWORD="$password" -e NEXUS_APP_PASSWORD="$password" \
  -e KEYCLOAK_DB_PASSWORD="$password" \
  -v "$PWD/infra/deploy/postgres/init:/docker-entrypoint-initdb.d:ro" "$IMAGE" >/dev/null
# durante il primo avvio PostgreSQL ascolta solo sul socket: si attende quello vero, in TCP
for i in $(seq 1 60); do
  docker exec "$NAME" pg_isready -q -h 127.0.0.1 -U postgres -d nexus 2>/dev/null && break
  [[ $i -eq 60 ]] && fail "PostgreSQL non parte: docker logs $NAME"
  sleep 1
done

psql() { docker exec -i "$NAME" psql -X -q -v ON_ERROR_STOP=1 -U postgres "$@"; }

step "Ripristino (stessa procedura del server)"
psql -d postgres <backup/ricrea-database.sql
docker exec -i "$NAME" pg_restore --exit-on-error --no-password -U postgres -d nexus <"$work/nexus.dump" \
  || fail "ripristino di nexus non riuscito"
docker exec -i "$NAME" pg_restore --exit-on-error --no-password -U postgres -d keycloak <"$work/keycloak.dump" \
  || fail "ripristino di keycloak non riuscito"
rm -f -- "$work/nexus.dump" "$work/keycloak.dump"
echo "Ripristinati nexus e keycloak."

step "Dati ritrovati (solo numeri)"
echo "Righe per tabella (schema nexus):"
psql -d nexus -At -F ': ' <backup/conteggi.sql | sed 's/^/  /'
echo "Trigger dello schema nexus: $(psql -d nexus -At -c "SELECT count(*) FROM pg_trigger t
  JOIN pg_class c ON c.oid = t.tgrelid JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE n.nspname = 'nexus' AND NOT t.tgisinternal")"
if [[ $(psql -d nexus -At -c "SELECT to_regclass('nexus.flyway_schema_history') IS NOT NULL") == t ]]; then
  echo "Ultima migrazione: V$(psql -d nexus -At -c "SELECT version FROM nexus.flyway_schema_history
    WHERE success AND version IS NOT NULL ORDER BY installed_rank DESC LIMIT 1")"
fi
if [[ $(psql -d keycloak -At -c "SELECT to_regclass('public.user_entity') IS NOT NULL") == t ]]; then
  echo "Utenti di Keycloak per realm:"
  psql -d keycloak -At -F ': ' -c "SELECT r.name, count(u.id) FROM realm r
    LEFT JOIN user_entity u ON u.realm_id = r.id GROUP BY r.name ORDER BY r.name" | sed 's/^/  /'
else
  echo "Tabelle del database keycloak: $(psql -d keycloak -At -c "SELECT count(*) FROM pg_tables
    WHERE schemaname = 'public'")"
fi

step "Fatto: il ripristino riesce"
if [[ $keep == true ]]; then
  cat <<EOF
Il database ripristinato resta acceso su 127.0.0.1:$PORT. Password di prova di tutti gli utenti: $password
Per vedere i dati con il backend in locale (cartella backend):
  PROFILES_ACTIVE=postgresql,security-mock DB_URL=jdbc:postgresql://localhost:$PORT/nexus DB_USERNAME=nexus_app \\
  DB_PASSWORD=$password FLYWAY_USERNAME=nexus_owner FLYWAY_PASSWORD=$password ./mvnw spring-boot:run
Per spegnerlo e cancellarlo: docker rm -f $NAME
EOF
else
  echo "Database temporaneo e file decifrati cancellati."
fi
