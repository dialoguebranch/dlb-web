<script setup>
import { computed, useTemplateRef } from 'vue';
import { useStateManagement } from '../../composables/state-management.js';
import DialogueBrowser from '../partials/DialogueBrowser.vue';
import HeaderMenuItem from '../widgets/HeaderMenuItem.vue';
import InteractionTester from '../partials/InteractionTester.vue';
import ResizablePanels from '../widgets/ResizablePanels.vue';
import VariableBrowser from '../partials/VariableBrowser.vue';

const versionInfo = computed(() => {
    return 'Not connected.';
});

const stateManagement = useStateManagement();

const interactionTester = useTemplateRef('interaction-tester');

function onLogoutClick() {
    stateManagement.logout();
}

function onSelectDialogue(dialogueName) {
    interactionTester.value.loadDialogue(dialogueName);
}
</script>

<template>
    <div class="w-screen h-screen flex flex-col">
        <header class="flex bg-menu-bar shadow-md shadow-gray-400 z-1">
            <a class="shrink-0" href="/"><img class="box-content h-[60px] pl-4 py-3" src="../../assets/img/dlb-square.png"></a>
            <span class="hidden sm:block font-title text-sm self-end pl-2 pb-3">{{ versionInfo }}</span>
            <div class="grow"></div>
            <div class="flex basis-0">
                <HeaderMenuItem text="Documentation" link="https://www.dialoguebranch.com/docs/dialogue-branch/dev/index.html" />
                <HeaderMenuItem text="Log out" @click="onLogoutClick" />
            </div>
        </header>

        <ResizablePanels id="main-container" cookiePrefix="mainPage" class="grow">
            <template #left>
                <DialogueBrowser class="grow" @selectDialogue="onSelectDialogue" />
            </template>
            <template #main>
                <InteractionTester ref="interaction-tester" class="grow" />
            </template>
            <template #right>
                <VariableBrowser class="grow" />
            </template>
        </ResizablePanels>
    </div>
</template>
