import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// For local dev, this proxies calls to the Spring Boot backend (8080)
// so you don't need to deal with CORS.
const BACKEND = process.env.VITE_BACKEND_URL || 'http://localhost:8080'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    target: 'esnext',
    outDir: 'dist',
  },
  server: {
    // Vite default is 5173 (matches README). Keep it unless taken.
    proxy: {
      // Convenience endpoints
      '^/api/.*': {
        target: BACKEND,
        changeOrigin: true,
      },
      // FHIR CRUD proxy endpoints
      '^/(Patient|Practitioner|Organization|Immunization|ImmunizationRecommendation|Consent|AdverseEvent|Encounter|Appointment|Schedule|Slot|ServiceRequest|PractitionerRole|Location|AllergyIntolerance|Condition)(/.*)?$': {
        target: BACKEND,
        changeOrigin: true,
      },
      // Docs
      '^/(openapi|swagger)(/.*)?$': {
        target: BACKEND,
        changeOrigin: true,
      },
    },
  },
})
