<script setup>
import state from '../../state.js';

const props = defineProps([
    'link',
    'text',
]);

const emit = defineEmits([
    'click',
]);

function onclick() {
    if (props.link) {
        window.open(props.link, '_blank');
    } else {
        emit('click');
    }
}
</script>

<template>
    <button class="font-title font-medium text-menu-text text-xl text-center hover:text-menu-text-highlight bg-menu-item hover:bg-menu-item-highlight px-4 cursor-pointer"
        @click="onclick"
    >{{ text }}</button>
</template>
