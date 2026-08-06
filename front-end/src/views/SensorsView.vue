<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useSensorsStore } from '@/stores/sensors'
import { useActionError } from '@/composables/useActionError'
import BaseButton from '@/components/common/BaseButton.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import LedDot from '@/components/common/LedDot.vue'
import SensorCard from '@/components/sensors/SensorCard.vue'
import SensorFormModal from '@/components/sensors/SensorFormModal.vue'
import IconPlus from '@/components/icons/IconPlus.vue'
import { SENSOR_TYPES, SENSOR_TYPE_LABELS, type Sensor, type SensorInput, type SensorType } from '@/types/sensor'

const store = useSensorsStore()

onMounted(async () => {
  await store.fetchAll()
  store.startPolling()
})

onUnmounted(() => {
  store.stopPolling()
})

const activeFilter = ref<SensorType | 'ALL'>('ALL')
const filters: { value: SensorType | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'All' },
  ...SENSOR_TYPES.map((t) => ({ value: t, label: SENSOR_TYPE_LABELS[t] })),
]

const filteredSensors = computed(() =>
  activeFilter.value === 'ALL'
    ? store.items
    : store.items.filter((s) => s.type === activeFilter.value),
)

const showForm = ref(false)
const editingSensor = ref<Sensor | null>(null)
const saving = ref(false)
const { error: formError, run: runForm } = useActionError()

function openCreate() {
  editingSensor.value = null
  formError.value = null
  showForm.value = true
}

function openEdit(sensor: Sensor) {
  editingSensor.value = sensor
  formError.value = null
  showForm.value = true
}

async function handleSubmit(input: SensorInput) {
  saving.value = true
  await runForm(() => (editingSensor.value ? store.update(editingSensor.value!.id, input) : store.create(input)))
  if (!formError.value) showForm.value = false
  saving.value = false
}

// Controlled purely by the sensor prop (see SensorCard.vue) — on failure
// the switch just stays as-is. Only catching here to avoid an unhandled
// rejection; no user-facing message for a quick toggle.
async function handleToggleEnabled(sensor: Sensor, on: boolean) {
  await store.update(sensor.id, { ...sensor, status: on ? 'on' : 'off' }).catch(() => {})
}

async function handleToggleCurtains(sensor: Sensor, open: boolean) {
  await store.update(sensor.id, { ...sensor, data: { ...sensor.data, isOpen: open } }).catch(() => {})
}

async function handleToggleBulb(sensor: Sensor, on: boolean) {
  await store.update(sensor.id, { ...sensor, data: { ...sensor.data, isOn: on } }).catch(() => {})
}

const pendingDelete = ref<Sensor | null>(null)
const deleting = ref(false)
const { error: deleteError, run: runDelete } = useActionError()

async function confirmDelete() {
  if (!pendingDelete.value) return
  deleting.value = true
  const id = pendingDelete.value.id
  await runDelete(() => store.remove(id))
  if (!deleteError.value) pendingDelete.value = null
  deleting.value = false
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-4">
      <div>
        <h1 class="font-display text-xl font-semibold text-ink">Sensors</h1>
        <p class="text-sm text-ink-soft">Monitor live device state and manage the fleet.</p>
      </div>
      <div class="flex items-center gap-3">
        <span
          class="hidden items-center gap-1.5 rounded-full bg-mist px-2.5 py-1 text-xs font-medium text-ink-soft sm:inline-flex"
        >
          <LedDot :on="true" color="circuit" />
          Live · refreshes every 5s
        </span>
        <BaseButton @click="openCreate">
          <IconPlus class="h-4 w-4" />
          Add sensor
        </BaseButton>
      </div>
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
      <p class="mt-1 text-sm text-ink-soft">Add a sensor to start monitoring and controlling it.</p>
    </div>

    <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <SensorCard
        v-for="sensor in filteredSensors"
        :key="sensor.id"
        :sensor="sensor"
        @edit="openEdit(sensor)"
        @delete="pendingDelete = sensor"
        @toggle-enabled="(on) => handleToggleEnabled(sensor, on)"
        @toggle-curtains="(open) => handleToggleCurtains(sensor, open)"
        @toggle-bulb="(on) => handleToggleBulb(sensor, on)"
      />
    </div>

    <SensorFormModal
      v-if="showForm"
      :sensor="editingSensor"
      :saving="saving"
      :error="formError"
      @submit="handleSubmit"
      @close="showForm = false"
    />

    <ConfirmDialog
      v-if="pendingDelete"
      title="Delete sensor"
      :message="`Remove &quot;${pendingDelete.name}&quot;? This can't be undone.`"
      :loading="deleting"
      :error="deleteError"
      @confirm="confirmDelete"
      @cancel="pendingDelete = null"
    />
  </div>
</template>
