import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  esbuild: {
    pure: ['console.log'], // console.log만 제거 (warn, error 등은 유지)
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080', // Spring Boot default port
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    }
  }
})
