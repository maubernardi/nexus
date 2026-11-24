#!/usr/bin/env bash
# EN-6: le immagini dei test (Testcontainers, nel codice Java) devono essere le stesse della produzione. Dependabot aggiorna
# compose e Dockerfile ma non le stringhe nel codice: questo controllo fa fallire la CI se divergono. Il backup
# (US-1302) usa la stessa immagine di PostgreSQL del server, così pg_dump ha la stessa versione del database; lo
# script dell'aggiornamento (EN-10) la usa per il ripristino.
set -euo pipefail
cd "$(dirname "$0")/../.."

fail=0
expect() { # $1 descrizione, $2 versione attesa, $3.. file
  local what=$1 want=$2; shift 2
  for f in "$@"; do
    if ! grep -q -- "$want" "$f"; then
      echo "::error file=$f::$what: attesa $want (come in produzione)"; fail=1
    fi
  done
}

postgres=$(grep -oE 'postgres:[0-9.]+-alpine' infra/deploy/compose.yaml | head -n1)
keycloak=$(grep -oE 'keycloak/keycloak:[0-9.]+' keycloak/Dockerfile | head -n1)
[[ -n $postgres && -n $keycloak ]] || { echo "::error::versioni di riferimento non trovate"; exit 1; }
echo "PostgreSQL: $postgres · Keycloak: $keycloak"

expect "PostgreSQL" "$postgres" infra/docker-compose.yml backup/Dockerfile backup/ripristino-di-prova.sh \
  infra/deploy/aggiorna-postgresql.sh

# tutte le immagini di PostgreSQL dei test, non solo quelle elencate (EN-10)
while IFS=: read -r f image; do
  if [[ $image != "$postgres" ]]; then
    echo "::error file=$f::PostgreSQL: $image, attesa $postgres (come in produzione)"; fail=1
  fi
done < <(grep -rHoE 'postgres:[0-9.]+-alpine' backend/src/test)

# EN-10: la nuova versione principale non legge i dati della precedente, quindi va su un volume nuovo con la versione
# nel nome. Una PR che cambia la versione principale senza volume nuovo fallisce qui: la procedura è nel README,
# «Aggiornare PostgreSQL»
major=${postgres#postgres:}; major=${major%%.*}
for f in infra/deploy/compose.yaml infra/docker-compose.yml; do
  if ! grep -qE "^ +- postgres${major}-data:/var/lib/postgresql" "$f"; then
    echo "::error file=$f::PostgreSQL $major: il volume dei dati deve essere postgres${major}-data (README," \
      "«Aggiornare PostgreSQL»)"; fail=1
  fi
done
grep -q "NEW_VOLUME=\"postgres${major}-data\"" infra/deploy/aggiorna-postgresql.sh \
  || { echo "::error file=infra/deploy/aggiorna-postgresql.sh::volume nuovo: atteso postgres${major}-data"; fail=1; }
expect "Keycloak" "$keycloak" infra/docker-compose.yml \
  backend/src/test/java/it/nexus/services/identity/KeycloakIdentityProviderUsersIT.java
exit $fail
