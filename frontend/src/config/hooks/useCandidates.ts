import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createCandidate,
  fetchMyCandidates,
  type Candidate,
  type CandidateCreate,
  type CandidateSummary,
} from '@/config/api/candidateApi';

export const MY_CANDIDATES_KEY = ['candidates', 'mine'] as const;

export const useMyCandidates = (): UseQueryResult<CandidateSummary[]> =>
  useQuery({ queryKey: MY_CANDIDATES_KEY, queryFn: fetchMyCandidates });

export const useCreateCandidate = (): UseMutationResult<Candidate, unknown, CandidateCreate> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createCandidate,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: MY_CANDIDATES_KEY }),
  });
};
