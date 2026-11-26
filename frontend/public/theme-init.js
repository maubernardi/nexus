// Tema scelto dall'utente (US-1312), applicato prima del primo disegno: la pagina non lampeggia nel tema sbagliato.
// Senza scelta vale il tema del sistema operativo (index.css). Chiave: THEME_STORAGE_KEY in src/hooks/useTheme.ts.
try {
  var theme = localStorage.getItem('nexus-theme');
  if (theme === 'light' || theme === 'dark') document.documentElement.dataset.theme = theme;
} catch (e) {
  // spazio locale non disponibile: si segue il sistema
}
