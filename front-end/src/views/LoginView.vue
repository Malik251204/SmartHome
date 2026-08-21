<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseCard from '@/components/common/BaseCard.vue'
import IconBulb from '@/components/icons/IconBulb.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const demoAccounts = [
  { label: 'Admin (Alice)', email: 'alice@example.com', password: 'alice123' },
  { label: 'User (Bob)', email: 'bob@example.com', password: 'bob123' },
  { label: 'User (Carol)', email: 'carol@example.com', password: 'carol123' },
]

function fillDemo(account: (typeof demoAccounts)[number]) {
  email.value = account.email
  password.value = account.password
  error.value = ''
}

async function handleSubmit() {
  loading.value = true
  error.value = ''
  try {
    await auth.login(email.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/sensors'
    router.push(redirect)
  } catch {
    error.value = 'Invalid email or password.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-panel px-4">
    <div class="w-full max-w-sm">
      <div class="mb-6 flex items-center justify-center gap-2 text-paper">
        <span class="flex h-9 w-9 items-center justify-center rounded-lg bg-circuit">
          <IconBulb class="h-5 w-5" />
        </span>
        <span class="font-display text-lg font-semibold">HomeControl</span>
      </div>

      <BaseCard class="p-6">
        <h1 class="font-display text-base font-semibold text-ink">Sign in</h1>
        <p class="mb-5 mt-1 text-sm text-ink-soft">Access your smart home dashboard.</p>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <BaseInput v-model="email" label="Email" type="email" required placeholder="name@example.com" />
          <BaseInput v-model="password" label="Password" type="password" required placeholder="••••••••" />

          <p v-if="error" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">{{ error }}</p>

          <BaseButton type="submit" class="w-full" :loading="loading">Sign in</BaseButton>
        </form>

        <p class="mt-5 text-center text-sm text-ink-soft">
          New here?
          <RouterLink :to="{ name: 'signup' }" class="font-medium text-circuit hover:underline">
            Create an account
          </RouterLink>
        </p>

        <div class="mt-6 border-t border-mist-dim pt-4">
          <p class="mb-2 text-xs font-medium uppercase tracking-wide text-ink-faint">
            Seeded test accounts
          </p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="account in demoAccounts"
              :key="account.email"
              type="button"
              class="rounded-full border border-mist-dim px-3 py-1 text-xs font-medium text-ink-soft hover:bg-mist"
              @click="fillDemo(account)"
            >
              {{ account.label }}
            </button>
          </div>
        </div>
      </BaseCard>
    </div>
  </div>
</template>
