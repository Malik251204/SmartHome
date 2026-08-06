<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useUsersStore } from '@/stores/users'
import { useAuthStore } from '@/stores/auth'
import { useActionError } from '@/composables/useActionError'
import BaseButton from '@/components/common/BaseButton.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import UserTable from '@/components/users/UserTable.vue'
import UserFormModal from '@/components/users/UserFormModal.vue'
import IconPlus from '@/components/icons/IconPlus.vue'
import type { User, UserInput } from '@/types/user'

const store = useUsersStore()
const auth = useAuthStore()

onMounted(() => {
  store.fetchAll()
})

const showForm = ref(false)
const editingUser = ref<User | null>(null)
const saving = ref(false)
const { error: formError, run: runForm } = useActionError()

function openCreate() {
  editingUser.value = null
  formError.value = null
  showForm.value = true
}

function openEdit(user: User) {
  editingUser.value = user
  formError.value = null
  showForm.value = true
}

async function handleSubmit(input: UserInput) {
  saving.value = true
  await runForm(() => (editingUser.value ? store.update(editingUser.value!.id, input) : store.create(input)))
  if (!formError.value) showForm.value = false
  saving.value = false
}

const pendingDelete = ref<User | null>(null)
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
        <h1 class="font-display text-xl font-semibold text-ink">Users</h1>
        <p class="text-sm text-ink-soft">Manage accounts and roles for HomeControl.</p>
      </div>
      <BaseButton @click="openCreate">
        <IconPlus class="h-4 w-4" />
        Add user
      </BaseButton>
    </div>

    <p v-if="store.error" class="rounded-lg bg-alert-tint px-4 py-3 text-sm text-alert">
      {{ store.error }}
    </p>

    <div v-if="store.loading" class="h-64 animate-pulse rounded-2xl bg-mist-dim" />

    <div
      v-else-if="store.items.length === 0"
      class="rounded-2xl border border-dashed border-mist-dim bg-paper px-6 py-16 text-center"
    >
      <p class="font-display text-sm font-medium text-ink">No users yet</p>
      <p class="mt-1 text-sm text-ink-soft">Add teammates or household members to get started.</p>
    </div>

    <UserTable
      v-else
      :users="store.items"
      :current-user-id="auth.user?.id"
      @edit="openEdit"
      @delete="pendingDelete = $event"
    />

    <UserFormModal
      v-if="showForm"
      :user="editingUser"
      :saving="saving"
      :error="formError"
      @submit="handleSubmit"
      @close="showForm = false"
    />

    <ConfirmDialog
      v-if="pendingDelete"
      title="Delete user"
      :message="`Remove &quot;${pendingDelete.name}&quot;? This can't be undone.`"
      :loading="deleting"
      :error="deleteError"
      @confirm="confirmDelete"
      @cancel="pendingDelete = null"
    />
  </div>
</template>
