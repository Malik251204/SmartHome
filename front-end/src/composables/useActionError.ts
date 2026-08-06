import { ref } from 'vue'

// backend/actual is missing several endpoints this app calls (writes on
// Rooms/Sensors, all of Users, Preferences, /readings). This wraps a call
// to one of them, catches the failure, and exposes a message for the
// modal/dialog to display instead of throwing.
export const NOT_AVAILABLE = 'Not available yet \u2014 backend/actual doesn\u2019t support this action yet.'

export function useActionError() {
  const error = ref<string | null>(null)

  async function run<T>(fn: () => Promise<T>, message = NOT_AVAILABLE): Promise<T | undefined> {
    error.value = null
    try {
      return await fn()
    } catch {
      error.value = message
    }
  }

  return { error, run }
}
