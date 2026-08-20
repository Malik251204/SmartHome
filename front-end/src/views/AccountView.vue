<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useActionError } from '@/composables/useActionError'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const auth = useAuthStore()

const NOT_REAL_USER =
  "This account isn't linked to a real backend user, so changes here can't be saved yet."
const NO_PASSWORD_SUPPORT = "Not available yet — password changes aren't built on the backend yet."

const form = reactive({
  name: auth.user?.name ?? '',
  email: auth.user?.email ?? '',
  phoneNumber: auth.user?.phoneNumber ?? '',
})

const saving = ref(false)
const success = ref(false)
const { error, run } = useActionError()

const errors = computed(() => ({
  name: form.name.trim().length === 0 ? 'Name is required' : '',
  email: form.email.trim().length === 0 ? 'Email is required' : '',
}))
const canSubmit = computed(() => Object.values(errors.value).every((e) => e === '') && auth.hasRealIdentity)

async function handleSubmit() {
  if (!canSubmit.value) return
  success.value = false
  saving.value = true
  await run(() => auth.updateProfile(form), NOT_REAL_USER)
  if (!error.value) success.value = true
  saving.value = false
}
</script>

<template>
  <div class="max-w-lg space-y-6">
    <div>
      <h1 class="font-display text-xl font-semibold text-ink">My account</h1>
      <p class="text-sm text-ink-soft">Update your profile details.</p>
    </div>

    <p v-if="!auth.hasRealIdentity" class="rounded-lg bg-alert-tint px-4 py-3 text-sm text-alert">
      {{ NOT_REAL_USER }}
    </p>

    <p v-if="error" class="rounded-lg bg-alert-tint px-4 py-3 text-sm text-alert">
      {{ error }}
    </p>
    <p v-if="success" class="rounded-lg bg-circuit-tint px-4 py-3 text-sm text-circuit-dark">
      Profile updated.
    </p>

    <form class="space-y-4" @submit.prevent="handleSubmit">
      <BaseInput v-model="form.name" label="Name" required :error="errors.name" />
      <BaseInput v-model="form.email" label="Email" type="email" required :error="errors.email" />
      <BaseInput v-model="form.phoneNumber" label="Phone number" />

      <div>
        <BaseInput model-value="········" label="Password" type="password" disabled />
        <p class="mt-1 text-xs text-ink-faint">{{ NO_PASSWORD_SUPPORT }}</p>
      </div>

      <div class="flex justify-end pt-2">
        <BaseButton type="submit" :loading="saving" :disabled="!canSubmit">Save changes</BaseButton>
      </div>
    </form>
  </div>
</template>