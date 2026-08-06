<script setup lang="ts">
import IconClose from '@/components/icons/IconClose.vue'

defineProps<{
  title: string
}>()

const emit = defineEmits<{ close: [] }>()
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-150 ease-out"
      enter-from-class="opacity-0"
      leave-active-class="transition duration-100 ease-in"
      leave-to-class="opacity-0"
    >
      <div
        class="fixed inset-0 z-40 flex items-center justify-center bg-ink/40 px-4"
        @keydown.esc="emit('close')"
        @click.self="emit('close')"
      >
        <Transition
          appear
          enter-active-class="transition duration-150 ease-out"
          enter-from-class="opacity-0 scale-95"
          leave-active-class="transition duration-100 ease-in"
          leave-to-class="opacity-0 scale-95"
        >
          <div
            class="max-h-[90vh] w-full max-w-md overflow-y-auto rounded-2xl bg-paper shadow-xl"
            role="dialog"
            aria-modal="true"
          >
            <div class="flex items-center justify-between border-b border-mist-dim px-5 py-4">
              <h2 class="font-display text-base font-semibold text-ink">{{ title }}</h2>
              <button
                type="button"
                aria-label="Close"
                class="rounded-md p-1 text-ink-faint hover:bg-mist hover:text-ink"
                @click="emit('close')"
              >
                <IconClose class="h-4 w-4" />
              </button>
            </div>
            <div class="px-5 py-5">
              <slot />
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>
