import axe from 'axe-core';
import { expect } from 'vitest';

/**
 * Esegue axe-core sulle regole WCAG 2.x A/AA. Il contrasto colori è escluso perché jsdom non calcola
 * gli stili: i token sono verificati separatamente (vedi index.css) e con audit nel browser.
 * Le voci `<option>` dei menu nativi sono escluse: nome e ruolo si controllano sul `<select>`, e nei moduli con gli
 * elenchi di paesi e lingue sono centinaia, cioè gran parte del tempo del controllo (azione A13).
 */
export const expectNoAxeViolations = async (container: Element = document.body): Promise<void> => {
  const results = await axe.run({ include: [container], exclude: [['option']] } as axe.ElementContext, {
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
