#!/usr/bin/env bash
# NEXUS — ambiente di collaudo sul PC del PO (EN-11). Dal passaggio in produzione (24/11) sul server ci sono solo dati
# veri: le storie nuove si provano qui, prima del merge, con lo stack della produzione e i dati finti del seed.
#
#   infra/collaudo/collaudo.sh avvia <numero della PR | branch | commit>
#       costruisce le immagini di quel commit e avvia il collaudo, sempre dai dati finti (quelli di prima si perdono)
#   infra/collaudo/collaudo.sh ferma        ferma il collaudo; i dati restano
#   infra/collaudo/collaudo.sh riprendi     lo riavvia con i suoi dati, per esempio dopo aver spento il PC
#   infra/collaudo/collaudo.sh stato        cosa è avviato e dove
#   infra/collaudo/collaudo.sh cancella     toglie container, dati, immagini e sorgenti del collaudo
#
# Indirizzi, solo da questo PC: NEXUS http://localhost:8090, email http://localhost:8025, console di Keycloak
# http://localhost:8092/admin. Utenti: tutor1, tutor2, operatore.cc, admin; password: vedi PASSWORD qui sotto.
# Serve quello che serve allo sviluppo (README, «Prerequisiti»): Docker, JDK, Node e pnpm.
# Nessun segreto: password e chiavi sono finte e valgono solo su localhost.
set -euo pipefail

REPO="$(cd "$(dirname "$0")/../.." && pwd)"
readonly REPO
readonly HERE="$REPO/infra/collaudo"
readonly WORK="${NEXUS_COLLAUDO_DIR:-${XDG_CACHE_HOME:-$HOME/.cache}/nexus-collaudo}"
readonly SRC="$WORK/sorgenti"
readonly REL="$WORK/release"
readonly PROJECT=nexus-collaudo
readonly REGISTRY=nexus-collaudo
readonly PASSWORD="collaudo-nexus-2026"
readonly APP_URL=http://localhost:8090
readonly AUTH_URL=http://localhost:8091
readonly ADMIN_URL=http://localhost:8092
readonly MAIL_URL=http://localhost:8025
readonly PORTS=(8090 8091 8092 8025)
readonly USAGE="Uso: $0 avvia <numero della PR | branch | commit> | ferma | riprendi | stato | cancella"

step() { printf '\n\033[1m==> %s\033[0m\n' "$*"; }
fail() { echo "ERRORE: $*" >&2; exit 1; }

compose() {
  docker compose -p "$PROJECT" --project-directory "$REL" -f "$REL/compose.yaml" -f "$REL/compose.collaudo.yaml" \
    --env-file "$REL/.env" --env-file "$REL/release.env" "$@"
}

# A37: container, volumi e rete del collaudo, anche se mancano i file della release
remove_stack() {
  local ids vols
  mapfile -t ids < <(docker ps -aq --filter "label=com.docker.compose.project=$PROJECT")
  ((${#ids[@]} == 0)) || docker rm -f -v "${ids[@]}" >/dev/null
  mapfile -t vols < <(docker volume ls -q --filter "label=com.docker.compose.project=$PROJECT")
  ((${#vols[@]} == 0)) || docker volume rm "${vols[@]}" >/dev/null
  docker network rm "${PROJECT}_default" >/dev/null 2>&1 || true
}

# numero della PR, branch o commit -> SHA completo. I branch si prendono sempre da GitHub, aggiornati
resolve() {
  local ref=$1
  if [[ $ref =~ ^[0-9]{1,6}$ ]]; then
    git -C "$REPO" fetch -q origin "pull/$ref/head" 2>/dev/null || fail "PR #$ref non trovata su GitHub"
    git -C "$REPO" rev-parse FETCH_HEAD
  elif [[ $ref =~ ^[0-9a-f]{40}$ ]] && git -C "$REPO" cat-file -e "$ref^{commit}" 2>/dev/null; then
    echo "$ref"
  elif git -C "$REPO" fetch -q origin "$ref" 2>/dev/null; then
    git -C "$REPO" rev-parse FETCH_HEAD
  else
    git -C "$REPO" rev-parse --verify -q "$ref^{commit}" || fail "«$ref» non è una PR, un branch o un commit"
  fi
}

check_ports() {
  local port busy=()
  for port in "${PORTS[@]}"; do
    if (exec 3<>"/dev/tcp/127.0.0.1/$port") 2>/dev/null; then busy+=("$port"); fi
  done
  ((${#busy[@]} == 0)) || fail "porte già usate sul PC: ${busy[*]}. Chiudi il programma che le usa e riprova"
}

# sorgenti del commit, fuori dal checkout: il lavoro in corso nel repository non cambia
build() {
  local sha=$1 image
  step "Sorgenti di ${sha:0:7}"
  rm -rf -- "$SRC"
  mkdir -p "$SRC"
  git -C "$REPO" archive "$sha" | tar -x -C "$SRC"
  if docker image inspect "$REGISTRY/nexus-backend:$sha" "$REGISTRY/nexus-web:$sha" \
    "$REGISTRY/nexus-keycloak:$sha" >/dev/null 2>&1; then
    echo "immagini già costruite"
    return
  fi
  step "Backend (senza test: li fa la CI della PR)"
  (cd "$SRC/backend" && ./mvnw -B -q -DskipTests package)
  step "PWA, con Keycloak su $AUTH_URL"
  (cd "$SRC/frontend" && pnpm install --frozen-lockfile --reporter=silent \
    && VITE_AUTH_MODE=keycloak VITE_KEYCLOAK_URL="$AUTH_URL" pnpm build >/dev/null)
  step "Immagini"
  for image in backend:backend web:frontend keycloak:keycloak; do
    docker build -q -t "$REGISTRY/nexus-${image%%:*}:$sha" "$SRC/${image##*:}" >/dev/null
    echo "nexus-${image%%:*}"
  done
}

# la release del collaudo: compose e init del database del commit, più i file del collaudo
prepare_release() {
  local sha=$1
  rm -rf -- "$REL"
  mkdir -p "$REL/keycloak" "$REL/collaudo"
  cp "$SRC/infra/deploy/compose.yaml" "$REL/compose.yaml"
  cp -r "$SRC/infra/deploy/postgres" "$REL/postgres"
  cp "$HERE/compose.yaml" "$REL/compose.collaudo.yaml"
  cp "$HERE/Caddyfile" "$REL/collaudo/Caddyfile"
  python3 -I "$HERE/realm-collaudo.py" "$SRC/infra/deploy/keycloak/nexus-realm.json" \
    "$SRC/infra/keycloak/nexus-realm.json" "$REL/keycloak/nexus-realm.json" "$PASSWORD"
  cat >"$REL/.env" <<EOF
NEXUS_REGISTRY=$REGISTRY
NEXUS_DOMAIN=localhost:8090
NEXUS_AUTH_DOMAIN=localhost:8091
NEXUS_BACKEND_PROFILES=keycloak,seed
POSTGRES_SUPERUSER_PASSWORD=collaudo-postgres
NEXUS_OWNER_PASSWORD=collaudo-owner
NEXUS_APP_PASSWORD=collaudo-app
KEYCLOAK_DB_PASSWORD=collaudo-keycloak
KEYCLOAK_ADMIN_PASSWORD=$PASSWORD
NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET=collaudo-backend-admin
NEXUS_SMTP_PASSWORD=collaudo
EOF
  echo "NEXUS_VERSION=$sha" >"$REL/release.env"
}

# $1 cosa, $2 indirizzo, $3 risposta attesa. Keycloak sano può rispondere 503 per qualche secondo, mentre finisce di
# importare il realm: si riprova per un minuto
expect_code() {
  local code i
  for ((i = 0; i < 30; i++)); do
    code=$(curl -s -o /dev/null -w '%{http_code}' "$2" || true)
    [[ $code == "$3" ]] && return
    sleep 2
  done
  fail "$1 risponde $code invece di $3 ($2)"
}

check() {
  step "Controlli"
  expect_code "NEXUS" "$APP_URL/" 200
  expect_code "Le API senza accesso" "$APP_URL/api/v1/me" 401
  expect_code "Keycloak" "$AUTH_URL/realms/nexus" 200
  expect_code "L'accesso degli amministratori di Keycloak" "$AUTH_URL/realms/master" 403
  expect_code "La casella delle email" "$MAIL_URL/" 200
  echo "tutto a posto"
}

summary() {
  # shellcheck disable=SC1091
  source "$WORK/stato"
  cat <<EOF

Collaudo di $RIF (${SHA:0:7}), avviato il $AVVIATO.
  NEXUS:               $APP_URL
  Email inviate:       $MAIL_URL
  Console di Keycloak: $ADMIN_URL/admin (utente admin)
  Utenti:              tutor1, tutor2 (Tutor) · operatore.cc (Call Center) · admin (ADMIN)
  Password di tutti:   $PASSWORD
Dati finti del seed: si possono cambiare liberamente. Fine del collaudo: $0 cancella
EOF
}

cmd_avvia() {
  local ref=${1:-} sha
  [[ -n $ref ]] || fail "$USAGE"
  command -v python3 >/dev/null || fail "serve python3"
  sha=$(resolve "$ref")
  step "Collaudo di $ref (${sha:0:7})"
  echo "si riparte dai dati finti: il collaudo precedente, se c'è, si cancella"
  remove_stack
  check_ports
  mkdir -p "$WORK"
  build "$sha"
  prepare_release "$sha"
  step "Avvio: Keycloak e backend impiegano un paio di minuti"
  compose up -d --wait --wait-timeout 420 web mailpit
  printf 'RIF=%q\nSHA=%q\nAVVIATO=%q\n' "$ref" "$sha" "$(date '+%d/%m/%Y alle %H:%M')" >"$WORK/stato"
  check
  summary
}

need_release() { [[ -f $REL/release.env && -f $WORK/stato ]] || fail "nessun collaudo preparato: $0 avvia <PR>"; }

cmd_ferma() {
  need_release
  compose stop
  echo "Collaudo fermato; i dati restano. Per riavviarlo: $0 riprendi"
}

cmd_riprendi() {
  need_release
  compose up -d --wait --wait-timeout 420 web mailpit
  check
  summary
}

cmd_stato() {
  if [[ ! -f $WORK/stato ]]; then
    echo "Nessun collaudo preparato."
    return
  fi
  summary
  echo
  compose ps --format 'table {{.Service}}\t{{.Status}}'
}

cmd_cancella() {
  local images
  remove_stack
  mapfile -t images < <(docker images -q --filter "reference=$REGISTRY/*" | sort -u)
  ((${#images[@]} == 0)) || docker rmi -f "${images[@]}" >/dev/null
  rm -rf -- "$WORK"
  echo "Collaudo cancellato: container, dati, immagini e sorgenti."
}

case "${1:-}" in
  avvia) cmd_avvia "${2:-}" ;;
  ferma) cmd_ferma ;;
  riprendi) cmd_riprendi ;;
  stato) cmd_stato ;;
  cancella) cmd_cancella ;;
  *) fail "$USAGE" ;;
esac
