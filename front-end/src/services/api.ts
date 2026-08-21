import axios from 'axios'
import router from '@/router'

// Talks to backend/actual directly (via the Vite dev proxy in
// development — see vite.config.ts).
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Attach the JWT if we have one. Reads straight from localStorage (rather
// than the Pinia store) so this file has no dependency on Pinia being
// initialized yet.
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

// A 401 here means the token is missing/expired/invalid — auth is real
// now, so this can genuinely happen (token expiry, backend restarted with
// a different JWT secret, etc.), not just a theoretical case. Clear the
// stale session and send the person back to log in, rather than leaving
// them stuck on a page where every request silently fails.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('homecontrol_auth')
      if (router.currentRoute.value.name !== 'login') {
        router.push({ name: 'login' })
      }
    }
    return Promise.reject(error)
  },
)
