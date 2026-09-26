/// <reference types="vitest/config" />
import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import { VitePWA } from 'vite-plugin-pwa';
import tsconfigPaths from 'vite-tsconfig-paths';

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    tsconfigPaths(),
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
        theme_color: '#0b5560',
        background_color: '#ffffff',
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
    alias: {
      'virtual:pwa-register/react': fileURLToPath(
        new URL('./src/tests/mocks/pwaRegister.ts', import.meta.url),
      ),
    },
  },
});
