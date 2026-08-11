<script setup lang="ts">
import { reactive, computed } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import ToggleSwitch from '@/components/common/ToggleSwitch.vue'
import type { PreferenceRule, PreferenceRuleInput } from '@/types/preference'

const props = defineProps<{
  rule: PreferenceRule | null
  myRooms: { id: string; name: string }[]
  userId: string
  saving?: boolean
  error?: string | null
}>()
const emit = defineEmits<{ submit: [input: PreferenceRuleInput]; close: [] }>()

const isEditing = !!props.rule

// '' is the sentinel for "all my rooms" (roomId: null); BaseSelect only
// works with string values.
const ALL_ROOMS = ''

const form = reactive({
  roomId: props.rule?.roomId ?? ALL_ROOMS,
  text: props.rule?.text ?? '',
  enabled: props.rule?.enabled ?? true,
})

const roomOptions = computed(() => [
  { value: ALL_ROOMS, label: 'All my rooms' },
  ...props.myRooms.map((r) => ({ value: r.id, label: r.name })),
])

const textError = computed(() => (form.text.trim().length === 0 ? 'Write your preference' : ''))
const canSubmit = computed(() => textError.value === '')

function handleSubmit() {
  if (!canSubmit.value) return
  emit('submit', {
    userId: props.userId,
    roomId: form.roomId === ALL_ROOMS ? null : form.roomId,
    text: form.text.trim(),
    enabled: form.enabled,
  })
}
</script>

<template>
  <BaseModal :title="isEditing ? 'Edit preference' : 'Add preference'" @close="emit('close')">
    <form class="space-y-4" @submit.prevent="handleSubmit">
      <BaseSelect v-model="form.roomId" label="Applies to" :options="roomOptions" />

      <label class="block">
        <span class="mb-1.5 block text-sm font-medium text-ink-soft">
          Your preference<span class="text-alert"> *</span>
        </span>
        <textarea
          v-model="form.text"
          rows="4"
          placeholder="e.g. I don't want it too cold on Friday afternoons since I workout"
          class="w-full rounded-lg border bg-paper px-3 py-2 text-sm text-ink placeholder:text-ink-faint focus:border-circuit"
          :class="textError ? 'border-alert' : 'border-mist-dim'"
        />
        <span v-if="textError" class="mt-1 block text-xs text-alert">{{ textError }}</span>
      </label>

      <div class="flex items-center justify-between rounded-lg border border-mist-dim px-3 py-2.5">
        <div>
          <span class="block text-sm font-medium text-ink">Enabled</span>
          <span class="block text-xs text-ink-faint">Turn off to pause without deleting</span>
        </div>
        <ToggleSwitch v-model="form.enabled" label="Preference enabled" />
      </div>

      <p v-if="error" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">{{ error }}</p>

      <div class="flex justify-end gap-2 pt-2">
        <BaseButton variant="ghost" type="button" @click="emit('close')">Cancel</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="!canSubmit">
          {{ isEditing ? 'Save changes' : 'Add preference' }}
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>
