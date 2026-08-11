import { markRaw, type Component } from 'vue'
import IconCurtains from '@/components/icons/IconCurtains.vue'
import IconBulb from '@/components/icons/IconBulb.vue'
import IconAc from '@/components/icons/IconAc.vue'
import type { DeviceType } from '@/types/device'

const icons: Record<DeviceType, Component> = {
  CURTAINS: markRaw(IconCurtains),
  LIGHT_BULB: markRaw(IconBulb),
  AC: markRaw(IconAc),
}

export function deviceIcon(type: DeviceType | null): Component {
  return type ? icons[type] : markRaw(IconAc)
}
