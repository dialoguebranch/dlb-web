<script setup>
defineProps([
    'title',
    'subtitle',
]);
</script>

<template>
    <div class="flex mt-2 ml-1 items-center gap-2">
        <div class="font-title basis-0 overflow-x-hidden whitespace-nowrap text-ellipsis grow">{{ title }}
            <span v-if="subtitle" class="italic">({{ subtitle }})</span>
        </div>
        <div class="h-7.5">
            <slot name="buttons" />
        </div>
    </div>
</template>
