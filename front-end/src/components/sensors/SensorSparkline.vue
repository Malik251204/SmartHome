<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    values: number[]
    color?: 'circuit' | 'filament' | 'coolant' | 'moss'
    width?: number
    height?: number
  }>(),
  { color: 'circuit', width: 96, height: 28 },
)

const path = computed(() => {
  if (props.values.length < 2) return ''
  const min = Math.min(...props.values)
  const max = Math.max(...props.values)
  const range = max - min || 1
  const stepX = props.width / (props.values.length - 1)
  return props.values
    .map((v, i) => {
      const x = i * stepX
      const y = props.height - ((v - min) / range) * props.height
      return `${i === 0 ? 'M' : 'L'}${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
})

const strokeColor: Record<string, string> = {
  circuit: 'var(--color-circuit)',
  filament: 'var(--color-filament)',
  coolant: 'var(--color-coolant)',
  moss: 'var(--color-moss)',
}
</script>

<template>
  <svg :viewBox="`0 0 ${width} ${height}`" :width="width" :height="height" class="overflow-visible">
    <line
      v-if="!path"
      x1="0"
      :y1="height / 2"
      :x2="width"
      :y2="height / 2"
      stroke="var(--color-mist-dim)"
      stroke-width="1.5"
      stroke-dasharray="3 3"
    />
    <path
      v-else
      :d="path"
      fill="none"
      :stroke="strokeColor[color]"
      stroke-width="1.6"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </svg>
</template>
