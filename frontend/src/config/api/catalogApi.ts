import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

/** Cataloghi gestiti dall'ADMIN (US-106). */
export type CatalogKind = 'zones' | 'job-categories';

export type CatalogItem = {
  id: string;
  /** Ricavato dal nome alla creazione; non cambia rinominando. */
  code: string;
  name: string;
  active: boolean;
};

export const fetchCatalog = async (kind: CatalogKind): Promise<CatalogItem[]> => {
  const { data } = await axiosInstance.get<CatalogItem[]>(`${ENDPOINTS.catalogs}/${kind}`);
  return data;
};

export const createCatalogItem = async ({
  kind,
  name,
}: {
  kind: CatalogKind;
  name: string;
}): Promise<CatalogItem> => {
  const { data } = await axiosInstance.post<CatalogItem>(`${ENDPOINTS.catalogs}/${kind}`, { name });
  return data;
};

export const renameCatalogItem = async ({
  kind,
  id,
  name,
}: {
  kind: CatalogKind;
  id: string;
  name: string;
}): Promise<CatalogItem> => {
  const { data } = await axiosInstance.put<CatalogItem>(`${ENDPOINTS.catalogs}/${kind}/${id}`, { name });
  return data;
};

export const setCatalogItemActive = async ({
  kind,
  id,
  active,
}: {
  kind: CatalogKind;
  id: string;
  active: boolean;
}): Promise<CatalogItem> => {
  const { data } = await axiosInstance.post<CatalogItem>(
    `${ENDPOINTS.catalogs}/${kind}/${id}/${active ? 'activate' : 'deactivate'}`,
  );
  return data;
};
