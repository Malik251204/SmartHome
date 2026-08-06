<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { usePreferencesStore } from '@/stores/preferences'
import { useSensorsStore } from '@/stores/sensors'
import { useAuthStore } from '@/stores/auth'
import { useActionError } from '@/composables/useActionError'
import BaseButton from '@/components/common/BaseButton.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import PreferenceTable from '@/components/preferences/PreferenceTable.vue'
import PreferenceFormModal from '@/components/preferences/PreferenceFormModal.vue'
import IconPlus from '@/components/icons/IconPlus.vue'
import type { PreferenceRule, PreferenceRuleInput } from '@/types/preference'

const store = usePreferencesStore()
const sensorsStore = useSensorsStore()
const auth = useAuthStore()

onMounted(async () => {
  if (!auth.user) return
  await Promise.all([
    store.fetchForUser(auth.user.id),
    sensorsStore.items.length ? Promise.resolve() : sensorsStore.fetchAll(),
  ])
})

// Preferences don't exist on backend/actual yet at all (see
// preferenceService.ts) — every write below fails and shows this message.
const NOT_BUILT = 'Not available yet \u2014 preferences aren\u2019t built on backend/actual yet.'

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
  await runForm(
    () => (editingRule.value ? store.update(editingRule.value!.id, input) : store.create(input)),
    NOT_BUILT,
  )
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
  await runDelete(() => store.remove(id), NOT_BUILT)
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
          Automation rules for your own devices.
        </p>
      </div>
      <BaseButton @click="openCreate">
        <IconPlus class="h-4 w-4" />
        Add rule
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
      <p class="font-display text-sm font-medium text-ink">No rules yet</p>
      <p class="mt-1 text-sm text-ink-soft">
        Add a rule to describe how a device should behave automatically.
      </p>
    </div>

    <PreferenceTable v-else :rules="store.items" @edit="openEdit" @delete="pendingDelete = $event" />

    <PreferenceFormModal
      v-if="showForm && auth.user"
      :rule="editingRule"
      :sensors="sensorsStore.items"
      :user-id="auth.user.id"
      :saving="saving"
      :error="formError"
      @submit="handleSubmit"
      @close="showForm = false"
    />

    <ConfirmDialog
      v-if="pendingDelete"
      title="Delete rule"
      :message="`Remove this rule for &quot;${pendingDelete.deviceName}&quot;? This can't be undone.`"
      :loading="deleting"
      :error="deleteError"
      @confirm="confirmDelete"
      @cancel="pendingDelete = null"
    />
  </div>
</template>
