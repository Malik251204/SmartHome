<script setup lang="ts">
import { reactive, computed, onMounted } from 'vue'
import BaseModal from '@/components/common/BaseModal.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseSelect from '@/components/common/BaseSelect.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import ToggleSwitch from '@/components/common/ToggleSwitch.vue'
import CurtainsFields from './CurtainsFields.vue'
import LightBulbFields from './LightBulbFields.vue'
import ACFields from './ACFields.vue'
import { useRoomsStore } from '@/stores/rooms'
import {
  SENSOR_TYPES,
  SENSOR_TYPE_LABELS,
  defaultDataFor,
  defaultUnitFor,
  type Sensor,
  type SensorInput,
  type CurtainsData,
  type LightBulbData,
  type ACData,
} from '@/types/sensor'

const props = defineProps<{
  sensor: Sensor | null
  saving?: boolean
  defaultRoomId?: string | null
  error?: string | null
}>()
const emit = defineEmits<{ submit: [input: SensorInput]; close: [] }>()

const isEditing = !!props.sensor

const rooms = useRoomsStore()
onMounted(() => {
  if (rooms.items.length === 0) rooms.fetchAll()
})

const roomOptions = computed(() => [
  { value: '', label: 'Unassigned' },
  ...rooms.items.map((r) => ({ value: r.id, label: r.name })),
])

const form = reactive<SensorInput>(
  props.sensor
    ? {
        name: props.sensor.name,
        type: props.sensor.type,
        unit: props.sensor.unit,
        status: props.sensor.status,
        data: { ...props.sensor.data },
        roomId: props.sensor.roomId,
      }
    : {
        name: '',
        type: 'CURTAINS',
        unit: defaultUnitFor('CURTAINS'),
        status: 'on',
        data: defaultDataFor('CURTAINS'),
        roomId: props.defaultRoomId ?? null,
      },
)

// BaseSelect only speaks plain strings, but roomId is `string | null` —
// bridge the two so "" on the widget round-trips to null on the model.
const roomIdField = computed({
  get: () => form.roomId ?? '',
  set: (v: string) => {
    form.roomId = v === '' ? null : v
  },
})

const typeOptions = SENSOR_TYPES.map((t) => ({ value: t, label: SENSOR_TYPE_LABELS[t] }))

function onTypeChange() {
  form.unit = defaultUnitFor(form.type)
  form.data = defaultDataFor(form.type)
}

const statusOn = computed({
  get: () => form.status === 'on',
  set: (v: boolean) => {
    form.status = v ? 'on' : 'off'
  },
})

const curtainsData = computed({
  get: () => form.data as CurtainsData,
  set: (v: CurtainsData) => {
    form.data = v
  },
})
const bulbData = computed({
  get: () => form.data as LightBulbData,
  set: (v: LightBulbData) => {
    form.data = v
  },
})
const acData = computed({
  get: () => form.data as ACData,
  set: (v: ACData) => {
    form.data = v
  },
})

const nameError = computed(() => (form.name.trim().length === 0 ? 'Name is required' : ''))
const canSubmit = computed(() => form.name.trim().length > 0)

function handleSubmit() {
  if (!canSubmit.value) return
  emit('submit', { ...form, name: form.name.trim() })
}
</script>

<template>
  <BaseModal :title="isEditing ? 'Edit sensor' : 'Add sensor'" @close="emit('close')">
    <form class="space-y-4" @submit.prevent="handleSubmit">
      <BaseInput
        v-model="form.name"
        label="Name"
        required
        :error="nameError"
        placeholder="e.g. Living room curtains"
      />

      <BaseSelect
        v-model="form.type"
        label="Type"
        :options="typeOptions"
        :disabled="isEditing"
        @update:model-value="onTypeChange"
      />
      <p v-if="isEditing" class="-mt-3 text-xs text-ink-faint">
        Type can't be changed after a sensor is created.
      </p>

      <BaseSelect v-model="roomIdField" label="Room" :options="roomOptions" />

      <div class="flex items-center justify-between rounded-lg border border-mist-dim px-3 py-2.5">
        <div>
          <span class="block text-sm font-medium text-ink">Device status</span>
          <span class="block text-xs text-ink-faint">Is this device online and reporting?</span>
        </div>
        <ToggleSwitch v-model="statusOn" label="Device status" />
      </div>

      <div class="rounded-lg bg-mist px-3 py-3">
        <CurtainsFields v-if="form.type === 'CURTAINS'" v-model="curtainsData" />
        <LightBulbFields v-else-if="form.type === 'LIGHT_BULB'" v-model="bulbData" />
        <ACFields v-else v-model="acData" />
      </div>

      <p v-if="error" class="rounded-lg bg-alert-tint px-3 py-2 text-sm text-alert">{{ error }}</p>

      <div class="flex justify-end gap-2 pt-2">
        <BaseButton variant="ghost" type="button" @click="emit('close')">Cancel</BaseButton>
        <BaseButton type="submit" :loading="saving" :disabled="!canSubmit">
          {{ isEditing ? 'Save changes' : 'Add sensor' }}
        </BaseButton>
      </div>
    </form>
  </BaseModal>
</template>
