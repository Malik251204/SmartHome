<script setup lang="ts">
import { reactive, computed } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { USER_ROLES, USER_ROLE_LABELS, type User, type UserInput } from '@/types/user'

const props = defineProps<{ user: User | null; saving?: boolean; error?: string | null }>()
const emit = defineEmits<{ submit: [input: UserInput]; close: [] }>()

const isEditing = !!props.user

// Phone number kept as a string in the form (natural for typing/editing),
// converted to a number only when submitting — matches UserInput.phoneNumber
// (a real number on backend/actual).
const form = reactive({
  name: props.user?.name ?? '',
  email: props.user?.email ?? '',
  phoneNumber: props.user ? String(props.user.phoneNumber) : '',
  role: props.user?.role ?? 'classic_user',
})

const roleOptions = USER_ROLES.map((r) => ({ value: r, label: USER_ROLE_LABELS[r] }))

const errors = computed(() => ({
  name: form.name.trim().length === 0 ? 'Name is required' : '',
  email: /^\S+@\S+\.\S+$/.test(form.email) ? '' : 'Enter a valid email address',
  phoneNumber: /^\d+$/.test(form.phoneNumber.trim()) ? '' : 'Digits only, no spaces or symbols',
}))

const canSubmit = computed(() => Object.values(errors.value).every((e) => e === ''))

function handleSubmit() {
  if (!canSubmit.value) return
  emit('submit', {
    name: form.name.trim(),
    email: form.email.trim(),
    phoneNumber: Number(form.phoneNumber.trim()),
    role: form.role,
  })
}
</script>

<template>
  <BaseModal :title="isEditing ? 'Edit user' : 'Add user'" @close="emit('close')">
    <form class="space-y-4" @submit.prevent="handleSubmit">
      <BaseInput v-model="form.name" label="Name" required :error="errors.name" placeholder="Full name" />
      <BaseInput
        v-model="form.email"
        label="Email"
        type="email"
        required
        :error="errors.email"
        placeholder="name@example.com"
      />
      <BaseInput
        v-model="form.phoneNumber"
        label="Phone number"
        required
        :error="errors.phoneNumber"
        placeholder="21620000000"
      />
      <BaseSelect v-model="form.role" label="Role" :options="roleOptions" />

      <p class="text-xs text-ink-faint">
        Room assignment is managed from each room's own page, not here.
      </p>

      <p v-if="error" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">{{ error }}</p>

      <div class="flex justify-end gap-2 pt-2">
        <BaseButton variant="ghost" type="button" @click="emit('close')">Cancel</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="!canSubmit">
          {{ isEditing ? 'Save changes' : 'Add user' }}
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>
