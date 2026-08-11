import { defineStore } from 'pinia'
import { sensorService } from '@/services/sensorService'
import type { Sensor } from '@/types/sensor'

interface SensorsState {
  items: Sensor[]
  loading: boolean
  error: string | null
}

let pollTimer: ReturnType<typeof setInterval> | null = null

// Read-only — sensors have no write path (see sensorService.ts).
export const useSensorsStore = defineStore('sensors', {
  state: (): SensorsState => ({
    items: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchAll() {
      this.loading = true
      this.error = null
      try {
        this.items = await sensorService.list()
      } catch {
        this.error = 'Could not load sensors. Check your connection and try again.'
      } finally {
        this.loading = false
      }
    },

    async refreshSilently() {
      try {
        this.items = await sensorService.list()
        this.error = null
      } catch {
        // Stay quiet on a background miss — the next tick just tries again.
      }
    },

    startPolling(intervalMs = 5000) {
      this.stopPolling()
      pollTimer = setInterval(() => {
        if (document.visibilityState === 'visible') {
          this.refreshSilently()
        }
      }, intervalMs)
    },

    stopPolling() {
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
      }
    },
  },
})
