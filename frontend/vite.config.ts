import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:9003',
        changeOrigin: true,
        rewrite: (path) =>
          path.replace(/^\/api/, '/project-realization-service'),
      },
      '/auth-api': {
        target: 'http://localhost:9003',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/auth-api/, '/stakeholder-service'),
      },
    },
  },
})
