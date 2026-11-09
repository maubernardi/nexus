#!/usr/bin/env bash
# EN-7: rigenera keycloak/password-blacklists/nexus-common-passwords.txt, l'elenco delle password comuni che Keycloak
# rifiuta (policy passwordBlacklist). Fonte: SecLists (licenza MIT), il milione di password più usate di xato.net,
# fissato a un commit e verificato con lo SHA-256. Si tengono solo quelle da 12 caratteri in su: le più corte le rifiuta
# già la policy length(12). In minuscolo (Keycloak confronta senza distinguere le maiuscole), senza doppioni.
#   tools/keycloak/update-password-blacklist.sh
set -euo pipefail
cd "$(dirname "$0")/../.."

readonly COMMIT=c205c36a445bff37f8e58a9ec829105cd4975c58
readonly FILE=Passwords/Common-Credentials/xato-net-10-million-passwords-1000000.txt
readonly SHA256=424a3e03a17df0a2bc2b3ca749d81b04e79d59cb7aeec8876a5a3f308d0caf51
readonly OUT=keycloak/password-blacklists/nexus-common-passwords.txt

tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT
curl -fsSL -o "$tmp" "https://raw.githubusercontent.com/danielmiessler/SecLists/$COMMIT/$FILE"
echo "$SHA256  $tmp" | sha256sum -c --quiet
awk 'length($0) >= 12' "$tmp" | tr 'A-Z' 'a-z' | LC_ALL=C sort -u > "$OUT"
echo "$(wc -l < "$OUT") password in $OUT"
