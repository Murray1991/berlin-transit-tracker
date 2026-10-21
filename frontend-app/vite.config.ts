import react from '@vitejs/plugin-react'
import {defineConfig} from 'vite'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        host: true,
        port: 5173,
        proxy: {
            '/api/': {
                target: 'http://api-gateway:8080',
                changeOrigin: true
            },
            '/ws/': {
                target: 'ws://api-gateway:8080',
                ws: true
            }
        }
    },
})
