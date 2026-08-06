<script setup lang="ts">
import { reactive, computed } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import ToggleSwitch from '@/components/common/ToggleSwitch.vue'
import type { PreferenceRule, PreferenceRuleInput } from '@/types/preference'
import type { Sensor } from '@/types/sensor'

const props = defineProps<{
  rule: PreferenceRule | null
  sensors: Sensor[]
  userId: string
  saving?: boolean
  error?: string | null
}>()
const emit = defineEmits<{ submit: [input: PreferenceRuleInput]; close: [] }>()

const isEditing = !!props.rule

const form = reactive<PreferenceRuleInput>(
  props.rule
    ? {
        userId: props.rule.userId,
        deviceId: props.rule.deviceId,
        deviceName: props.rule.deviceName,
        condition: props.rule.condition,
        action: props.rule.action,
        strict: props.rule.strict,
        enabled: props.rule.enabled,
      }
    : {
        userId: props.userId,
        deviceId: props.sensors[0]?.id ?? '',
        deviceName: props.sensors[0]?.name ?? '',
        condition: '',
        action: '',
        strict: false,
        enabled: true,
      },
)

const deviceOptions = computed(() => props.sensors.map((s) => ({ value: s.id, label: s.name })))

function onDeviceChange() {
  form.deviceName = props.sensors.find((s) => s.id === form.deviceId)?.name ?? ''
}

const errors = computed(() => ({
  deviceId: form.deviceId ? '' : 'Choose a device',
  condition: form.condition.trim().length === 0 ? 'Describe the condition' : '',
  action: form.action.trim().length === 0 ? 'Describe the action' : '',
}))
const canSubmit = computed(() => Object.values(errors.value).every((e) => e === ''))

function handleSubmit() {
  if (!canSubmit.value) return
  emit('submit', { ...form, condition: form.condition.trim(), action: form.action.trim() })
}
</script>

<template>
  <BaseModal :title="isEditing ? 'Edit rule' : 'Add rule'" @close="emit('close')">
    <form class="space-y-4" @submit.prevent="handleSubmit">
      <BaseSelect
        v-model="form.deviceId"
        label="Device"
        :options="deviceOptions"
        @update:model-value="onDeviceChange"
      />
      <BaseInput
        v-model="form.condition"
        label="If..."
        required
        :error="errors.condition"
        placeholder="e.g. Outdoor light exceeds 700 lux"
      />
      <BaseInput
        v-model="form.action"
        label="Then..."
        required
        :error="errors.action"
        placeholder="e.g. Close curtains"
      />

      <div class="flex items-center justify-between rounded-lg border border-mist-dim px-3 py-2.5">
        <div>
          <span class="block text-sm font-medium text-ink">Strict</span>
          <span class="block text-xs text-ink-faint">Not overridden by comfort settings</span>
        </div>
        <ToggleSwitch v-model="form.strict" label="Strict rule" />
      </div>

      <div class="flex items-center justify-between rounded-lg border border-mist-dim px-3 py-2.5">
        <div>
          <span class="block text-sm font-medium text-ink">Enabled</span>
          <span class="block text-xs text-ink-faint">Turn off to pause without deleting</span>
        </div>
        <ToggleSwitch v-model="form.enabled" label="Rule enabled" />
      </div>

      <div class="flex justify-end gap-2 pt-2">
        <BaseButton variant="ghost" type="button" @click="emit('close')">Cancel</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="!canSubmit">
          {{ isEditing ? 'Save changes' : 'Add rule' }}
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>
