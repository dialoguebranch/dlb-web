<script setup>
import { computed, inject, ref } from 'vue';
import { DocumentFunctions } from '../../dlb-lib/util/DocumentFunctions.js';
import HeaderMenuItem from '../widgets/HeaderMenuItem.vue';

const state = inject('state');

const dialogueBrowserWidth = ref(200);
const variableBrowserWidth = ref(200);

var dragResizeState;

function initDragResize() {
    dragResizeState = {
        left: {
            dragging: false,
            startX: 0,
            startWidth: 0,
        },
        right: {
            dragging: false,
            startX: 0,
            startWidth: 0,
        },
    };
}

initDragResize();

const versionInfo = computed(() => {
    return 'Not connected.';
});

function onLogoutClick() {
    DocumentFunctions.deleteCookie('user.name');
    DocumentFunctions.deleteCookie('user.authToken');
    DocumentFunctions.deleteCookie('user.role');
    state.value.user = null;
}

function onMouseDownLeftDivider(event) {
    dragResizeState.left.dragging = true;
    dragResizeState.left.startX = event.pageX;
    dragResizeState.left.startWidth = dialogueBrowserWidth.value;
}

function onMouseDownRightDivider() {
    dragResizeState.right.dragging = true;
    dragResizeState.right.startX = event.pageX;
    dragResizeState.right.startWidth = variableBrowserWidth.value;
}

function onMouseMove(event) {
    if (dragResizeState.left.dragging) {
        onDragLeftDivider(event);
    } else if (dragResizeState.right.dragging) {
        onDragRightDivider(event);
    }
}

function onDragLeftDivider(event) {
    dialogueBrowserWidth.value = dragResizeState.left.startWidth +
        event.x - dragResizeState.left.startX;
}

function onDragRightDivider(event) {
    variableBrowserWidth.value = dragResizeState.right.startWidth +
        dragResizeState.right.startX - event.x;
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

        <main class="bg-background grow flex" @mousemove="onMouseMove" @mouseup="initDragResize" @mouseleave="initDragResize">
            <div class="bg-white" :style="{width: dialogueBrowserWidth + 'px'}"></div>
            <div class="w-2 cursor-col-resize" @mousedown="onMouseDownLeftDivider"></div>
            <div class="grow bg-white"></div>
            <div class="w-2 cursor-col-resize" @mousedown="onMouseDownRightDivider"></div>
            <div class="bg-white" :style="{width: variableBrowserWidth + 'px'}"></div>
        </main>
    </div>
</template>
