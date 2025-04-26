<script setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

defineProps([
    'icon',
]);
</script>

<template>
    <button class="rounded-full bg-icon-button w-7.5 h-7.5 cursor-pointer hover:brightness-110"><FontAwesomeIcon class="text-white" :icon="icon" /></button>
</template>
