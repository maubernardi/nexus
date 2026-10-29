import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import type {
  AtecoCode,
  LegalRepresentative,
  LegalRepresentativeForm,
} from '@/config/api/companyLegalDataApi';
import {
  addInternshipAtecoCode,
  addInternshipLegalRepresentative,
  downloadInternshipDocument,
  fetchInternship,
  saveInternship,
  type Internship,
  type InternshipDocumentKind,
  type InternshipForm,
} from '@/config/api/internshipApi';
import { TICKETS_KEY } from '@/config/hooks/useTickets';

// sotto la chiave delle segnalazioni: si riallinea anche dopo le azioni sulla segnalazione (es. appuntamento fallito)
const internshipKey = (ticketId: string) => [...TICKETS_KEY, 'internship', ticketId] as const;

export const useInternship = (ticketId: string): UseQueryResult<Internship> =>
  useQuery({ queryKey: internshipKey(ticketId), queryFn: () => fetchInternship(ticketId), retry: false });

/** Salvataggio della scheda: anche in caso di conflitto si riallinea alla versione salvata (e alla cronologia). */
export const useSaveInternship = (): UseMutationResult<
  Internship,
  unknown,
  { ticketId: string; payload: InternshipForm }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: saveInternship,
    onSettled: () => queryClient.invalidateQueries({ queryKey: TICKETS_KEY }),
  });
};

/** Download di un documento: il server lo registra in cronologia, che quindi si aggiorna. */
export const useDownloadInternshipDocument = (): UseMutationResult<
  Blob,
  unknown,
  { ticketId: string; kind: InternshipDocumentKind }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: downloadInternshipDocument,
    onSettled: () => queryClient.invalidateQueries({ queryKey: [...TICKETS_KEY, 'history'] }),
  });
};

export const useAddInternshipLegalRepresentative = (): UseMutationResult<
  LegalRepresentative,
  unknown,
  { ticketId: string; payload: LegalRepresentativeForm }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addInternshipLegalRepresentative,
    onSettled: (_r, _e, vars) => queryClient.invalidateQueries({ queryKey: internshipKey(vars.ticketId) }),
  });
};

export const useAddInternshipAtecoCode = (): UseMutationResult<
  AtecoCode,
  unknown,
  { ticketId: string; code: string; description: string | null }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addInternshipAtecoCode,
    onSettled: (_r, _e, vars) => queryClient.invalidateQueries({ queryKey: internshipKey(vars.ticketId) }),
  });
};
