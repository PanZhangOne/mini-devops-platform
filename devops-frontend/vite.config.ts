import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true,
        // Allow SSE long-lived connections to stay open
        configure: (proxy) => {
          proxy.on('proxyReq', (_proxyReq, req) => {
            if (req.headers['accept']?.includes('text/event-stream')) {
              // Prevent the proxy from timing out streaming connections
              req.socket.setTimeout(0)
            }
          })
        },
      },
    },
  },
})
