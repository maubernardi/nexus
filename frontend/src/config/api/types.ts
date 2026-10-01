/** Formato errori uniforme restituito dal backend. */
export type ApiError = {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  /** Codice stabile per i casi gestiti in modo dedicato (es. `USER_NOT_ENABLED`). */
  code?: string;
  fieldErrors?: { field: string; message: string }[];
};
