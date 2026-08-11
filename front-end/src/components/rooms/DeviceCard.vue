<script setup lang="ts">
import { computed } from 'vue'
import BaseCard from '@/components/common/BaseCard.vue'
import ToggleSwitch from '@/components/common/ToggleSwitch.vue'
import { deviceIcon } from '@/utils/deviceVisuals'
import { DEVICE_TYPE_LABELS, DEVICE_STATUS_PAIR, type Device } from '@/types/device'

const props = defineProps<{ device: Device }>()
const emit = defineEmits<{ toggle: [on: boolean] }>()

const icon = computed(() => deviceIcon(props.device.type))
const label = computed(() => (props.device.type ? DEVICE_TYPE_LABELS[props.device.type] : 'Device'))

const pair = computed(() => (props.device.type ? DEVICE_STATUS_PAIR[props.device.type] : (['OFF', 'ON'] as const)))
const isOn = computed({
  get: () => props.device.status === pair.value[1],
  set: (v: boolean) => emit('toggle', v),
})
</script>

<template>
  <BaseCard class="flex items-center justify-between gap-3 p-4">
    <div class="flex items-center gap-3">
      <span class="flex h-9 w-9 items-center justify-center rounded-xl bg-mist text-ink-soft">
        <component :is="icon" class="h-4 w-4" />
      </span>
      <div>
        <h4 class="text-sm font-semibold text-ink">{{ device.name }}</h4>
        <p class="text-xs text-ink-faint">{{ label }} · {{ device.status }}</p>
      </div>
    </div>
    <ToggleSwitch v-model="isOn" :label="`Toggle ${device.name}`" />
  </BaseCard>
</template>
