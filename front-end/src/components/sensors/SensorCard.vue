<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import BaseCard from '@/components/common/BaseCard.vue'
import BaseBadge from '@/components/common/BaseBadge.vue'
import LedDot from '@/components/common/LedDot.vue'
import SensorSparkline from './SensorSparkline.vue'
import { sensorVisual } from '@/utils/sensorVisuals'
import { sensorService } from '@/services/sensorService'
import { SENSOR_TYPE_LABELS, formatSensorValue, type Sensor, type SensorReading } from '@/types/sensor'

const props = defineProps<{ sensor: Sensor }>()

const visual = computed(() => sensorVisual(props.sensor.type))
const readout = computed(() => formatSensorValue(props.sensor.type, props.sensor.data))

// History sparkline — fetched and refreshed independently per card.
// backend/actual has no /readings route yet (only backend/mock does, and
// backend/actual doesn't proxy to it — see sensorService.ts), so this
// currently 404s every cycle and the sparkline just shows its empty
// placeholder. Left wired for when that route exists.
const readings = ref<SensorReading[]>([])
let readingsTimer: ReturnType<typeof setInterval> | null = null

function numericValue(reading: SensorReading): number {
  const d = reading.data as unknown as Record<string, number>
  return d.lux ?? d.celsius ?? d.count ?? 0
}

const sparklineValues = computed(() => readings.value.map(numericValue))

async function loadReadings() {
  try {
    readings.value = await sensorService.readings(props.sensor.id, props.sensor.type, 12)
  } catch {
    // Route doesn't exist on backend/actual yet — every retry would just
    // be another guaranteed 404 in the network console. Stop polling
    // rather than hammering it every 5s; a page reload tries again once,
    // in case the route has since been added.
    if (readingsTimer) {
      clearInterval(readingsTimer)
      readingsTimer = null
    }
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
    <div class="flex items-center gap-3">
      <span
        class="flex h-10 w-10 items-center justify-center rounded-xl"
        :class="{
          'bg-filament-tint text-[#8a5a17]': sensor.type === 'LUX',
          'bg-coolant-tint text-coolant': sensor.type === 'TEMPERATURE',
          'bg-moss-tint text-moss': sensor.type === 'OCCUPANCY',
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

    <div class="flex items-center gap-2 rounded-lg bg-mist px-3 py-2.5">
      <LedDot :on="sensor.status === 'on'" :color="visual.color" />
      <span class="font-mono text-sm text-ink">{{ readout }}</span>
    </div>

    <div class="flex items-center justify-between">
      <span class="text-xs text-ink-faint">History</span>
      <SensorSparkline :values="sparklineValues" :color="visual.color" />
    </div>

    <div class="border-t border-mist-dim pt-3">
      <span class="text-xs text-ink-faint">#{{ sensor.id }}</span>
    </div>
  </BaseCard>
</template>
