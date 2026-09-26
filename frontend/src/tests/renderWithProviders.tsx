import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, type RenderResult } from '@testing-library/react';
import { createMemoryRouter, RouterProvider, type RouteObject } from 'react-router';

type RenderRoutesOptions = {
  routes: RouteObject[];
  initialPath?: string;
};

export const createTestQueryClient = (): QueryClient =>
  new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });

export const renderRoutes = ({
  routes,
  initialPath = '/',
}: RenderRoutesOptions): RenderResult & { router: ReturnType<typeof createMemoryRouter> } => {
  const router = createMemoryRouter(routes, { initialEntries: [initialPath] });
  const result = render(
    <QueryClientProvider client={createTestQueryClient()}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  );
  return { ...result, router };
};

export const renderWithProviders = (ui: React.ReactElement): RenderResult =>
  renderRoutes({ routes: [{ path: '*', element: ui }] });
