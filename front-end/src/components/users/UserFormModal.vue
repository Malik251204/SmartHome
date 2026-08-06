<script setup lang="ts">
import { reactive, computed, onMounted } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import RoomMultiSelect from '@/components/common/RoomMultiSelect.vue'
import { useRoomsStore } from '@/stores/rooms'
import { USER_ROLES, USER_ROLE_LABELS, type User, type UserInput } from '@/types/user'

const props = defineProps<{ user: User | null; saving?: boolean; error?: string | null }>()
const emit = defineEmits<{ submit: [input: UserInput]; close: [] }>()

const isEditing = !!props.user

const rooms = useRoomsStore()
onMounted(() => {
  if (rooms.items.length === 0) rooms.fetchAll()
})

const form = reactive<UserInput>(
  props.user
    ? {
        name: props.user.name,
        email: props.user.email,
        phoneNumber: props.user.phoneNumber,
        role: props.user.role,
        roomIds: [...props.user.roomIds],
      }
    : { name: '', email: '', phoneNumber: '', role: 'classic_user', roomIds: [] },
)

const roleOptions = USER_ROLES.map((r) => ({ value: r, label: USER_ROLE_LABELS[r] }))

const errors = computed(() => ({
  name: form.name.trim().length === 0 ? 'Name is required' : '',
  email: /^\S+@\S+\.\S+$/.test(form.email) ? '' : 'Enter a valid email address',
  phoneNumber: form.phoneNumber.trim().length === 0 ? 'Phone number is required' : '',
}))

const canSubmit = computed(() => Object.values(errors.value).every((e) => e === ''))

function handleSubmit() {
  if (!canSubmit.value) return
  emit('submit', { ...form, name: form.name.trim(), email: form.email.trim() })
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
        placeholder="+216 20 000 000"
      />
      <BaseSelect v-model="form.role" label="Role" :options="roleOptions" />
      <RoomMultiSelect v-model="form.roomIds" label="Rooms" :rooms="rooms.items" />

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
