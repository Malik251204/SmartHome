<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import IconBulb from '@/components/icons/IconBulb.vue'

const auth = useAuthStore()
const router = useRouter()

const name = ref('')
const email = ref('')
const phoneNumber = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')

const errors = computed(() => ({
  name: name.value.trim().length === 0 ? 'Name is required' : '',
  email: /^\S+@\S+\.\S+$/.test(email.value) ? '' : 'Enter a valid email address',
  phoneNumber: phoneNumber.value.trim().length === 0 ? 'Phone number is required' : '',
  password: password.value.length < 6 ? 'At least 6 characters' : '',
  confirmPassword: confirmPassword.value !== password.value ? "Passwords don't match" : '',
}))

const canSubmit = computed(() => Object.values(errors.value).every((e) => e === ''))

async function handleSubmit() {
  if (!canSubmit.value) return
  loading.value = true
  error.value = ''
  try {
    await auth.signup({
      name: name.value.trim(),
      email: email.value.trim(),
      phoneNumber: phoneNumber.value.trim(),
      password: password.value,
    })
    router.push({ name: 'rooms' })
  } catch {
    error.value = 'Could not create your account. The backend may be unreachable, or that email is already in use.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-panel px-4 py-10">
    <div class="w-full max-w-sm">
      <div class="mb-6 flex items-center justify-center gap-2 text-paper">
        <span class="flex h-9 w-9 items-center justify-center rounded-lg bg-circuit">
          <IconBulb class="h-5 w-5" />
        </span>
        <span class="font-display text-lg font-semibold">HomeControl</span>
      </div>

      <BaseCard class="p-6">
        <h1 class="font-display text-base font-semibold text-ink">Create your account</h1>
        <p class="mb-5 mt-1 text-sm text-ink-soft">
          New accounts are classic users. An admin can change your role later.
        </p>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <BaseInput v-model="name" label="Name" required :error="errors.name" placeholder="Full name" />
          <BaseInput
            v-model="email"
            label="Email"
            type="email"
            required
            :error="errors.email"
            placeholder="name@example.com"
          />
          <BaseInput
            v-model="phoneNumber"
            label="Phone number"
            required
            :error="errors.phoneNumber"
            placeholder="+216 20 000 000"
          />
          <BaseInput
            v-model="password"
            label="Password"
            type="password"
            required
            :error="errors.password"
            placeholder="At least 6 characters"
          />
          <BaseInput
            v-model="confirmPassword"
            label="Confirm password"
            type="password"
            required
            :error="errors.confirmPassword"
            placeholder="••••••••"
          />

          <p v-if="error" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">{{ error }}</p>

          <BaseButton type="submit" class="w-full" :loading="loading" :disabled="!canSubmit">
            Create account
          </BaseButton>
        </form>

        <p class="mt-5 text-center text-sm text-ink-soft">
          Already have an account?
          <RouterLink :to="{ name: 'login' }" class="font-medium text-circuit hover:underline">
            Sign in
          </RouterLink>
        </p>
      </BaseCard>
    </div>
  </div>
</template>
