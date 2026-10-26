import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  // VITE_API_BASE_URL comes from the environment (e.g. Vercel project settings) or a .env file.
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [react()],
    define: {
      __API_BASE_URL__: JSON.stringify(env.VITE_API_BASE_URL || 'http://localhost:8080')
    },
    server: {
      port: 5173,
      open: true
    }
  };
});
