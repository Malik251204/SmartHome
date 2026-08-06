<script setup lang="ts">
import ToggleSwitch from '@/components/common/ToggleSwitch.vue'
import type { CurtainsData } from '@/types/sensor'

const model = defineModel<CurtainsData>({ required: true })
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <span class="block text-sm font-medium text-ink-soft">Open</span>
        <span class="block text-xs text-ink-faint">
          The real curtain motor only supports fully open or fully closed.
        </span>
      </div>
      <ToggleSwitch v-model="model.isOpen" color="moss" label="Curtains open" />
    </div>

    <p v-if="model.roomLightLux !== undefined" class="text-xs text-ink-faint">
      Last reported light level:
      <span class="font-mono text-ink">{{ model.roomLightLux }} lux</span>
      (read-only — reported by the device, not something you set)
    </p>
  </div>
</template>
