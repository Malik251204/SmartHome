<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { isAdminLike } from '@/utils/permissions'
import { roomService } from '@/services/roomService'
import { sensorService } from '@/services/sensorService'
import { userService } from '@/services/userService'
import { useActionError } from '@/composables/useActionError'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import RoleBadge from '@/components/users/RoleBadge.vue'
import SensorCard from '@/components/sensors/SensorCard.vue'
import SensorFormModal from '@/components/sensors/SensorFormModal.vue'
import IconPlus from '@/components/icons/IconPlus.vue'
import IconTrash from '@/components/icons/IconTrash.vue'
import type { RoomDetail } from '@/types/room'
import type { Sensor, SensorInput } from '@/types/sensor'
import type { User } from '@/types/user'

const route = useRoute()
const auth = useAuthStore()

const roomId = computed(() => String(route.params.id))
const canManage = computed(() => isAdminLike(auth.role))

const room = ref<RoomDetail | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

async function loadRoom() {
  try {
    room.value = await roomService.get(roomId.value)
    error.value = null
  } catch {
    error.value = 'Could not load this room. It may have been deleted.'
  } finally {
    loading.value = false
  }
}

onMounted(loadRoom)

// Refresh the room's own sensor readouts on the same cadence as the main
// Sensors page used to, so cards here stay live too.
let pollTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => {
  pollTimer = setInterval(() => {
    if (document.visibilityState === 'visible') loadRoom()
  }, 5000)
})
onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

// --- Sensors: toggle / edit / delete / create-in-room -------------------

const showSensorForm = ref(false)
const editingSensor = ref<Sensor | null>(null)
const savingSensor = ref(false)
const { error: sensorFormError, run: runSensorForm } = useActionError()
// Shared banner for the quick actions below (toggle/assign/unassign),
// which all hit the same not-yet-implemented sensor PUT.
const { error: sensorActionError, run: runSensorAction } = useActionError()

function openCreateSensor() {
  editingSensor.value = null
  sensorFormError.value = null
  showSensorForm.value = true
}
function openEditSensor(sensor: Sensor) {
  editingSensor.value = sensor
  sensorFormError.value = null
  showSensorForm.value = true
}

async function handleSensorSubmit(input: SensorInput) {
  savingSensor.value = true
  await runSensorForm(async () => {
    if (editingSensor.value) {
      await sensorService.update(editingSensor.value!.id, input)
    } else {
      await sensorService.create(input)
    }
    await loadRoom()
  })
  if (!sensorFormError.value) showSensorForm.value = false
  savingSensor.value = false
}

async function toggleSensor(sensor: Sensor, patch: Partial<SensorInput>) {
  await runSensorAction(async () => {
    await sensorService.update(sensor.id, { ...sensor, ...patch })
    await loadRoom()
  })
}

const pendingDeleteSensor = ref<Sensor | null>(null)
const deletingSensor = ref(false)
const { error: deleteSensorError, run: runDeleteSensor } = useActionError()
async function confirmDeleteSensor() {
  if (!pendingDeleteSensor.value) return
  deletingSensor.value = true
  const id = pendingDeleteSensor.value.id
  await runDeleteSensor(async () => {
    await sensorService.remove(id)
    await loadRoom()
  })
  if (!deleteSensorError.value) pendingDeleteSensor.value = null
  deletingSensor.value = false
}

// Assign an existing sensor (unassigned, or currently in another room)
const assignableSensors = ref<Sensor[]>([])
const pickedSensorId = ref('')
async function openAssignSensor() {
  await runSensorAction(async () => {
    const all = await sensorService.list()
    assignableSensors.value = all.filter((s) => s.roomId !== roomId.value)
    pickedSensorId.value = assignableSensors.value[0]?.id ?? ''
  }, 'Could not load sensors to assign. Check your connection and try again.')
}
const assignSensorOptions = computed(() =>
  assignableSensors.value.map((s) => ({
    value: s.id,
    label: s.roomName ? `${s.name} (currently in ${s.roomName})` : s.name,
  })),
)
async function assignPickedSensor() {
  const sensor = assignableSensors.value.find((s) => s.id === pickedSensorId.value)
  if (!sensor) return
  await runSensorAction(async () => {
    await sensorService.update(sensor.id, { ...sensor, roomId: roomId.value })
    assignableSensors.value = assignableSensors.value.filter((s) => s.id !== sensor.id)
    pickedSensorId.value = assignableSensors.value[0]?.id ?? ''
    await loadRoom()
  })
}
async function unassignSensor(sensor: Sensor) {
  await runSensorAction(async () => {
    await sensorService.update(sensor.id, { ...sensor, roomId: null })
    await loadRoom()
  })
}

// --- Users: assign / remove ----------------------------------------------
// backend/actual has no /api/users endpoint at all yet (see
// userService.ts) — every action in this section is speculative.
const { error: userActionError, run: runUserAction } = useActionError()

const assignableUsers = ref<User[]>([])
const pickedUserId = ref('')
async function openAssignUser() {
  await runUserAction(async () => {
    const all = await userService.list()
    assignableUsers.value = all.filter((u) => !u.roomIds.includes(roomId.value))
    pickedUserId.value = assignableUsers.value[0]?.id ?? ''
  }, 'Not available yet \u2014 backend/actual has no user directory to assign from.')
}
const assignUserOptions = computed(() =>
  assignableUsers.value.map((u) => ({ value: u.id, label: u.name })),
)
async function assignPickedUser() {
  const user = assignableUsers.value.find((u) => u.id === pickedUserId.value)
  if (!user) return
  await runUserAction(async () => {
    await userService.update(user.id, { ...user, roomIds: [...user.roomIds, roomId.value] })
    assignableUsers.value = assignableUsers.value.filter((u) => u.id !== user.id)
    pickedUserId.value = assignableUsers.value[0]?.id ?? ''
    await loadRoom()
  })
}
async function removeUser(user: User) {
  await runUserAction(async () => {
    await userService.update(user.id, { ...user, roomIds: user.roomIds.filter((id) => id !== roomId.value) })
    await loadRoom()
  })
}
</script>

<template>
  <div class="space-y-8">
    <RouterLink :to="{ name: 'rooms' }" class="text-sm font-medium text-ink-soft hover:text-ink">
      &larr; Rooms
    </RouterLink>

    <p v-if="error" class="rounded-lg bg-alert-tint px-4 py-3 text-sm text-alert">{{ error }}</p>

    <div v-else-if="loading" class="h-24 animate-pulse rounded-2xl bg-mist-dim" />

    <template v-else-if="room">
      <h1 class="font-display text-xl font-semibold text-ink">{{ room.name }}</h1>

      <!-- Sensors -->
      <section class="space-y-4">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <h2 class="font-display text-base font-semibold text-ink">Sensors</h2>
          <div v-if="canManage" class="flex items-center gap-2">
            <BaseSelect
              v-if="assignableSensors.length > 0"
              v-model="pickedSensorId"
              label="Sensor"
              :options="assignSensorOptions"
              class="w-56"
            />
            <BaseButton v-if="assignableSensors.length > 0" size="sm" variant="subtle" @click="assignPickedSensor">
              Assign
            </BaseButton>
            <BaseButton size="sm" variant="ghost" @click="openAssignSensor">Find sensor to assign</BaseButton>
            <BaseButton size="sm" @click="openCreateSensor">
              <IconPlus class="h-4 w-4" />
              New sensor
            </BaseButton>
          </div>
        </div>

        <p v-if="sensorActionError" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">
          {{ sensorActionError }}
        </p>

        <p v-if="room.sensors.length === 0" class="text-sm text-ink-soft">No sensors in this room yet.</p>
        <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div v-for="sensor in room.sensors" :key="sensor.id" class="relative">
            <SensorCard
              :sensor="sensor"
              @edit="openEditSensor(sensor)"
              @delete="pendingDeleteSensor = sensor"
              @toggle-enabled="(on) => toggleSensor(sensor, { status: on ? 'on' : 'off' })"
              @toggle-curtains="(open) => toggleSensor(sensor, { data: { ...sensor.data, isOpen: open } })"
              @toggle-bulb="(on) => toggleSensor(sensor, { data: { ...sensor.data, isOn: on } })"
            />
            <button
              v-if="canManage"
              type="button"
              class="absolute right-3 top-3 rounded-md bg-paper/90 px-2 py-1 text-xs font-medium text-ink-faint shadow-sm hover:bg-alert-tint hover:text-alert"
              @click="unassignSensor(sensor)"
            >
              Remove from room
            </button>
          </div>
        </div>
      </section>

      <!-- Users -->
      <section class="space-y-4">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <h2 class="font-display text-base font-semibold text-ink">Users</h2>
          <div v-if="canManage" class="flex items-center gap-2">
            <BaseSelect
              v-if="assignableUsers.length > 0"
              v-model="pickedUserId"
              label="User"
              :options="assignUserOptions"
              class="w-56"
            />
            <BaseButton v-if="assignableUsers.length > 0" size="sm" variant="subtle" @click="assignPickedUser">
              Assign
            </BaseButton>
            <BaseButton size="sm" variant="ghost" @click="openAssignUser">Find user to assign</BaseButton>
          </div>
        </div>

        <p v-if="userActionError" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">
          {{ userActionError }}
        </p>

        <p v-if="room.users.length === 0" class="text-sm text-ink-soft">No users assigned to this room yet.</p>
        <div v-else class="overflow-hidden rounded-2xl border border-mist-dim bg-paper">
          <div
            v-for="user in room.users"
            :key="user.id"
            class="flex items-center justify-between border-b border-mist-dim px-5 py-3 last:border-0"
          >
            <div>
              <p class="text-sm font-medium text-ink">{{ user.name }}</p>
              <p class="text-xs text-ink-faint">{{ user.email }}</p>
            </div>
            <div class="flex items-center gap-3">
              <RoleBadge :role="user.role" />
              <button
                v-if="canManage"
                type="button"
                class="rounded-md p-1.5 text-ink-faint hover:bg-alert-tint hover:text-alert"
                aria-label="Remove from room"
                @click="removeUser(user)"
              >
                <IconTrash class="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>
      </section>
    </template>

    <SensorFormModal
      v-if="showSensorForm"
      :sensor="editingSensor"
      :saving="savingSensor"
      :default-room-id="roomId"
      :error="sensorFormError"
      @submit="handleSensorSubmit"
      @close="showSensorForm = false"
    />

    <ConfirmDialog
      v-if="pendingDeleteSensor"
      title="Delete sensor"
      :message="`Remove &quot;${pendingDeleteSensor.name}&quot;? This can't be undone.`"
      :loading="deletingSensor"
      :error="deleteSensorError"
      @confirm="confirmDeleteSensor"
      @cancel="pendingDeleteSensor = null"
    />
  </div>
</template>
