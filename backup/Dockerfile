# Immagine nexus-backup (US-1302): backup notturno cifrato dei database di NEXUS e Keycloak su IONOS Object Storage.
# Base: la stessa immagine di PostgreSQL del server, così pg_dump ha la stessa versione del database (EN-6).
# Progetto: openspec/changes/archive/2026-11-23-add-database-backup/design.md; procedura nel README principale.
FROM postgres:18.6-alpine
LABEL org.opencontainers.image.source="https://github.com/maubernardi/nexus" \
      org.opencontainers.image.title="nexus-backup" \
      org.opencontainers.image.description="NEXUS — backup notturno cifrato dei database"
# age: cifratura con la chiave pubblica del PO; aws-cli: caricamento su IONOS (S3); tzdata: le 3:30 ora italiana.
# Le versioni dei pacchetti sono quelle del ramo di Alpine della base: cambiano con le PR di Dependabot della base.
RUN apk add --no-cache age aws-cli tzdata \
 && install -d -o postgres -g postgres -m 700 /var/lib/postgresql/nexus-backup
COPY --chmod=755 nexus-backup /usr/local/bin/nexus-backup
# lo stato sta in /var/lib/postgresql, il volume dichiarato dalla base: nel compose ci va il volume `backup-state`
ENV TZ=Europe/Rome \
    NEXUS_BACKUP_STATE_DIR=/var/lib/postgresql/nexus-backup
USER postgres
WORKDIR /var/lib/postgresql
STOPSIGNAL SIGTERM
ENTRYPOINT ["/usr/local/bin/nexus-backup"]
CMD ["pianifica"]
