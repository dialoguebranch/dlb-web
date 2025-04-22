<script setup>
import { computed, inject, ref, onMounted, onUnmounted } from 'vue';
import { DocumentFunctions } from '../../dlb-lib/util/DocumentFunctions.js';
import HeaderMenuItem from '../widgets/HeaderMenuItem.vue';

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

const minPanelWidth = 200;
const leftPanelWidth = ref(200);
const rightPanelWidth = ref(200);
const dividerWidth = 8;
var resizeListener;
var dragResizeState;

onMounted(() => {
    initPanelWidths();
    clearDragResizeState();
    resizeListener = () => reduceSideWidthsToFit();
    addEventListener('resize', resizeListener);
});

onUnmounted(() => {
    removeEventListener('resize', resizeListener);
});

function initPanelWidths() {
    let leftWidth = DocumentFunctions.getCookie('mainPageLeftPanelWidth');
    if (leftWidth) {
        leftPanelWidth.value = Math.max(minPanelWidth, parseInt(leftWidth));
    }
    let rightWidth = DocumentFunctions.getCookie('mainPageRightPanelWidth');
    if (rightWidth) {
        rightPanelWidth.value = Math.max(minPanelWidth, parseInt(rightWidth));
    }
    reduceSideWidthsToFit();
    // always save the widths again so the cookies stay valid
    saveLeftPanelWidth();
    saveRightPanelWidth();
}

function saveLeftPanelWidth() {
    DocumentFunctions.setCookie('mainPageLeftPanelWidth', leftPanelWidth.value.toString(), 365);
}

function saveRightPanelWidth() {
    DocumentFunctions.setCookie('mainPageRightPanelWidth', rightPanelWidth.value.toString(), 365);
}

function clearDragResizeState() {
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

function stopDragResize() {
    if (!dragResizeState) {
        return;
    }
    if (dragResizeState.left.dragging) {
        saveLeftPanelWidth();
    } else if (dragResizeState.right.dragging) {
        saveRightPanelWidth();
    }
    clearDragResizeState();
}

function onMouseDownLeftDivider(event) {
    if (!dragResizeState) {
        return;
    }
    dragResizeState.left.dragging = true;
    dragResizeState.left.startX = event.pageX;
    dragResizeState.left.startWidth = leftPanelWidth.value;
}

function onMouseDownRightDivider(event) {
    if (!dragResizeState) {
        return;
    }
    dragResizeState.right.dragging = true;
    dragResizeState.right.startX = event.pageX;
    dragResizeState.right.startWidth = rightPanelWidth.value;
}

function onMouseMove(event) {
    if (!dragResizeState) {
        return;
    }
    if (dragResizeState.left.dragging) {
        onDragLeftDivider(event);
    } else if (dragResizeState.right.dragging) {
        onDragRightDivider(event);
    }
}

function onDragLeftDivider(event) {
    const mainContainer = document.getElementById('main-container');
    const leftSpace = mainContainer.clientWidth - 2 * dividerWidth - minPanelWidth -
        rightPanelWidth.value;
    if (leftSpace < minPanelWidth)
        return;
    const prefWidth = dragResizeState.left.startWidth + event.x - dragResizeState.left.startX;
    leftPanelWidth.value = Math.max(minPanelWidth, Math.min(leftSpace, prefWidth));
}

function onDragRightDivider(event) {
    const mainContainer = document.getElementById('main-container');
    const rightSpace = mainContainer.clientWidth - 2 * dividerWidth - minPanelWidth -
        leftPanelWidth.value;
    if (rightSpace < minPanelWidth)
        return;
    const prefWidth = dragResizeState.right.startWidth + dragResizeState.right.startX - event.x;
    rightPanelWidth.value = Math.max(minPanelWidth, Math.min(rightSpace, prefWidth));
}

function reduceSideWidthsToFit() {
    const mainContainer = document.getElementById('main-container');
    const sideSpace = mainContainer.clientWidth - 2 * dividerWidth - minPanelWidth;
    let leftWidth = leftPanelWidth.value;
    let rightWidth = rightPanelWidth.value;
    const reduce = leftWidth + rightWidth - sideSpace;
    if (reduce > 0) {
        // left and side panel now take more space than available:
        // try to reduce their sizes proportionally
        const prefReduceLeft = Math.round(reduce * leftWidth / (leftWidth + rightWidth));
        const prefLeftWidth = leftWidth - prefReduceLeft;
        // set leftWidth to prefLeftWidth, but keep enough space for the right panel, and never go
        // under minPanelWidth
        leftWidth = Math.max(minPanelWidth, Math.min(sideSpace - minPanelWidth, prefLeftWidth));
        // set rightWidth to remaining space, and never go under minPanelWidth
        rightWidth = Math.max(minPanelWidth, sideSpace - leftWidth);
        if (leftWidth != leftPanelWidth.value) {
            leftPanelWidth.value = leftWidth;
            saveLeftPanelWidth();
        }
        if (rightWidth != rightPanelWidth.value) {
            rightPanelWidth.value = rightWidth;
            saveRightPanelWidth();
        }
    }
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

        <main id="main-container" class="bg-background grow flex" @mousemove="onMouseMove" @mouseup="stopDragResize" @mouseleave="stopDragResize">
            <div id="left-panel" class="bg-white hidden sm:block" :style="{width: leftPanelWidth + 'px'}"></div>
            <div class="cursor-col-resize hidden sm:block" :style="{width: dividerWidth + 'px'}" @mousedown="onMouseDownLeftDivider"></div>
            <div id="main-panel" class="grow bg-white"></div>
            <div class="cursor-col-resize hidden sm:block" :style="{width: dividerWidth + 'px'}" @mousedown="onMouseDownRightDivider"></div>
            <div id="right-panel" class="bg-white hidden sm:block" :style="{width: rightPanelWidth + 'px'}"></div>
        </main>
    </div>
</template>
