import { defineStore } from 'pinia'
import { preferenceService } from '@/services/preferenceService'
import type { PreferenceRule, PreferenceRuleInput } from '@/types/preference'

interface PreferencesState {
  items: PreferenceRule[]
  loading: boolean
  error: string | null
}

export const usePreferencesStore = defineStore('preferences', {
  state: (): PreferencesState => ({
    items: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchForUser(userId: string) {
      this.loading = true
      this.error = null
      try {
        this.items = await preferenceService.list(userId)
      } catch {
        this.error = 'Could not load preferences. Check your connection and try again.'
      } finally {
        this.loading = false
      }
    },

    async create(input: PreferenceRuleInput) {
      const created = await preferenceService.create(input)
      this.items = [...this.items, created]
      return created
    },

    async update(id: string, input: PreferenceRuleInput) {
      const updated = await preferenceService.update(id, input)
      this.items = this.items.map((r) => (r.id === id ? updated : r))
      return updated
    },

    async remove(id: string) {
      await preferenceService.remove(id)
      this.items = this.items.filter((r) => r.id !== id)
    },
  },
})
