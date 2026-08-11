<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { isAdminLike } from '@/utils/permissions'
import { roomService } from '@/services/roomService'
import { deviceService } from '@/services/deviceService'
import { userService } from '@/services/userService'
import { useActionError } from '@/composables/useActionError'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import RoleBadge from '@/components/users/RoleBadge.vue'
import SensorCard from '@/components/sensors/SensorCard.vue'
import DeviceCard from '@/components/rooms/DeviceCard.vue'
import IconTrash from '@/components/icons/IconTrash.vue'
import type { RoomDetail } from '@/types/room'
import { DEVICE_STATUS_PAIR, type Device } from '@/types/device'
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

// Refresh sensor readouts on the same cadence as the main Sensors page.
let pollTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => {
  pollTimer = setInterval(() => {
    if (document.visibilityState === 'visible') loadRoom()
  }, 5000)
})
onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

// --- Devices: the controllable actuators — real, working PUT ------------

const { error: deviceActionError, run: runDeviceAction } = useActionError()

async function toggleDevice(device: Device, on: boolean) {
  if (!device.type) return
  const status = DEVICE_STATUS_PAIR[device.type][on ? 1 : 0]
  await runDeviceAction(async () => {
    await deviceService.updateStatus(device.id, status)
    await loadRoom()
  })
}

// --- Users: assign / remove — real, working endpoints ---------------------

const { error: userActionError, run: runUserAction } = useActionError()

const assignableUsers = ref<User[]>([])
const pickedUserId = ref('')
async function openAssignUser() {
  await runUserAction(async () => {
    const all = await userService.list()
    const assignedIds = new Set(room.value?.users.map((u) => u.id) ?? [])
    assignableUsers.value = all.filter((u) => !assignedIds.has(u.id))
    pickedUserId.value = assignableUsers.value[0]?.id ?? ''
  }, 'Could not load users to assign. Check your connection and try again.')
}
const assignUserOptions = computed(() =>
  assignableUsers.value.map((u) => ({ value: u.id, label: u.name })),
)
async function assignPickedUser() {
  const user = assignableUsers.value.find((u) => u.id === pickedUserId.value)
  if (!user) return
  await runUserAction(async () => {
    await roomService.assignUser(roomId.value, user.id)
    assignableUsers.value = assignableUsers.value.filter((u) => u.id !== user.id)
    pickedUserId.value = assignableUsers.value[0]?.id ?? ''
    await loadRoom()
  })
}
async function removeUser(user: User) {
  await runUserAction(async () => {
    await roomService.removeUser(roomId.value, user.id)
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

      <!-- Devices: controllable actuators -->
      <section class="space-y-4">
        <h2 class="font-display text-base font-semibold text-ink">Devices</h2>

        <p v-if="deviceActionError" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">
          {{ deviceActionError }}
        </p>

        <p v-if="room.devices.length === 0" class="text-sm text-ink-soft">No devices in this room yet.</p>
        <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <DeviceCard
            v-for="device in room.devices"
            :key="device.id"
            :device="device"
            @toggle="(on) => toggleDevice(device, on)"
          />
        </div>
      </section>

      <!-- Sensors: read-only ambient readings -->
      <section class="space-y-4">
        <h2 class="font-display text-base font-semibold text-ink">Sensors</h2>

        <p v-if="room.sensors.length === 0" class="text-sm text-ink-soft">No sensors in this room yet.</p>
        <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <SensorCard v-for="sensor in room.sensors" :key="sensor.id" :sensor="sensor" />
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
  </div>
</template>
