/** «Mario Rossi» → «mario.rossi»: senza accenti, spazi e apostrofi (decisione del PO, §8.8 review dello Sprint 8). */
export const suggestUsername = (firstName: string, lastName: string): string => {
  const part = (s: string): string =>
    s
      .normalize('NFD')
      .replace(/\p{M}/gu, '')
      .toLowerCase()
      .replace(/[^a-z0-9]/g, '');
  return [part(firstName), part(lastName)].filter(Boolean).join('.');
};
