<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    label: string
    type?: string
    placeholder?: string
    error?: string
    required?: boolean
  }>(),
  { type: 'text', required: false },
)

const model = defineModel<string>({ default: '' })
const inputId = `field-${props.label.toLowerCase().replace(/\s+/g, '-')}`
</script>

<template>
  <label :for="inputId" class="block">
    <span class="mb-1.5 block text-sm font-medium text-ink-soft">
      {{ label }}<span v-if="required" class="text-alert"> *</span>
    </span>
    <input
      :id="inputId"
      v-model="model"
      :type="type"
      :placeholder="placeholder"
      :required="required"
      class="w-full rounded-lg border bg-paper px-3 py-2 text-sm text-ink placeholder:text-ink-faint focus:border-circuit"
      :class="error ? 'border-alert' : 'border-mist-dim'"
    />
    <span v-if="error" class="mt-1 block text-xs text-alert">{{ error }}</span>
  </label>
</template>
