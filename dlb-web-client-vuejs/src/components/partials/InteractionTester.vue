<script setup>
import { ref } from 'vue';
import IconButton from '../widgets/IconButton.vue';
import MainPagePanelHeader from '../widgets/MainPagePanelHeader.vue';
import MainPagePanelContainer from '../widgets/MainPagePanelContainer.vue';

const dialogueName = ref(null);

const loadDialogue = (name) => {
    dialogueName.value = name;
};

defineExpose({
    loadDialogue,
});
</script>

<template>
    <div class="flex flex-col gap-1">
        <MainPagePanelHeader
            title="Interaction Tester"
            :subtitle="dialogueName ? dialogueName + '.dlb' : null"
        >
            <template #buttons>
                <IconButton icon="fa-solid fa-circle-xmark" color="warning" :disabled="dialogueName === null" />
            </template>
        </MainPagePanelHeader>
        <MainPagePanelContainer>
        </MainPagePanelContainer>
    </div>
</template>
