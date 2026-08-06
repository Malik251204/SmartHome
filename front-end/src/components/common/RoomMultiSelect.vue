<script setup lang="ts">
import type { Room } from '@/types/room'

defineProps<{ label: string; rooms: Room[] }>()
const model = defineModel<string[]>({ required: true })

function toggle(id: string) {
  model.value = model.value.includes(id) ? model.value.filter((r) => r !== id) : [...model.value, id]
}
</script>

<template>
  <div>
    <span class="mb-1.5 block text-sm font-medium text-ink-soft">{{ label }}</span>
    <div
      v-if="rooms.length === 0"
      class="rounded-lg border border-dashed border-mist-dim px-3 py-3 text-sm text-ink-faint"
    >
      No rooms yet.
    </div>
    <div v-else class="max-h-40 space-y-1 overflow-y-auto rounded-lg border border-mist-dim px-3 py-2">
      <label
        v-for="room in rooms"
        :key="room.id"
        class="flex cursor-pointer items-center gap-2.5 rounded-md px-1.5 py-1.5 text-sm text-ink hover:bg-mist"
      >
        <input
          type="checkbox"
          class="h-4 w-4 rounded border-mist-dim text-circuit focus:ring-circuit"
          :checked="model.includes(room.id)"
          @change="toggle(room.id)"
        />
        {{ room.name }}
      </label>
    </div>
  </div>
</template>
