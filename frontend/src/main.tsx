import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import '@/i18n';
import '@/index.css';
import { App } from '@/App';
import { initAuth } from '@/config/auth/authService';

const root = document.getElementById('root');
if (!root) {
  throw new Error('Elemento #root mancante');
}

await initAuth();

createRoot(root).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
