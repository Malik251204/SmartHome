<script setup lang="ts">
import { reactive, computed } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import type { Room, RoomInput } from '@/types/room'

const props = defineProps<{ room: Room | null; saving?: boolean; error?: string | null }>()
const emit = defineEmits<{ submit: [input: RoomInput]; close: [] }>()

const isEditing = !!props.room

const form = reactive<RoomInput>({ name: props.room?.name ?? '' })

const nameError = computed(() => (form.name.trim().length === 0 ? 'Name is required' : ''))
const canSubmit = computed(() => form.name.trim().length > 0)

function handleSubmit() {
  if (!canSubmit.value) return
  emit('submit', { name: form.name.trim() })
}
</script>

<template>
  <BaseModal :title="isEditing ? 'Rename room' : 'Add room'" @close="emit('close')">
    <form class="space-y-4" @submit.prevent="handleSubmit">
      <BaseInput v-model="form.name" label="Name" required :error="nameError" placeholder="e.g. Living room" />

      <p v-if="error" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">{{ error }}</p>

      <div class="flex justify-end gap-2 pt-2">
        <BaseButton variant="ghost" type="button" @click="emit('close')">Cancel</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="!canSubmit">
          {{ isEditing ? 'Save changes' : 'Add room' }}
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>
