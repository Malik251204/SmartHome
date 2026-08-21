<script setup lang="ts">
import { reactive, computed } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import {
  DEVICE_TYPES,
  DEVICE_TYPE_LABELS,
  DEVICE_STATUS_PAIR,
  type Device,
  type DeviceInput,
  type DeviceType,
} from '@/types/device'

// Scoped to one room — devices are created/edited from that room's own
// page, matching how user assignment already works (RoomDetailView).
const props = defineProps<{
  device: Device | null
  roomId: string
  saving?: boolean
  error?: string | null
}>()
const emit = defineEmits<{ submit: [input: DeviceInput]; close: [] }>()

const isEditing = !!props.device

const form = reactive({
  name: props.device?.name ?? '',
  type: (props.device?.type ?? 'AC') as DeviceType,
  // Default to the "off" half of the type's status pair on create.
  status: props.device?.status ?? DEVICE_STATUS_PAIR['AC'][0],
})

const typeOptions = DEVICE_TYPES.map((t) => ({ value: t, label: DEVICE_TYPE_LABELS[t] }))
const statusOptions = computed(() =>
  DEVICE_STATUS_PAIR[form.type].map((s) => ({ value: s, label: s })),
)

function onTypeChange() {
  // Each type has its own two-state vocabulary — reset to the type's
  // first state so we never submit a status the new type doesn't have.
  form.status = DEVICE_STATUS_PAIR[form.type][0]
}

const nameError = computed(() => (form.name.trim().length === 0 ? 'Name is required' : ''))
const canSubmit = computed(() => form.name.trim().length > 0)

function handleSubmit() {
  if (!canSubmit.value) return
  emit('submit', {
    name: form.name.trim(),
    type: form.type,
    status: form.status,
    roomId: props.roomId,
  })
}
</script>

<template>
  <BaseModal :title="isEditing ? 'Edit device' : 'Add device'" @close="emit('close')">
    <form class="space-y-4" @submit.prevent="handleSubmit">
      <BaseInput
        v-model="form.name"
        label="Name"
        required
        :error="nameError"
        placeholder="e.g. Living room AC"
      />
      <BaseSelect v-model="form.type" label="Type" :options="typeOptions" @update:model-value="onTypeChange" />
      <BaseSelect v-model="form.status" label="Initial status" :options="statusOptions" />

      <p v-if="error" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">{{ error }}</p>

      <div class="flex justify-end gap-2 pt-2">
        <BaseButton variant="ghost" type="button" @click="emit('close')">Cancel</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="!canSubmit">
          {{ isEditing ? 'Save changes' : 'Add device' }}
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>
