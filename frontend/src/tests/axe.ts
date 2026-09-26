import axe from 'axe-core';
import { expect } from 'vitest';

/**
 * Esegue axe-core sulle regole WCAG 2.x A/AA. Il contrasto colori è escluso perché jsdom non calcola
 * gli stili: i token sono verificati separatamente (vedi index.css) e con audit nel browser.
 */
export const expectNoAxeViolations = async (container: Element = document.body): Promise<void> => {
  const results = await axe.run(container, {
    runOnly: {
      type: 'tag',
      values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa', 'best-practice'],
    },
    rules: { 'color-contrast': { enabled: false } },
  });
  const summary = results.violations.map(
    (v) => `${v.id}: ${v.help} (${v.nodes.map((n) => n.target).join(', ')})`,
  );
  expect(summary).toEqual([]);
};
