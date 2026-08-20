<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { isAdminLike } from '@/utils/permissions'
import { USER_ROLE_LABELS } from '@/types/user'
import IconBulb from '@/components/icons/IconBulb.vue'
import IconGrid from '@/components/icons/IconGrid.vue'
import IconSliders from '@/components/icons/IconSliders.vue'
import IconUsers from '@/components/icons/IconUsers.vue'
import IconLogout from '@/components/icons/IconLogout.vue'

const auth = useAuthStore()
const router = useRouter()

const showUsersNav = computed(() => isAdminLike(auth.role))

const navItems = computed(() => [
  { to: { name: 'rooms' }, label: 'Rooms', icon: IconGrid, show: true },
  { to: { name: 'sensors' }, label: 'All sensors', icon: IconBulb, show: true },
  { to: { name: 'preferences' }, label: 'Preferences', icon: IconSliders, show: true },
  { to: { name: 'users' }, label: 'Users', icon: IconUsers, show: showUsersNav.value },
])

function logout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="min-h-screen bg-mist lg:flex">
    <aside class="flex shrink-0 flex-col bg-panel px-4 py-5 text-mist lg:w-60">
      <div class="mb-8 flex items-center gap-2 px-2">
        <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-circuit">
          <IconBulb class="h-4.5 w-4.5 text-white" />
        </span>
        <span class="font-display text-sm font-semibold tracking-wide">HomeControl</span>
      </div>

      <nav class="flex flex-1 flex-col gap-1">
        <RouterLink
          v-for="item in navItems.filter((i) => i.show)"
          :key="item.label"
          :to="item.to"
          class="flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-mist/70 transition-colors hover:bg-panel-soft hover:text-white"
          active-class="!bg-panel-soft !text-white"
        >
          <component :is="item.icon" class="h-4.5 w-4.5" />
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="mt-6 border-t border-panel-soft pt-4">
        <RouterLink
          :to="{ name: 'account' }"
          class="block truncate px-3 text-sm font-medium text-white hover:underline"
        >
          {{ auth.user?.name }}
        </RouterLink>
        <p class="px-3 text-xs text-mist/60">
          {{ auth.role ? USER_ROLE_LABELS[auth.role] : '' }}
        </p>
        <button
          type="button"
          class="mt-3 flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-mist/70 transition-colors hover:bg-panel-soft hover:text-white"
          @click="logout"
        >
          <IconLogout class="h-4.5 w-4.5" />
          Log out
        </button>
      </div>
    </aside>

    <main class="flex-1 px-6 py-8 lg:px-10">
      <div class="mx-auto max-w-6xl">
        <RouterView />
      </div>
    </main>
  </div>
</template>