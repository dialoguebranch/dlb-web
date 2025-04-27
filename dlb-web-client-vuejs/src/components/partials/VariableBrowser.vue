<script setup>
import IconButton from '../widgets/IconButton.vue';
import MainPagePanelHeader from '../widgets/MainPagePanelHeader.vue';
import MainPagePanelContainer from '../widgets/MainPagePanelContainer.vue';
</script>

<template>
    <div class="flex flex-col gap-1">
        <MainPagePanelHeader title="Variable Browser" class="sm:mr-1">
            <template #buttons>
                <IconButton icon="fa-solid fa-arrows-rotate" />
            </template>
        </MainPagePanelHeader>
        <MainPagePanelContainer class="sm:mr-1">
        </MainPagePanelContainer>
    </div>
</template>
