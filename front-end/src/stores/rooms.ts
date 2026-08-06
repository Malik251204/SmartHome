import { defineStore } from 'pinia'
import { roomService } from '@/services/roomService'
import type { Room, RoomDetail, RoomInput } from '@/types/room'

interface RoomsState {
  items: Room[]
  loading: boolean
  error: string | null
}

export const useRoomsStore = defineStore('rooms', {
  state: (): RoomsState => ({
    items: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchAll() {
      this.loading = true
      this.error = null
      try {
        this.items = await roomService.list()
      } catch {
        this.error = 'Could not load rooms. Check your connection and try again.'
      } finally {
        this.loading = false
      }
    },

    async getDetail(id: string): Promise<RoomDetail> {
      return roomService.get(id)
    },

    async create(input: RoomInput) {
      const created = await roomService.create(input)
      this.items = [...this.items, created]
      return created
    },

    async rename(id: string, input: RoomInput) {
      const updated = await roomService.update(id, input)
      this.items = this.items.map((r) => (r.id === id ? updated : r))
      return updated
    },

    // Backend unassigns everything pointing here rather than blocking or
    // cascading — sensors/users just lose this room, they're never deleted.
    async remove(id: string) {
      await roomService.remove(id)
      this.items = this.items.filter((r) => r.id !== id)
    },
  },
})
