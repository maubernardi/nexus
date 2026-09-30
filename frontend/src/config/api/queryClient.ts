import { QueryClient } from '@tanstack/react-query';

/** Client unico delle query: condiviso dal provider e dagli interceptor HTTP. */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, refetchOnWindowFocus: false },
  },
});

export const CURRENT_USER_QUERY_KEY = ['currentUser'] as const;
