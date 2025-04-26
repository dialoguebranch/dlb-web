<script setup>
import { computed, inject } from 'vue';
import { DocumentFunctions } from '../../dlb-lib/util/DocumentFunctions.js';
import DialogueBrowser from '../partials/DialogueBrowser.vue';
import HeaderMenuItem from '../widgets/HeaderMenuItem.vue';
import ResizablePanels from '../widgets/ResizablePanels.vue';

const state = inject('state');

const versionInfo = computed(() => {
    return 'Not connected.';
});

function onLogoutClick() {
    DocumentFunctions.deleteCookie('user.name');
    DocumentFunctions.deleteCookie('user.authToken');
    DocumentFunctions.deleteCookie('user.role');
    state.value.user = null;
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
                <DialogueBrowser class="grow" />
            </template>
            <template #main>
                <div class="bg-white grow"></div>
            </template>
            <template #right>
                <div class="bg-white grow"></div>
            </template>
        </ResizablePanels>
    </div>
</template>
