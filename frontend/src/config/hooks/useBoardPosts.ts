import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  archiveBoardPost,
  createBoardPost,
  fetchBoardPost,
  fetchBoardPosts,
  fetchTutorBoard,
  publishBoardPost,
  updateBoardPost,
  type BoardPost,
  type BoardPostForm,
  type BoardPostStatus,
  type TutorBoardFilters,
  type TutorBoardPost,
} from '@/config/api/boardPostApi';

const BOARD_KEY = ['board-posts'] as const;

export const useBoardPosts = (status: BoardPostStatus): UseQueryResult<BoardPost[]> =>
  useQuery({ queryKey: [...BOARD_KEY, 'list', status], queryFn: () => fetchBoardPosts(status) });

export const useBoardPost = (id: string): UseQueryResult<BoardPost> =>
  useQuery({ queryKey: [...BOARD_KEY, 'detail', id], queryFn: () => fetchBoardPost(id) });

export const useTutorBoard = (filters: TutorBoardFilters): UseQueryResult<TutorBoardPost[]> =>
  useQuery({ queryKey: [...BOARD_KEY, 'tutor', filters], queryFn: () => fetchTutorBoard(filters) });

const useBoardPostMutation = <V>(
  mutationFn: (vars: V) => Promise<BoardPost>,
): UseMutationResult<BoardPost, unknown, V> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    // anche in caso di conflitto elenchi e dettaglio vanno riallineati
    onSettled: () => queryClient.invalidateQueries({ queryKey: BOARD_KEY }),
  });
};

/** Crea una bozza; si riallineano anche le mansioni, che indicano il proprio annuncio. */
export const useCreateBoardPost = (): UseMutationResult<BoardPost, unknown, string> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createBoardPost,
    onSettled: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: BOARD_KEY }),
        queryClient.invalidateQueries({ queryKey: ['companies'] }),
      ]),
  });
};

export const useUpdateBoardPost = (): UseMutationResult<
  BoardPost,
  unknown,
  { id: string; payload: BoardPostForm }
> => useBoardPostMutation(updateBoardPost);

export const usePublishBoardPost = (): UseMutationResult<
  BoardPost,
  unknown,
  { id: string; version: number }
> => useBoardPostMutation(publishBoardPost);

export const useArchiveBoardPost = (): UseMutationResult<
  BoardPost,
  unknown,
  { id: string; version: number }
> => useBoardPostMutation(archiveBoardPost);
