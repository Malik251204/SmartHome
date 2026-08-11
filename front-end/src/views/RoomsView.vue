<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useRoomsStore } from '@/stores/rooms'
import { useAuthStore } from '@/stores/auth'
import { isAdminLike } from '@/utils/permissions'
import { useActionError } from '@/composables/useActionError'
import { roomService } from '@/services/roomService'
import BaseCard from '@/components/common/BaseCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import IconPlus from '@/components/icons/IconPlus.vue'
import IconGrid from '@/components/icons/IconGrid.vue'
import IconTrash from '@/components/icons/IconTrash.vue'
import IconPencil from '@/components/icons/IconPencil.vue'
import RoomFormModal from '@/components/rooms/RoomFormModal.vue'
import type { Room, RoomInput } from '@/types/room'

const store = useRoomsStore()
const auth = useAuthStore()
const router = useRouter()

onMounted(() => {
  store.fetchAll()
})

const canManage = computed(() => isAdminLike(auth.role))

// User doesn't carry its own room list (Room owns that relationship) — so
// figuring out "my rooms" means cross-referencing the full room list, same
// approach as PreferencesView.vue / UserDetailView.vue.
const myRoomIds = ref<Set<string>>(new Set())
onMounted(async () => {
  if (canManage.value || !auth.user) return
  try {
    const allRooms = await roomService.listDetailed()
    myRoomIds.value = new Set(
      allRooms.filter((r) => r.users.some((u) => u.id === auth.user!.id)).map((r) => r.id),
    )
  } catch {
    // Falls back to showing nothing rather than everything — safer default.
  }
})

// Admin sees every room. Anyone else only sees the rooms they're actually
// assigned to — this is a display filter, not a security boundary (there's
// no real auth on the backend yet, same caveat as everywhere else in the
// app right now).
const visibleRooms = computed(() => {
  if (canManage.value) return store.items
  return store.items.filter((r) => myRoomIds.value.has(r.id))
})

const showForm = ref(false)
const editingRoom = ref<Room | null>(null)
const saving = ref(false)
const { error: formError, run: runForm } = useActionError()

function openCreate() {
  editingRoom.value = null
  formError.value = null
  showForm.value = true
}
function openEdit(room: Room) {
  editingRoom.value = room
  formError.value = null
  showForm.value = true
}

async function handleSubmit(input: RoomInput) {
  saving.value = true
  await runForm(() => (editingRoom.value ? store.rename(editingRoom.value!.id, input) : store.create(input)))
  if (!formError.value) showForm.value = false
  saving.value = false
}

const pendingDelete = ref<Room | null>(null)
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

function openRoom(room: Room) {
  router.push({ name: 'room-detail', params: { id: room.id } })
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-4">
      <div>
        <h1 class="font-display text-xl font-semibold text-ink">Rooms</h1>
        <p class="text-sm text-ink-soft">
          {{ canManage ? 'Every room in the building.' : 'Rooms you have access to.' }}
        </p>
      </div>
      <BaseButton v-if="canManage" @click="openCreate">
        <IconPlus class="h-4 w-4" />
        Add room
      </BaseButton>
    </div>

    <p v-if="store.error" class="rounded-lg bg-alert-tint px-4 py-3 text-sm text-alert">
      {{ store.error }}
    </p>

    <div v-if="store.loading" class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <div v-for="i in 3" :key="i" class="h-32 animate-pulse rounded-2xl bg-mist-dim" />
    </div>

    <div
      v-else-if="visibleRooms.length === 0"
      class="rounded-2xl border border-dashed border-mist-dim bg-paper px-6 py-16 text-center"
    >
      <p class="font-display text-sm font-medium text-ink">
        {{ canManage ? 'No rooms yet' : "You're not assigned to any room yet" }}
      </p>
      <p class="mt-1 text-sm text-ink-soft">
        {{ canManage ? 'Add a room to start grouping sensors and users.' : 'Ask an admin to assign you to one.' }}
      </p>
    </div>

    <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <BaseCard
        v-for="room in visibleRooms"
        :key="room.id"
        class="group cursor-pointer p-5 transition-shadow hover:shadow-md"
        @click="openRoom(room)"
      >
        <div class="flex items-start justify-between">
          <div class="flex items-center gap-3">
            <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-circuit-tint text-circuit-dark">
              <IconGrid class="h-5 w-5" />
            </span>
            <div>
              <h3 class="font-display text-sm font-semibold text-ink">{{ room.name }}</h3>
              <p class="text-xs text-ink-faint">
                {{ room.sensorCount }} sensor{{ room.sensorCount === 1 ? '' : 's' }} ·
                {{ room.userCount }} user{{ room.userCount === 1 ? '' : 's' }}
              </p>
            </div>
          </div>
          <div v-if="canManage" class="flex gap-1 opacity-0 transition-opacity group-hover:opacity-100">
            <button
              type="button"
              class="rounded-md p-1.5 text-ink-faint hover:bg-mist hover:text-ink"
              aria-label="Rename room"
              @click.stop="openEdit(room)"
            >
              <IconPencil class="h-4 w-4" />
            </button>
            <button
              type="button"
              class="rounded-md p-1.5 text-ink-faint hover:bg-alert-tint hover:text-alert"
              aria-label="Delete room"
              @click.stop="pendingDelete = room"
            >
              <IconTrash class="h-4 w-4" />
            </button>
          </div>
        </div>
      </BaseCard>
    </div>

    <RoomFormModal
      v-if="showForm"
      :room="editingRoom"
      :saving="saving"
      :error="formError"
      @submit="handleSubmit"
      @close="showForm = false"
    />

    <ConfirmDialog
      v-if="pendingDelete"
      title="Delete room"
      :message="`Delete &quot;${pendingDelete.name}&quot;? Its sensors and users won't be deleted — they'll just become unassigned.`"
      :loading="deleting"
      :error="deleteError"
      @confirm="confirmDelete"
      @cancel="pendingDelete = null"
    />
  </div>
</template>
