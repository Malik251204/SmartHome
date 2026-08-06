<script setup lang="ts">
withDefaults(
  defineProps<{
    label?: string
    color?: 'circuit' | 'filament' | 'coolant' | 'moss'
    disabled?: boolean
  }>(),
  { color: 'circuit', disabled: false },
)

const model = defineModel<boolean>({ default: false })

const trackOn: Record<string, string> = {
  circuit: 'bg-circuit',
  filament: 'bg-filament',
  coolant: 'bg-coolant',
  moss: 'bg-moss',
}

function toggle() {
  model.value = !model.value
}
</script>

<template>
  <button
    type="button"
    role="switch"
    :aria-checked="model"
    :aria-label="label"
    :disabled="disabled"
    class="relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors disabled:opacity-50"
    :class="model ? trackOn[color] : 'bg-mist-dim'"
    @click="toggle"
  >
    <span
      class="inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform"
      :class="model ? 'translate-x-6' : 'translate-x-1'"
    />
  </button>
</template>
