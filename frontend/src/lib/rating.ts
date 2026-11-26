/** Stelle di affidabilità (1–4) o pollici di interesse al progetto (1–2) dell'azienda (US-514). */
export type RatingKind = 'stars' | 'thumbs';

/** Massimo di ogni valutazione, come nel server. */
export const RATING_MAX: Record<RatingKind, number> = { stars: 4, thumbs: 2 };
