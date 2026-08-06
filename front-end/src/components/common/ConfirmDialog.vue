<script setup lang="ts">
import BaseModal from './BaseModal.vue'
import BaseButton from './BaseButton.vue'

withDefaults(
  defineProps<{
    title: string
    message: string
    confirmLabel?: string
    loading?: boolean
    error?: string | null
  }>(),
  { confirmLabel: 'Delete', loading: false, error: null },
)

const emit = defineEmits<{ confirm: []; cancel: [] }>()
</script>

<template>
  <BaseModal :title="title" @close="emit('cancel')">
    <p class="text-sm text-ink-soft">{{ message }}</p>
    <p v-if="error" class="mt-3 rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">{{ error }}</p>
    <div class="mt-6 flex justify-end gap-2">
      <BaseButton variant="ghost" @click="emit('cancel')">Cancel</BaseButton>
      <BaseButton variant="danger" :loading="loading" @click="emit('confirm')">
        {{ confirmLabel }}
      </BaseButton>
    </div>
  </BaseModal>
</template>
