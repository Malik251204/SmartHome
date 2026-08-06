<script setup lang="ts">
import SliderInput from '@/components/common/SliderInput.vue'
import ToggleSwitch from '@/components/common/ToggleSwitch.vue'
import type { LightBulbData } from '@/types/sensor'

const model = defineModel<LightBulbData>({ required: true })
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <span class="text-sm font-medium text-ink-soft">Power</span>
      <ToggleSwitch v-model="model.isOn" color="filament" label="Bulb on" />
    </div>
    <SliderInput
      v-model="model.brightness"
      label="Brightness"
      unit="%"
      color="filament"
      :disabled="!model.isOn"
    />
  </div>
</template>
