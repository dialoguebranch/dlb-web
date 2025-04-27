<script setup>
import { computed } from 'vue';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

/**
 * Properties:
 * - icon (String): fontawesome classes like: "fa-solid fa-arrows-rotate"
 * - color (String): one of: default, warning
 * - disabled (boolean)
 */
const props = defineProps({
    'icon': String,
    'color': {
        type: String,
        default: 'default',
    },
    'disabled': Boolean,
});

const classes = computed(() => {
    const result = [];
    if (props.disabled) {
        result.push('cursor-not-allowed');
    } else {
        result.push('cursor-pointer');
    }
    if (props.disabled) {
        result.push('bg-icon-button-disabled');
    } else if (props.color == 'warning') {
        result.push('bg-icon-button-warning');
        result.push('hover:bg-icon-button-warning-hover');
    } else {
        result.push('bg-icon-button');
        result.push('hover:bg-icon-button-hover');
    }
    return result;
});
</script>

<template>
    <button class="rounded-full w-7.5 h-7.5" :class="classes"><FontAwesomeIcon class="text-white" :icon="icon" /></button>
</template>
