import axios from 'axios'

// This is the ONE line that changes when the real backend is ready:
// set VITE_API_BASE_URL in .env to your teammate's Spring Boot URL
// (e.g. http://localhost:8080/api) and set VITE_USE_MOCKS=false.
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Attach the mock/JWT token if we have one. Reads straight from
// localStorage (rather than the Pinia store) so this file has no
// dependency on Pinia being initialized yet.
api.interceptors.request.use((config) => {
  const raw = localStorage.getItem('homecontrol_auth')
  if (raw) {
    try {
      const { token } = JSON.parse(raw) as { token?: string }
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch {
      // ignore malformed session
    }
  }
  return config
})
