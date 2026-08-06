<script setup lang="ts">
const props = defineProps<{
  label: string
  options: { value: string; label: string }[]
  disabled?: boolean
}>()

const model = defineModel<string>({ default: '' })
const selectId = `field-${props.label.toLowerCase().replace(/\s+/g, '-')}`
</script>

<template>
  <label :for="selectId" class="block">
    <span class="mb-1.5 block text-sm font-medium text-ink-soft">{{ label }}</span>
    <select
      :id="selectId"
      v-model="model"
      :disabled="disabled"
      class="w-full rounded-lg border border-mist-dim bg-paper px-3 py-2 text-sm text-ink focus:border-circuit disabled:bg-mist disabled:text-ink-faint"
    >
      <option v-for="opt in options" :key="opt.value" :value="opt.value">
        {{ opt.label }}
      </option>
    </select>
  </label>
</template>
