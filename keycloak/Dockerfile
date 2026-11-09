# Immagine nexus-keycloak: Keycloak ufficiale (ultima versione, EN-6) con il tema di accesso "nexus" e l'elenco delle
# password comuni (EN-7).
FROM quay.io/keycloak/keycloak:26.8.2
LABEL org.opencontainers.image.source="https://github.com/maubernardi/nexus" \
      org.opencontainers.image.title="nexus-keycloak" \
      org.opencontainers.image.description="NEXUS — Keycloak con il tema di accesso"
COPY themes/nexus /opt/keycloak/themes/nexus
# EN-7: password comuni rifiutate dalla policy passwordBlacklist(nexus-common-passwords.txt)
# (tools/keycloak/update-password-blacklist.sh)
COPY password-blacklists/nexus-common-passwords.txt /opt/keycloak/data/password-blacklists/nexus-common-passwords.txt
# Email di sicurezza (US-1308): l'ascoltatore "email" degli eventi, attivo nel realm, scrive all'utente solo quando cambia
# la password; senza questo filtro scriverebbe anche a ogni password sbagliata.
ENV KC_SPI_EVENTS_LISTENER__EMAIL__INCLUDE_EVENTS=UPDATE_CREDENTIAL
