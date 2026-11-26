/** Livelli della valutazione di un'azienda (US-1104): 1 Pessima … 5 Ottima. */
export const REVIEW_LEVELS = [1, 2, 3, 4, 5] as const;
export type ReviewLevel = (typeof REVIEW_LEVELS)[number];

/** Faccina di una media: quella del numero intero più vicino (4,33 → 4; 4,67 → 5), sempre tra 1 e 5. */
export const reviewLevel = (average: number): ReviewLevel =>
  Math.min(5, Math.max(1, Math.round(average))) as ReviewLevel;

const oneDecimal = new Intl.NumberFormat('it-IT', { minimumFractionDigits: 1, maximumFractionDigits: 1 });

/** Media con un decimale, all'italiana: «4,3». */
export const formatAverage = (average: number): string => oneDecimal.format(average);

/** Media dei tre voti del report; null finché ne manca uno. */
export const reviewAverage = (votes: readonly (number | null)[]): number | null =>
  votes.every((v): v is number => v !== null) ? votes.reduce((a, b) => a + b, 0) / votes.length : null;
