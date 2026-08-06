<script setup lang="ts">
import SliderInput from '@/components/common/SliderInput.vue'
import type { ACData, ACMode } from '@/types/sensor'

const model = defineModel<ACData>({ required: true })

const modes: { value: ACMode; label: string }[] = [
  { value: 'OFF', label: 'Off' },
  { value: 'HEAT', label: 'Heat' },
  { value: 'COOL', label: 'Cool' },
]
</script>

<template>
  <div class="space-y-4">
    <div>
      <span class="mb-1.5 block text-sm font-medium text-ink-soft">Mode</span>
      <div class="inline-flex rounded-lg border border-mist-dim bg-paper p-1">
        <button
          v-for="m in modes"
          :key="m.value"
          type="button"
          class="rounded-md px-3 py-1.5 text-sm font-display font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50"
          :class="model.mode === m.value ? 'bg-coolant text-white' : 'text-ink-soft hover:bg-mist'"
          @click="model.mode = m.value"
        >
          {{ m.label }}
        </button>
      </div>
    </div>
    <SliderInput
      v-model="model.targetTemp"
      label="Target temperature"
      unit="°C"
      :min="16"
      :max="30"
      color="coolant"
      :disabled="model.mode === 'OFF'"
    />
  </div>
</template>
