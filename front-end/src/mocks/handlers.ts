import { http, HttpResponse } from 'msw'
import { v4 as uuid } from 'uuid'
import { db } from './db'
import type { User } from '@/types/user'

// Auth is the only thing left mocked (see project notes — the real backend
// has no login/signup at all yet). Every other request — sensors, users,
// commands, readings, preferences — is left unhandled here on purpose:
// onUnhandledRequest: 'bypass' (see mocks/browser.ts) means those requests
// just fall through to the real network, which the Vite dev proxy then
// forwards to whichever backend VITE_API_PROXY_TARGET points at.

const delay = () => new Promise((resolve) => setTimeout(resolve, 250))

export const handlers = [
  http.post('/api/auth/login', async ({ request }) => {
    await delay()
    const { email, password } = (await request.json()) as { email: string; password: string }
    const expected = db.credentials[email]
    const user = db.users.find((u) => u.email === email)
    if (!expected || !user || expected !== password) {
      return HttpResponse.json({ message: 'Invalid email or password' }, { status: 401 })
    }
    return HttpResponse.json({ token: uuid(), user })
  }),

  http.post('/api/auth/signup', async ({ request }) => {
    await delay()
    const { name, email, phoneNumber, password } = (await request.json()) as {
      name: string
      email: string
      phoneNumber: string
      password: string
    }
    if (db.users.some((u) => u.email === email)) {
      return HttpResponse.json({ message: 'Email already in use' }, { status: 409 })
    }
    const user: User = {
      id: uuid(),
      name,
      email,
      phoneNumber,
      role: 'classic_user',
      roomIds: [],
      rooms: [],
      createdAt: new Date().toISOString(),
    }
    db.users = [...db.users, user]
    db.credentials = { ...db.credentials, [email]: password }
    db.saveUsers()
    db.saveCredentials()
    return HttpResponse.json({ token: uuid(), user }, { status: 201 })
  }),
]
