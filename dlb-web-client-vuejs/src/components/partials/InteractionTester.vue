<script setup>
import IconButton from '../widgets/IconButton.vue';

const loadDialogue = (dialogueName) => {
    console.log('Load dialogue: ' + dialogueName);
};

defineExpose({
    loadDialogue,
});
</script>

<template>
    <div class="flex flex-col gap-1">
        <div class="flex mt-2 items-center">
            <span class="font-title grow">Interaction Tester</span>
            <IconButton icon="fa-solid fa-circle-xmark" class="bg-icon-button-warning" />
        </div>
        <div class="basis-0 grow overflow-y-auto mb-2 bg-white shadow-[0_0_1px_black]">
        </div>
    </div>
</template>
