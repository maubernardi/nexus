import { createBrowserRouter, RouterProvider } from 'react-router';

import { routes } from '@/config/routing/Routes';
import { MainProvider } from '@/MainProvider';

const router = createBrowserRouter(routes);

export const App = () => (
  <MainProvider>
    <RouterProvider router={router} />
  </MainProvider>
);
