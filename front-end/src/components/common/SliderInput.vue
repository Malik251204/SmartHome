<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    min?: number
    max?: number
    step?: number
    unit?: string
    color?: 'circuit' | 'filament' | 'coolant' | 'moss'
    disabled?: boolean
  }>(),
  { min: 0, max: 100, step: 1, unit: '', color: 'circuit', disabled: false },
)

const model = defineModel<number>({ default: 0 })

const accent: Record<string, string> = {
  circuit: 'var(--color-circuit)',
  filament: 'var(--color-filament)',
  coolant: 'var(--color-coolant)',
  moss: 'var(--color-moss)',
}
</script>

<template>
  <label class="block">
    <span class="mb-1.5 flex items-center justify-between text-sm font-medium text-ink-soft">
      <span>{{ label }}</span>
      <span class="font-mono text-ink">{{ model }}{{ unit }}</span>
    </span>
    <input
      v-model.number="model"
      type="range"
      :min="min"
      :max="max"
      :step="step"
      :disabled="disabled"
      :style="{ accentColor: accent[color] }"
      class="w-full disabled:opacity-50"
    />
  </label>
</template>
