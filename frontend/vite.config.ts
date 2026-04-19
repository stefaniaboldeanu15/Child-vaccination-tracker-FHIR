import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const BACKEND = process.env.VITE_BACKEND_URL || 'http://localhost:8081'
const FHIR = (process.env.VITE_FHIR_URL || 'http://localhost:8080').replace(/\/fhir\/?$/, '')

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
    server: {
        proxy: {
            '^/api/.*': {
                target: BACKEND,
                changeOrigin: true,
            },
            '^/fhir(?:/.*)?$': {
                target: FHIR,
                changeOrigin: true,
            },
        },
    },
})
