import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  assetsInclude: ['**/*.ttf'],
  server: {
    port: 5173,
    proxy: {
      '/api/v1/search': {
        target: 'http://localhost:8000',
        changeOrigin: true,
      },
      '/api/v1/dokumenti': {
        target: 'http://localhost:9070',
        changeOrigin: true,
      },
      '/api/dokument-tag': {
        target: 'http://localhost:9070',
        changeOrigin: true,
      },
      '/api/tag': {
        target: 'http://localhost:9070',
        changeOrigin: true,
      },
      '/api/tip-dokumenta': {
        target: 'http://localhost:9070',
        changeOrigin: true,
      },
      '/api/tip-metapodatka': {
        target: 'http://localhost:9070',
        changeOrigin: true,
      },
      '/api/metapodatak': {
        target: 'http://localhost:9070',
        changeOrigin: true,
      },
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
