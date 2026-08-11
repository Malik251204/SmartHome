<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { userService } from '@/services/userService'
import { roomService } from '@/services/roomService'
import { preferenceService } from '@/services/preferenceService'
import { useActionError } from '@/composables/useActionError'
import RoleBadge from '@/components/users/RoleBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import BaseBadge from '@/components/common/BaseBadge.vue'
import IconTrash from '@/components/icons/IconTrash.vue'
import type { User } from '@/types/user'
import type { PreferenceRule } from '@/types/preference'

const route = useRoute()
const userId = computed(() => String(route.params.id))

const user = ref<User | null>(null)
const rooms = ref<{ id: string; name: string }[]>([])
const preferences = ref<PreferenceRule[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

async function load() {
  loading.value = true
  try {
    const [u, allRooms, prefs] = await Promise.all([
      userService.get(userId.value),
      roomService.listDetailed(),
      preferenceService.listForUser(userId.value),
    ])
    user.value = u
    rooms.value = allRooms.filter((r) => r.users.some((ru) => ru.id === userId.value)).map((r) => ({ id: r.id, name: r.name }))
    preferences.value = prefs
    error.value = null
  } catch {
    error.value = 'Could not load this user. They may have been deleted.'
  } finally {
    loading.value = false
  }
}

onMounted(load)

// Admins can delete a preference as moderation, but never edit one — see
// project notes: editing someone else's own words would misattribute
// them. This page is delete-only by design, not a missing edit feature.
const pendingDelete = ref<PreferenceRule | null>(null)
const deleting = ref(false)
const { error: deleteError, run: runDelete } = useActionError()

async function confirmDelete() {
  if (!pendingDelete.value) return
  deleting.value = true
  const id = pendingDelete.value.id
  await runDelete(async () => {
    await preferenceService.remove(id)
    preferences.value = preferences.value.filter((p) => p.id !== id)
  })
  if (!deleteError.value) pendingDelete.value = null
  deleting.value = false
}
</script>

<template>
  <div class="space-y-8">
    <RouterLink :to="{ name: 'users' }" class="text-sm font-medium text-ink-soft hover:text-ink">
      &larr; Users
    </RouterLink>

    <p v-if="error" class="rounded-lg bg-alert-tint px-4 py-3 text-sm text-alert">{{ error }}</p>

    <div v-else-if="loading" class="h-24 animate-pulse rounded-2xl bg-mist-dim" />

    <template v-else-if="user">
      <div class="flex items-center gap-3">
        <h1 class="font-display text-xl font-semibold text-ink">{{ user.name }}</h1>
        <RoleBadge :role="user.role" />
      </div>
      <p class="text-sm text-ink-soft">{{ user.email }} \u00b7 {{ user.phoneNumber }}</p>

      <section class="space-y-4">
        <h2 class="font-display text-base font-semibold text-ink">Rooms</h2>
        <p v-if="rooms.length === 0" class="text-sm text-ink-soft">Not assigned to any room yet.</p>
        <div v-else class="flex flex-wrap gap-2">
          <RouterLink
            v-for="room in rooms"
            :key="room.id"
            :to="{ name: 'room-detail', params: { id: room.id } }"
          >
            <BaseBadge tone="neutral">{{ room.name }}</BaseBadge>
          </RouterLink>
        </div>
      </section>

      <section class="space-y-4">
        <h2 class="font-display text-base font-semibold text-ink">Preferences</h2>

        <p v-if="deleteError" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">{{ deleteError }}</p>

        <p v-if="preferences.length === 0" class="text-sm text-ink-soft">No preferences written yet.</p>
        <div v-else class="space-y-3">
          <div
            v-for="pref in preferences"
            :key="pref.id"
            class="rounded-2xl border border-mist-dim bg-paper p-4"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0 space-y-1.5">
                <div class="flex flex-wrap items-center gap-1.5">
                  <BaseBadge tone="neutral">{{ pref.roomName ?? 'All rooms' }}</BaseBadge>
                  <BaseBadge :tone="pref.enabled ? 'circuit' : 'neutral'">
                    {{ pref.enabled ? 'On' : 'Paused' }}
                  </BaseBadge>
                </div>
                <p class="text-sm text-ink">{{ pref.text }}</p>
              </div>
              <button
                type="button"
                class="shrink-0 rounded-md p-1.5 text-ink-faint hover:bg-alert-tint hover:text-alert"
                aria-label="Delete preference"
                @click="pendingDelete = pref"
              >
                <IconTrash class="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>
      </section>
    </template>

    <ConfirmDialog
      v-if="pendingDelete"
      title="Delete preference"
      message="Remove this preference? This can't be undone."
      :loading="deleting"
      @confirm="confirmDelete"
      @cancel="pendingDelete = null"
    />
  </div>
</template>
