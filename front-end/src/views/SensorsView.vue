<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useSensorsStore } from '@/stores/sensors'
import { useAuthStore } from '@/stores/auth'
import { isAdminLike } from '@/utils/permissions'
import { roomService } from '@/services/roomService'
import LedDot from '@/components/common/LedDot.vue'
import SensorCard from '@/components/sensors/SensorCard.vue'
import { SENSOR_TYPES, SENSOR_TYPE_LABELS, type SensorType } from '@/types/sensor'

const store = useSensorsStore()
const auth = useAuthStore()
const canSeeAll = computed(() => isAdminLike(auth.role))

// Classic users only see sensors in rooms they're actually assigned to —
// same room-membership technique as RoomsView.vue/PreferencesView.vue,
// since sensors otherwise have no owner/room-visibility restriction of
// their own on the backend.
const myRoomIds = ref<Set<string>>(new Set())

onMounted(async () => {
  await store.fetchAll()
  store.startPolling()
  if (!canSeeAll.value && auth.user) {
    try {
      const allRooms = await roomService.listDetailed()
      myRoomIds.value = new Set(
        allRooms.filter((r) => r.users.some((u) => u.id === auth.user!.id)).map((r) => r.id),
      )
    } catch {
      // Falls back to showing nothing rather than everything — safer default.
    }
  }
})

onUnmounted(() => {
  store.stopPolling()
})

const activeFilter = ref<SensorType | 'ALL'>('ALL')
const filters: { value: SensorType | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'All' },
  ...SENSOR_TYPES.map((t) => ({ value: t, label: SENSOR_TYPE_LABELS[t] })),
]

const visibleSensors = computed(() =>
  canSeeAll.value ? store.items : store.items.filter((s) => s.roomId && myRoomIds.value.has(s.roomId)),
)

const filteredSensors = computed(() =>
  activeFilter.value === 'ALL'
    ? visibleSensors.value
    : visibleSensors.value.filter((s) => s.type === activeFilter.value),
)
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-4">
      <div>
        <h1 class="font-display text-xl font-semibold text-ink">Sensors</h1>
        <p class="text-sm text-ink-soft">Live ambient readings for every room.</p>
      </div>
      <span
        class="hidden items-center gap-1.5 rounded-full bg-mist px-2.5 py-1 text-xs font-medium text-ink-soft sm:inline-flex"
      >
        <LedDot :on="true" color="circuit" />
        Live · refreshes every 5s
      </span>
    </div>

    <div class="flex flex-wrap gap-2">
      <button
        v-for="f in filters"
        :key="f.value"
        type="button"
        class="rounded-full px-3.5 py-1.5 text-sm font-display font-medium transition-colors"
        :class="
          activeFilter === f.value
            ? 'bg-circuit text-white'
            : 'bg-paper text-ink-soft border border-mist-dim hover:bg-mist-dim'
        "
        @click="activeFilter = f.value"
      >
        {{ f.label }}
      </button>
    </div>

    <p v-if="store.error" class="rounded-lg bg-alert-tint px-4 py-3 text-sm text-alert">
      {{ store.error }}
    </p>

    <div v-if="store.loading" class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <div v-for="i in 3" :key="i" class="h-44 animate-pulse rounded-2xl bg-mist-dim" />
    </div>

    <div
      v-else-if="filteredSensors.length === 0"
      class="rounded-2xl border border-dashed border-mist-dim bg-paper px-6 py-16 text-center"
    >
      <p class="font-display text-sm font-medium text-ink">No sensors here yet</p>
      <p class="mt-1 text-sm text-ink-soft">Sensors are seeded per room — none exist yet.</p>
    </div>

    <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <SensorCard v-for="sensor in filteredSensors" :key="sensor.id" :sensor="sensor" />
    </div>
  </div>
</template>
