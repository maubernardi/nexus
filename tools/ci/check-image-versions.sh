#!/usr/bin/env bash
# EN-6: le immagini dei test (Testcontainers, nel codice Java) devono essere le stesse della demo. Dependabot aggiorna
# compose e Dockerfile ma non le stringhe nel codice: questo controllo fa fallire la CI se divergono.
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

expect "PostgreSQL" "$postgres" infra/docker-compose.yml \
  backend/src/test/java/it/nexus/TestcontainersConfiguration.java \
  backend/src/test/java/it/nexus/config/FlywayRollbackIT.java
expect "Keycloak" "$keycloak" infra/docker-compose.yml \
  backend/src/test/java/it/nexus/services/identity/KeycloakIdentityProviderUsersIT.java
exit $fail
