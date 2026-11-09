/** Date ISO (aaaa-mm-gg) come arrivano dalle API, senza passare dai fusi orari. */

/** Oggi a Roma, come data ISO. */
export const todayIso = (): string =>
  new Intl.DateTimeFormat('sv-SE', { timeZone: 'Europe/Rome' }).format(new Date());

/** Data ISO → gg/mm/aaaa. */
export const formatIsoDate = (iso: string): string => {
  const [y, m, d] = iso.split('-');
  return `${d}/${m}/${y}`;
};

/** Il giorno prima di una data ISO: per le validità con la fine esclusa, l'ultimo giorno valido. */
export const dayBefore = (iso: string): string => {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(Date.UTC(y, m - 1, d - 1)).toISOString().slice(0, 10);
};
