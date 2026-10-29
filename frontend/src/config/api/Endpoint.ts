export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

export const ENDPOINTS = {
  currentUser: '/me',
  myProjects: '/me/projects',
  tickets: '/tickets',
  projects: '/reference/projects',
  calendar: '/calendar',
  notifications: '/notifications',
  companies: '/companies',
  jobSlots: '/job-slots',
  boardPosts: '/board-posts',
  board: '/board',
  catalogs: '/catalogs',
  beneficiaries: '/beneficiaries',
  zones: '/reference/zones',
  jobCategories: '/reference/job-categories',
  internshipTypes: '/reference/internship-types',
} as const;
