<script setup lang="ts">
import { RouterLink } from 'vue-router'
import RoleBadge from './RoleBadge.vue'
import IconPencil from '@/components/icons/IconPencil.vue'
import IconTrash from '@/components/icons/IconTrash.vue'
import type { User } from '@/types/user'

defineProps<{ users: User[]; currentUserId?: string }>()
const emit = defineEmits<{ edit: [user: User]; delete: [user: User] }>()
</script>

<template>
  <div class="overflow-x-auto rounded-2xl border border-mist-dim bg-paper">
    <table class="w-full text-left text-sm">
      <thead>
        <tr class="border-b border-mist-dim text-xs uppercase tracking-wide text-ink-faint">
          <th class="px-5 py-3 font-medium">Name</th>
          <th class="px-5 py-3 font-medium">Email</th>
          <th class="px-5 py-3 font-medium">Phone</th>
          <th class="px-5 py-3 font-medium">Role</th>
          <th class="px-5 py-3 font-medium text-right">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="user in users"
          :key="user.id"
          class="border-b border-mist-dim last:border-0 hover:bg-mist/60"
        >
          <td class="px-5 py-3.5 font-medium text-ink">
            <RouterLink :to="{ name: 'user-detail', params: { id: user.id } }" class="hover:underline">
              {{ user.name }}
            </RouterLink>
            <span v-if="user.id === currentUserId" class="ml-1.5 text-xs font-normal text-ink-faint">
              (you)
            </span>
          </td>
          <td class="px-5 py-3.5 font-mono text-ink-soft">{{ user.email }}</td>
          <td class="px-5 py-3.5 font-mono text-ink-soft">{{ user.phoneNumber }}</td>
          <td class="px-5 py-3.5"><RoleBadge :role="user.role" /></td>
          <td class="px-5 py-3.5">
            <div class="flex justify-end gap-1">
              <button
                type="button"
                class="rounded-md p-1.5 text-ink-faint hover:bg-mist hover:text-ink"
                aria-label="Edit user"
                @click="emit('edit', user)"
              >
                <IconPencil class="h-4 w-4" />
              </button>
              <button
                type="button"
                class="rounded-md p-1.5 text-ink-faint hover:bg-alert-tint hover:text-alert"
                aria-label="Delete user"
                @click="emit('delete', user)"
              >
                <IconTrash class="h-4 w-4" />
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
