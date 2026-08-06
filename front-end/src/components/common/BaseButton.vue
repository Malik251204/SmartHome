<script setup lang="ts">
withDefaults(
  defineProps<{
    variant?: 'primary' | 'ghost' | 'danger' | 'subtle'
    size?: 'sm' | 'md'
    type?: 'button' | 'submit'
    disabled?: boolean
    loading?: boolean
  }>(),
  {
    variant: 'primary',
    size: 'md',
    type: 'button',
    disabled: false,
    loading: false,
  },
)

const base =
  'inline-flex items-center justify-center gap-2 rounded-lg font-display font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed'

const variants: Record<string, string> = {
  primary: 'bg-circuit text-white hover:bg-circuit-dark',
  ghost: 'bg-transparent text-ink-soft hover:bg-mist-dim hover:text-ink',
  danger: 'bg-transparent text-alert hover:bg-alert-tint',
  subtle: 'bg-mist-dim text-ink hover:bg-mist',
}

const sizes: Record<string, string> = {
  sm: 'text-sm px-3 py-1.5',
  md: 'text-sm px-4 py-2.5',
}
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="[base, variants[variant], sizes[size]]"
  >
    <span
      v-if="loading"
      class="h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent"
      aria-hidden="true"
    />
    <slot />
  </button>
</template>
