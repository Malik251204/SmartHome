<script setup lang="ts">
import BaseBadge from '@/components/common/BaseBadge.vue'
import IconPencil from '@/components/icons/IconPencil.vue'
import IconTrash from '@/components/icons/IconTrash.vue'
import type { PreferenceRule } from '@/types/preference'

defineProps<{ rules: PreferenceRule[]; canDelete?: boolean }>()
const emit = defineEmits<{ edit: [rule: PreferenceRule]; delete: [rule: PreferenceRule] }>()
</script>

<template>
  <div class="space-y-3">
    <div
      v-for="rule in rules"
      :key="rule.id"
      class="rounded-2xl border border-mist-dim bg-paper p-4"
    >
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0 space-y-1.5">
          <div class="flex flex-wrap items-center gap-1.5">
            <BaseBadge tone="neutral">{{ rule.roomName ?? 'All my rooms' }}</BaseBadge>
            <BaseBadge :tone="rule.enabled ? 'circuit' : 'neutral'">
              {{ rule.enabled ? 'On' : 'Paused' }}
            </BaseBadge>
          </div>
          <p class="text-sm text-ink">{{ rule.text }}</p>
        </div>
        <div class="flex shrink-0 gap-1">
          <button
            type="button"
            class="rounded-md p-1.5 text-ink-faint hover:bg-mist hover:text-ink"
            aria-label="Edit preference"
            @click="emit('edit', rule)"
          >
            <IconPencil class="h-4 w-4" />
          </button>
          <button
            v-if="canDelete ?? true"
            type="button"
            class="rounded-md p-1.5 text-ink-faint hover:bg-alert-tint hover:text-alert"
            aria-label="Delete preference"
            @click="emit('delete', rule)"
          >
            <IconTrash class="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
