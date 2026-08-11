<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { usePreferencesStore } from '@/stores/preferences'
import { useAuthStore } from '@/stores/auth'
import { roomService } from '@/services/roomService'
import { useActionError } from '@/composables/useActionError'
import BaseButton from '@/components/common/BaseButton.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import PreferenceTable from '@/components/preferences/PreferenceTable.vue'
import PreferenceFormModal from '@/components/preferences/PreferenceFormModal.vue'
import IconPlus from '@/components/icons/IconPlus.vue'
import type { PreferenceRule, PreferenceRuleInput } from '@/types/preference'

const store = usePreferencesStore()
const auth = useAuthStore()

// Rooms the current user is assigned to — the room picker in the form
// only offers these, not every room in the house. User doesn't own the
// room relationship, so this comes from cross-referencing the full room
// list rather than a direct lookup — see roomService.listDetailed().
const myRooms = ref<{ id: string; name: string }[]>([])

onMounted(async () => {
  if (!auth.user) return
  await store.fetchForUser(auth.user.id)
  try {
    const allRooms = await roomService.listDetailed()
    myRooms.value = allRooms
      .filter((r) => r.users.some((u) => u.id === auth.user!.id))
      .map((r) => ({ id: r.id, name: r.name }))
  } catch {
    // Room picker just falls back to "All my rooms" only — not fatal.
  }
})

const showForm = ref(false)
const editingRule = ref<PreferenceRule | null>(null)
const saving = ref(false)
const { error: formError, run: runForm } = useActionError()

function openCreate() {
  editingRule.value = null
  formError.value = null
  showForm.value = true
}

function openEdit(rule: PreferenceRule) {
  editingRule.value = rule
  formError.value = null
  showForm.value = true
}

async function handleSubmit(input: PreferenceRuleInput) {
  saving.value = true
  await runForm(() => (editingRule.value ? store.update(editingRule.value!.id, input) : store.create(input)))
  if (!formError.value) showForm.value = false
  saving.value = false
}

const pendingDelete = ref<PreferenceRule | null>(null)
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
        <h1 class="font-display text-xl font-semibold text-ink">Preferences</h1>
        <p class="max-w-xl text-sm text-ink-soft">
          Write how you like things, in your own words. Leave "Applies to" as
          "All my rooms" unless this is about one room specifically.
        </p>
      </div>
      <BaseButton @click="openCreate">
        <IconPlus class="h-4 w-4" />
        Add preference
      </BaseButton>
    </div>

    <p v-if="store.error" class="rounded-lg bg-alert-tint px-4 py-3 text-sm text-alert">
      {{ store.error }}
    </p>

    <div v-if="store.loading" class="h-48 animate-pulse rounded-2xl bg-mist-dim" />

    <div
      v-else-if="store.items.length === 0"
      class="rounded-2xl border border-dashed border-mist-dim bg-paper px-6 py-16 text-center"
    >
      <p class="font-display text-sm font-medium text-ink">No preferences yet</p>
      <p class="mt-1 text-sm text-ink-soft">
        Add one to describe how you like your rooms — messy or specific, both are fine.
      </p>
    </div>

    <PreferenceTable v-else :rules="store.items" @edit="openEdit" @delete="pendingDelete = $event" />

    <PreferenceFormModal
      v-if="showForm && auth.user"
      :rule="editingRule"
      :my-rooms="myRooms"
      :user-id="auth.user.id"
      :saving="saving"
      :error="formError"
      @submit="handleSubmit"
      @close="showForm = false"
    />

    <ConfirmDialog
      v-if="pendingDelete"
      title="Delete preference"
      message="Remove this preference? This can't be undone."
      :loading="deleting"
      :error="deleteError"
      @confirm="confirmDelete"
      @cancel="pendingDelete = null"
    />
  </div>
</template>
