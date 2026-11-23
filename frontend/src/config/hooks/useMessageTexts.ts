import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  fetchMessageTexts,
  resetMessageText,
  updateMessageText,
  type MessageKey,
  type MessagePart,
  type MessageText,
} from '@/config/api/messageTextApi';

export const MESSAGE_TEXTS_KEY = ['message-texts'] as const;

export const useMessageTexts = (): UseQueryResult<MessageText[]> =>
  useQuery({ queryKey: MESSAGE_TEXTS_KEY, queryFn: fetchMessageTexts });

/** Il messaggio aggiornato prende il posto di quello in elenco (US-1204). */
const replaceInList = (list: MessageText[] | undefined, updated: MessageText): MessageText[] | undefined =>
  list?.map((m) => (m.key === updated.key ? updated : m));

export const useUpdateMessageText = (): UseMutationResult<
  MessageText,
  unknown,
  { key: MessageKey; part: MessagePart; text: string }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateMessageText,
    onSuccess: (updated) =>
      queryClient.setQueryData<MessageText[]>(MESSAGE_TEXTS_KEY, (list) => replaceInList(list, updated)),
  });
};

export const useResetMessageText = (): UseMutationResult<
  MessageText,
  unknown,
  { key: MessageKey; part: MessagePart }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: resetMessageText,
    onSuccess: (updated) =>
      queryClient.setQueryData<MessageText[]>(MESSAGE_TEXTS_KEY, (list) => replaceInList(list, updated)),
  });
};
