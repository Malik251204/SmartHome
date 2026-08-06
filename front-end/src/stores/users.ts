import { defineStore } from 'pinia'
import { userService } from '@/services/userService'
import type { User, UserInput } from '@/types/user'

interface UsersState {
  items: User[]
  loading: boolean
  error: string | null
}

export const useUsersStore = defineStore('users', {
  state: (): UsersState => ({
    items: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchAll() {
      this.loading = true
      this.error = null
      try {
        this.items = await userService.list()
      } catch {
        this.error = 'Could not load users. Check your connection and try again.'
      } finally {
        this.loading = false
      }
    },

    async create(input: UserInput) {
      const created = await userService.create(input)
      this.items = [...this.items, created]
      return created
    },

    async update(id: string, input: UserInput) {
      const updated = await userService.update(id, input)
      this.items = this.items.map((u) => (u.id === id ? updated : u))
      return updated
    },

    async remove(id: string) {
      await userService.remove(id)
      this.items = this.items.filter((u) => u.id !== id)
    },
  },
})
