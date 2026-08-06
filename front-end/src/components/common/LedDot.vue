<script setup lang="ts">
withDefaults(
  defineProps<{
    on?: boolean
    color?: 'circuit' | 'filament' | 'coolant' | 'moss'
  }>(),
  { on: false, color: 'circuit' },
)

const glow: Record<string, string> = {
  circuit: 'bg-circuit shadow-[0_0_0_3px_var(--color-circuit-tint)]',
  filament: 'bg-filament shadow-[0_0_0_3px_var(--color-filament-tint)]',
  coolant: 'bg-coolant shadow-[0_0_0_3px_var(--color-coolant-tint)]',
  moss: 'bg-moss shadow-[0_0_0_3px_var(--color-moss-tint)]',
}
</script>

<template>
  <span
    class="inline-block h-2 w-2 rounded-full transition-all duration-300"
    :class="on ? glow[color] : 'bg-ink-faint/50'"
    aria-hidden="true"
  />
</template>
