<script setup>
import { computed } from 'vue';
import HeaderMenuItem from '../widgets/HeaderMenuItem.vue';

const versionInfo = computed(() => {
    return 'Not connected.';
});

function logout() {
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
                <HeaderMenuItem text="Logout" @click="logout" />
            </div>
        </header>

        <main class="bg-background grow">
        </main>
    </div>
</template>
