<script setup lang="ts">
import BaseBadge from '@/components/common/BaseBadge.vue'
import { USER_ROLE_LABELS, type UserRole } from '@/types/user'

const props = defineProps<{ role: UserRole }>()

const tones: Record<UserRole, 'circuit' | 'coolant' | 'neutral'> = {
  admin: 'circuit',
  maintainer: 'coolant',
  classic_user: 'neutral',
}
</script>

<template>
  <BaseBadge :tone="tones[props.role]">{{ USER_ROLE_LABELS[props.role] }}</BaseBadge>
</template>
