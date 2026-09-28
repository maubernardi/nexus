import js from '@eslint/js';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import { defineConfig, globalIgnores } from 'eslint/config';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default defineConfig([
  globalIgnores(['dist', 'dev-dist', 'coverage']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.strict,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
      jsxA11y.flatConfigs.strict,
    ],
    languageOptions: {
      ecmaVersion: 2023,
      globals: globals.browser,
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'error',
      '@typescript-eslint/consistent-type-imports': 'error',
      'no-console': ['error', { allow: ['warn', 'error'] }],
      'no-restricted-syntax': [
        'error',
        { selector: 'ExportDefaultDeclaration', message: 'Usare named export.' },
        { selector: 'TSEnumDeclaration', message: 'Usare union di tipi invece di enum.' },
      ],
      'no-restricted-imports': [
        'error',
        {
          patterns: [{ group: ['../*'], message: "Usare l'alias @/." }],
          // l'entry principale inietta un <style> inline, bloccato dalla CSP di produzione (style-src 'self')
          paths: [
            { name: 'react-toastify', message: "Usare 'react-toastify/unstyled' (CSS importato come file)." },
          ],
        },
      ],
    },
  },
  {
    // I file di configurazione dei tool richiedono export default.
    files: ['*.config.ts'],
    rules: { 'no-restricted-syntax': 'off' },
  },
  {
    // Primitive shadcn generate: esportano anche varianti CVA.
    files: ['src/components/ui/**/*.tsx'],
    rules: { 'react-refresh/only-export-components': 'off' },
  },
]);
