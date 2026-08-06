<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import BaseCard from '@/components/common/BaseCard.vue'
import BaseBadge from '@/components/common/BaseBadge.vue'
import LedDot from '@/components/common/LedDot.vue'
import ToggleSwitch from '@/components/common/ToggleSwitch.vue'
import SensorSparkline from './SensorSparkline.vue'
import IconPencil from '@/components/icons/IconPencil.vue'
import IconTrash from '@/components/icons/IconTrash.vue'
import { sensorVisual } from '@/utils/sensorVisuals'
import { sensorService } from '@/services/sensorService'
import {
  SENSOR_TYPE_LABELS,
  type Sensor,
  type SensorReading,
  type CurtainsData,
  type LightBulbData,
  type ACData,
} from '@/types/sensor'

const props = defineProps<{ sensor: Sensor }>()
const emit = defineEmits<{
  edit: []
  delete: []
  'toggle-enabled': [on: boolean]
  'toggle-curtains': [open: boolean]
  'toggle-bulb': [on: boolean]
}>()

const visual = computed(() => sensorVisual(props.sensor.type))

const readout = computed(() => {
  const s = props.sensor
  if (s.type === 'CURTAINS') {
    const d = s.data as CurtainsData
    const lux = d.roomLightLux !== undefined ? ` \u00b7 ${d.roomLightLux} lux` : ''
    return `${d.isOpen ? 'Open' : 'Closed'}${lux}`
  }
  if (s.type === 'LIGHT_BULB') {
    const d = s.data as LightBulbData
    return `${d.isOn ? 'On' : 'Off'} \u00b7 ${d.brightness}%`
  }
  const d = s.data as ACData
  return d.mode === 'OFF' ? 'Off' : `${d.mode} \u00b7 ${d.targetTemp}\u00b0C`
})

// Top toggle: "is this device enabled" — plain metadata, a direct,
// synchronous PUT. Real actuation happens through the type-specific quick
// action below, also a direct PUT — there's no queue anymore.
const enabledOn = computed({
  get: () => props.sensor.status === 'on',
  set: (v: boolean) => emit('toggle-enabled', v),
})

const curtainsOpen = computed({
  get: () => (props.sensor.data as CurtainsData).isOpen,
  set: (v: boolean) => emit('toggle-curtains', v),
})
const bulbOn = computed({
  get: () => (props.sensor.data as LightBulbData).isOn,
  set: (v: boolean) => emit('toggle-bulb', v),
})

// History sparkline — fetched and refreshed independently per card.
// backend/actual has no /readings route yet (only backend/mock does, and
// backend/actual doesn't proxy to it — see sensorService.ts), so this
// currently 404s every cycle and the sparkline just shows its empty
// placeholder. Left wired for when that route exists.
const readings = ref<SensorReading[]>([])
let readingsTimer: ReturnType<typeof setInterval> | null = null

function numericValue(reading: SensorReading): number {
  const d = reading.data
  if ('roomLightLux' in d && typeof d.roomLightLux === 'number') return d.roomLightLux
  if ('brightness' in d) return d.brightness
  if ('targetTemp' in d) return d.targetTemp
  return 0
}

const sparklineValues = computed(() => readings.value.map(numericValue))

async function loadReadings() {
  try {
    readings.value = await sensorService.readings(props.sensor.id, props.sensor.type, 12)
  } catch {
    // Sparkline is decorative — a failed background fetch just keeps the
    // last known trend on screen rather than surfacing an error.
  }
}

onMounted(() => {
  loadReadings()
  readingsTimer = setInterval(loadReadings, 5000)
})
onUnmounted(() => {
  if (readingsTimer) clearInterval(readingsTimer)
})
</script>

<template>
  <BaseCard class="flex flex-col gap-4 p-5">
    <div class="flex items-start justify-between">
      <div class="flex items-center gap-3">
        <span
          class="flex h-10 w-10 items-center justify-center rounded-xl"
          :class="{
            'bg-moss-tint text-moss': sensor.type === 'CURTAINS',
            'bg-filament-tint text-[#8a5a17]': sensor.type === 'LIGHT_BULB',
            'bg-coolant-tint text-coolant': sensor.type === 'AC',
          }"
        >
          <component :is="visual.icon" class="h-5 w-5" />
        </span>
        <div>
          <h3 class="font-display text-sm font-semibold text-ink">{{ sensor.name }}</h3>
          <div class="mt-1 flex flex-wrap items-center gap-1.5">
            <BaseBadge :tone="visual.color">
              {{ SENSOR_TYPE_LABELS[sensor.type] }}
            </BaseBadge>
            <span v-if="sensor.roomName" class="text-xs text-ink-faint">{{ sensor.roomName }}</span>
          </div>
        </div>
      </div>
      <ToggleSwitch v-model="enabledOn" :color="visual.color" label="Device enabled" />
    </div>

    <div class="flex items-center gap-2 rounded-lg bg-mist px-3 py-2.5">
      <LedDot :on="sensor.status === 'on'" :color="visual.color" />
      <span class="font-mono text-sm text-ink">{{ readout }}</span>
    </div>

    <div v-if="sensor.type === 'CURTAINS'" class="flex items-center justify-between">
      <span class="text-sm font-medium text-ink-soft">Curtains</span>
      <ToggleSwitch v-model="curtainsOpen" color="moss" label="Open curtains" />
    </div>
    <div v-else-if="sensor.type === 'LIGHT_BULB'" class="flex items-center justify-between">
      <span class="text-sm font-medium text-ink-soft">Power</span>
      <ToggleSwitch v-model="bulbOn" color="filament" label="Bulb power" />
    </div>

    <div class="flex items-center justify-between">
      <span class="text-xs text-ink-faint">History</span>
      <SensorSparkline :values="sparklineValues" :color="visual.color" />
    </div>

    <div class="flex items-center justify-between border-t border-mist-dim pt-3">
      <span class="text-xs text-ink-faint">#{{ sensor.id }}</span>
      <div class="flex gap-1">
        <button
          type="button"
          class="rounded-md p-1.5 text-ink-faint hover:bg-mist hover:text-ink"
          aria-label="Edit sensor"
          @click="emit('edit')"
        >
          <IconPencil class="h-4 w-4" />
        </button>
        <button
          type="button"
          class="rounded-md p-1.5 text-ink-faint hover:bg-alert-tint hover:text-alert"
          aria-label="Delete sensor"
          @click="emit('delete')"
        >
          <IconTrash class="h-4 w-4" />
        </button>
      </div>
    </div>
  </BaseCard>
</template>
