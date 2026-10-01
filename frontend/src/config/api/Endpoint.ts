export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

export const ENDPOINTS = {
  currentUser: '/me',
  myProjects: '/me/projects',
  tickets: '/tickets',
  candidates: '/candidates',
  zones: '/reference/zones',
  jobCategories: '/reference/job-categories',
} as const;
