import { markRaw, type Component } from 'vue'
import IconSliders from '@/components/icons/IconSliders.vue'
import IconUsers from '@/components/icons/IconUsers.vue'
import type { SensorType } from '@/types/sensor'

export type SignalColor = 'filament' | 'coolant' | 'moss'

interface SensorVisual {
  icon: Component
  color: SignalColor
}

// No dedicated icons for these yet — reusing generic ones rather than
// adding new SVGs for a 3-type set.
const visuals: Record<SensorType, SensorVisual> = {
  LUX: { icon: markRaw(IconSliders), color: 'filament' },
  TEMPERATURE: { icon: markRaw(IconSliders), color: 'coolant' },
  OCCUPANCY: { icon: markRaw(IconUsers), color: 'moss' },
}

export function sensorVisual(type: SensorType): SensorVisual {
  return visuals[type]
}
