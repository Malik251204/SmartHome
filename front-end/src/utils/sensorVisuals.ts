import { markRaw, type Component } from 'vue'
import IconCurtains from '@/components/icons/IconCurtains.vue'
import IconBulb from '@/components/icons/IconBulb.vue'
import IconAc from '@/components/icons/IconAc.vue'
import type { SensorType } from '@/types/sensor'

export type SignalColor = 'filament' | 'coolant' | 'moss'

interface SensorVisual {
  icon: Component
  color: SignalColor
}

const visuals: Record<SensorType, SensorVisual> = {
  CURTAINS: { icon: markRaw(IconCurtains), color: 'moss' },
  LIGHT_BULB: { icon: markRaw(IconBulb), color: 'filament' },
  AC: { icon: markRaw(IconAc), color: 'coolant' },
}

export function sensorVisual(type: SensorType): SensorVisual {
  return visuals[type]
}
