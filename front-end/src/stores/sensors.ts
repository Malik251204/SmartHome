import { defineStore } from 'pinia'
import { sensorService } from '@/services/sensorService'
import type { Sensor, SensorInput } from '@/types/sensor'

interface SensorsState {
  items: Sensor[]
  loading: boolean
  error: string | null
}

let pollTimer: ReturnType<typeof setInterval> | null = null

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

    async create(input: SensorInput) {
      const created = await sensorService.create(input)
      this.items = [...this.items, created]
      return created
    },

    // Single write path — direct and synchronous, no command queue. Used
    // for both metadata edits (name/unit/status/room) and operational
    // changes (open/close, on/off, temperature) alike.
    async update(id: string, input: SensorInput) {
      const updated = await sensorService.update(id, input)
      this.items = this.items.map((s) => (s.id === id ? updated : s))
      return updated
    },

    async remove(id: string) {
      await sensorService.remove(id)
      this.items = this.items.filter((s) => s.id !== id)
    },
  },
})
