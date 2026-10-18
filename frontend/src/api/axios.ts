import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' }
});

const SESSION_KEYS = ['sa_session', 'sa_userId', 'sa_role', 'sa_fullName'];

const clearSessionAndRedirect = () => {
  SESSION_KEYS.forEach((k) => localStorage.removeItem(k));
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login';
  }
};

api.interceptors.request.use((config) => {
  const userId = localStorage.getItem('sa_userId');
  const role = localStorage.getItem('sa_role');
  const url = config.url ?? '';
  const isAuthEndpoint = url.includes('/api/auth/');

  // If the session was cleared (e.g. localStorage wiped from DevTools), stop
  // sending unauthenticated calls and force the user back to the login page.
  if (!userId && !isAuthEndpoint) {
    clearSessionAndRedirect();
    return Promise.reject(new axios.Cancel('No active session'));
  }

  if (userId) config.headers['X-User-Id'] = userId;
  if (role) config.headers['X-User-Role'] = role;
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (err) => {
    const status = err.response?.status;
    // 401 Unauthorized → the session is no longer valid; log the user out.
    if (status === 401) {
      clearSessionAndRedirect();
    }
    return Promise.reject(err);
  }
);

export default api;

export const extractMessage = (err: any): string => {
  const data = err?.response?.data;
  if (!data) return err?.message ?? 'Something went wrong';
  if (typeof data === 'string') return data;
  if (data.message) return data.message;
  return Object.values(data).join(', ');
};
