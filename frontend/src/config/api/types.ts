/** Formato errori uniforme restituito dal backend. */
export type ApiError = {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: { field: string; message: string }[];
};
