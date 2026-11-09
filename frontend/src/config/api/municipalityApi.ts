import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

/** Versione di un comune (US-114): `validTo` assente = ancora valido; altrimenti vale fino al giorno prima. */
export type Municipality = {
  id: string;
  name: string;
  istatCode: string;
  cadastralCode: string;
  provinceName: string;
  provinceAbbreviation: string;
  /** Data ISO (aaaa-mm-gg). */
  validFrom: string;
  validTo?: string;
};

export type MunicipalityStatus = {
  municipalities: number;
  provinces: number;
  lastImport?: { validFrom: string; importedAt: string; importedBy?: string };
};

/** Differenze per comuni o province: conteggi e primi esempi. */
export type MunicipalityChanges = {
  added: number;
  removed: number;
  changed: number;
  addedExamples: string[];
  removedExamples: string[];
  changedExamples: string[];
};

export type MunicipalityImportResult = {
  rows: number;
  municipalities: MunicipalityChanges;
  provinces: MunicipalityChanges;
};

/** Solo il caricamento del file usa questa dimensione: il file dell'ISTAT pesa circa 1 MB. */
export const ISTAT_FILE_MAX_BYTES = 6 * 1024 * 1024;

export const fetchMunicipalityStatus = async (): Promise<MunicipalityStatus> => {
  const { data } = await axiosInstance.get<MunicipalityStatus>(`${ENDPOINTS.municipalities}/status`);
  return data;
};

export const searchMunicipalities = async ({
  q,
  history = false,
}: {
  q: string;
  history?: boolean;
}): Promise<Municipality[]> => {
  const { data } = await axiosInstance.get<Municipality[]>(
    history ? `${ENDPOINTS.municipalities}/history` : ENDPOINTS.municipalities,
    { params: { q } },
  );
  return data;
};

export const previewMunicipalityImport = async (file: File): Promise<MunicipalityImportResult> => {
  const body = new FormData();
  body.append('file', file);
  const { data } = await axiosInstance.post<MunicipalityImportResult>(
    `${ENDPOINTS.municipalities}/import/preview`,
    body,
  );
  return data;
};

export const importMunicipalities = async ({
  file,
  validFrom,
}: {
  file: File;
  validFrom: string;
}): Promise<MunicipalityImportResult> => {
  const body = new FormData();
  body.append('file', file);
  body.append('validFrom', validFrom);
  const { data } = await axiosInstance.post<MunicipalityImportResult>(
    `${ENDPOINTS.municipalities}/import`,
    body,
  );
  return data;
};
