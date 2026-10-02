/// <reference types="vitest/config" />
import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import { VitePWA } from 'vite-plugin-pwa';

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      // Aggiornamento solo su conferma dell'utente: mai ricaricare durante una compilazione.
      registerType: 'prompt',
      injectRegister: false,
      pwaAssets: { disabled: false, config: true },
      manifest: {
        id: '/',
        name: 'NEXUS — Area Lavoro e Call Center Sociale',
        short_name: 'NEXUS',
        description: 'Gestione segnalazioni e tirocini per Tutor territoriali e operatori Call Center.',
        lang: 'it',
        dir: 'ltr',
        start_url: '/',
        scope: '/',
        display: 'standalone',
        orientation: 'any',
        theme_color: '#014c17',
        background_color: '#fdfbf7',
        categories: ['productivity', 'business'],
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,svg,png,ico,webmanifest,woff2}'],
        navigateFallback: 'index.html',
        navigateFallbackDenylist: [/^\/api\//, /^\/actuator\//],
        cleanupOutdatedCaches: true,
        runtimeCaching: [
          {
            // Nessun dato personale in cache sul dispositivo (GDPR): le API vanno sempre in rete.
            urlPattern: ({ url }) => url.pathname.startsWith('/api/'),
            handler: 'NetworkOnly',
          },
        ],
      },
      devOptions: {
        enabled: false,
      },
    }),
  ],
  resolve: {
    // alias @/* letti da tsconfig (supporto nativo di Vite 8)
    tsconfigPaths: true,
  },
  build: {
    rolldownOptions: {
      output: {
        // librerie in chunk separati: cambiano raramente e restano in cache tra un rilascio e l'altro
        codeSplitting: {
          groups: [
            { name: 'react', test: /node_modules[\\/](react|react-dom|scheduler|react-router)[\\/]/ },
            {
              name: 'vendor',
              // keycloak-js e workbox-window restano import dinamici, caricati solo se servono
              test: (id: string) =>
                /node_modules[\\/]/.test(id) &&
                !/node_modules[\\/](keycloak-js|workbox-window)[\\/]/.test(id),
            },
          ],
        },
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
  preview: {
    port: 4173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/tests/setup.ts',
    css: false,
    // i controlli axe sui moduli grandi, con più file in parallelo, superano i 5 s predefiniti
    testTimeout: 15_000,
    alias: {
      'virtual:pwa-register/react': fileURLToPath(
        new URL('./src/tests/mocks/pwaRegister.ts', import.meta.url),
      ),
    },
  },
});
