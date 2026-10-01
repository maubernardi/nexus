import { QueryClientProvider } from '@tanstack/react-query';
import { ToastContainer } from 'react-toastify/unstyled';
import 'react-toastify/dist/ReactToastify.css';

import { queryClient } from '@/config/api/queryClient';

type MainProviderProps = {
  children: React.ReactNode;
};

export const MainProvider = ({ children }: MainProviderProps) => (
  <QueryClientProvider client={queryClient}>
    {children}
    <ToastContainer position="top-center" role="status" />
  </QueryClientProvider>
);
