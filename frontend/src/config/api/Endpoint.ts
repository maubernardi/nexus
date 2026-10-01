export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

export const ENDPOINTS = {
  currentUser: '/me',
  myProjects: '/me/projects',
  tickets: '/tickets',
  ticketQueue: '/tickets/queue',
  ticketsAssignedToMe: '/tickets/assigned-to-me',
  projects: '/reference/projects',
  companies: '/companies',
  beneficiaries: '/beneficiaries',
  zones: '/reference/zones',
  jobCategories: '/reference/job-categories',
} as const;
