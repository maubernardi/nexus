# Immagine nexus-web: Caddy (TLS, reverse proxy) con la PWA già compilata (CI: pnpm build con le variabili VITE_*).
FROM caddy:2.11.4-alpine
LABEL org.opencontainers.image.source="https://github.com/maubernardi/nexus" \
      org.opencontainers.image.title="nexus-web" \
      org.opencontainers.image.description="NEXUS — PWA e reverse proxy HTTPS"
COPY Caddyfile /etc/caddy/Caddyfile
COPY dist/ /srv/
