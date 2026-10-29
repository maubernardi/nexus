/** Calcoli della griglia oraria nel modulo (US-1002): gli stessi del server, per il totale mentre si compila. */

export type ScheduleDayValues = { first: boolean; second: boolean; hours: string };

export type SlotValues = {
  firstSlotStart: string;
  firstSlotEnd: string;
  secondSlotStart: string;
  secondSlotEnd: string;
};

/** I sette giorni della griglia, da lunedì (1) a domenica (7). */
export const DAYS = [1, 2, 3, 4, 5, 6, 7] as const;

/** Minuti dalla mezzanotte di un orario "hh:mm" (anche "hh:mm:ss"); null se vuoto o non valido. */
export const timeToMinutes = (time: string): number | null => {
  const match = /^(\d{2}):(\d{2})/.exec(time);
  return match ? Number(match[1]) * 60 + Number(match[2]) : null;
};

const duration = (start: string, end: string): number => {
  const from = timeToMinutes(start);
  const to = timeToMinutes(end);
  return from === null || to === null || to <= from ? 0 : to - from;
};

/** Durata delle fasce scelte nel giorno: il massimo delle ore che il giorno può indicare. */
export const slotMinutes = (slots: SlotValues, day: ScheduleDayValues): number =>
  (day.first ? duration(slots.firstSlotStart, slots.firstSlotEnd) : 0) +
  (day.second ? duration(slots.secondSlotStart, slots.secondSlotEnd) : 0);

/** Ore scritte nel modulo in minuti: "3", "2,5", "2.75"; null se vuote, NaN se non sono quarti d'ora. */
export const parseHours = (text: string): number | null => {
  const value = text.trim().replace(',', '.');
  if (value === '') return null;
  if (!/^\d{1,2}(\.\d{1,2})?$/.test(value)) return Number.NaN;
  const minutes = Math.round(Number(value) * 60);
  return minutes > 0 && minutes % 15 === 0 ? minutes : Number.NaN;
};

/** Minuti del giorno: le ore indicate oppure le fasce scelte intere (0 se il giorno non ha fasce). */
export const dayMinutes = (slots: SlotValues, day: ScheduleDayValues): number => {
  if (!day.first && !day.second) return 0;
  const hours = parseHours(day.hours);
  return hours !== null && !Number.isNaN(hours) ? hours : slotMinutes(slots, day);
};

/** Totale settimanale in minuti; null se nessun giorno ha una fascia. */
export const weeklyMinutes = (slots: SlotValues, days: ScheduleDayValues[]): number | null =>
  days.some((d) => d.first || d.second) ? days.reduce((sum, d) => sum + dayMinutes(slots, d), 0) : null;

const hoursNumber = new Intl.NumberFormat('it-IT', { maximumFractionDigits: 2 });

/** Ore da minuti per i campi del modulo: 180 → "3", 150 → "2,5". */
export const minutesToHoursText = (minutes: number): string => hoursNumber.format(minutes / 60);
