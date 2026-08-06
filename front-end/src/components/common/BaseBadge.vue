<script setup lang="ts">
withDefaults(
  defineProps<{
    tone?: 'circuit' | 'filament' | 'coolant' | 'moss' | 'alert' | 'neutral'
  }>(),
  { tone: 'neutral' },
)

const tones: Record<string, string> = {
  circuit: 'bg-circuit-tint text-circuit-dark',
  filament: 'bg-filament-tint text-[#8a5a17]',
  coolant: 'bg-coolant-tint text-[#1d6a64]',
  moss: 'bg-moss-tint text-[#48632f]',
  alert: 'bg-alert-tint text-alert',
  neutral: 'bg-mist-dim text-ink-soft',
}
</script>

<template>
  <span
    class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium font-display"
    :class="tones[tone]"
  >
    <slot />
  </span>
</template>
