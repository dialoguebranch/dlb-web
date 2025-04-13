<script setup>
import HeaderNavLink from './components/HeaderNavLink.vue';

function logout() {
}
</script>

<template>
    <div class="w-screen h-screen flex flex-col">
        <header class="flex shadow-md shadow-grey-200 z-1">
            <a href="/"><img class="box-content h-[60px] pl-4 py-3" src="./assets/img/dlb-square.png"></a>
            <span class="font-title text-sm self-end pl-2 pb-3">Some version info here.</span>
            <div class="grow"></div>
            <div class="flex">
                <HeaderNavLink text="Documentation" link="https://www.dialoguebranch.com/docs/dialogue-branch/dev/index.html" />
                <HeaderNavLink text="Logout" @click="logout" />
            </div>
        </header>

        <main class="bg-gray-200 grow">
        </main>
    </div>
</template>
