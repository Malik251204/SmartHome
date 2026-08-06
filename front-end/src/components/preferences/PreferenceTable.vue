<script setup lang="ts">
import BaseBadge from '@/components/common/BaseBadge.vue'
import IconPencil from '@/components/icons/IconPencil.vue'
import IconTrash from '@/components/icons/IconTrash.vue'
import type { PreferenceRule } from '@/types/preference'

defineProps<{ rules: PreferenceRule[] }>()
const emit = defineEmits<{ edit: [rule: PreferenceRule]; delete: [rule: PreferenceRule] }>()
</script>

<template>
  <div class="overflow-x-auto rounded-2xl border border-mist-dim bg-paper">
    <table class="w-full text-left text-sm">
      <thead>
        <tr class="border-b border-mist-dim text-xs uppercase tracking-wide text-ink-faint">
          <th class="px-5 py-3 font-medium">Device</th>
          <th class="px-5 py-3 font-medium">If</th>
          <th class="px-5 py-3 font-medium">Then</th>
          <th class="px-5 py-3 font-medium">Strict</th>
          <th class="px-5 py-3 font-medium">Enabled</th>
          <th class="px-5 py-3 font-medium text-right">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="rule in rules"
          :key="rule.id"
          class="border-b border-mist-dim last:border-0 hover:bg-mist/60"
        >
          <td class="px-5 py-3.5 font-medium text-ink">{{ rule.deviceName }}</td>
          <td class="px-5 py-3.5 text-ink-soft">{{ rule.condition }}</td>
          <td class="px-5 py-3.5 text-ink-soft">{{ rule.action }}</td>
          <td class="px-5 py-3.5">
            <BaseBadge :tone="rule.strict ? 'alert' : 'neutral'">
              {{ rule.strict ? 'Strict' : 'Flexible' }}
            </BaseBadge>
          </td>
          <td class="px-5 py-3.5">
            <BaseBadge :tone="rule.enabled ? 'circuit' : 'neutral'">
              {{ rule.enabled ? 'On' : 'Paused' }}
            </BaseBadge>
          </td>
          <td class="px-5 py-3.5">
            <div class="flex justify-end gap-1">
              <button
                type="button"
                class="rounded-md p-1.5 text-ink-faint hover:bg-mist hover:text-ink"
                aria-label="Edit rule"
                @click="emit('edit', rule)"
              >
                <IconPencil class="h-4 w-4" />
              </button>
              <button
                type="button"
                class="rounded-md p-1.5 text-ink-faint hover:bg-alert-tint hover:text-alert"
                aria-label="Delete rule"
                @click="emit('delete', rule)"
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
