<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { DocumentFunctions } from '../../dlb-lib/util/DocumentFunctions.js';

const props = defineProps([
    'id',
    'cookiePrefix',
]);

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
    let leftWidth = DocumentFunctions.getCookie(props.cookiePrefix + 'LeftPanelWidth');
    if (leftWidth) {
        leftPanelWidth.value = Math.max(minPanelWidth, parseInt(leftWidth));
    }
    let rightWidth = DocumentFunctions.getCookie(props.cookiePrefix + 'RightPanelWidth');
    if (rightWidth) {
        rightPanelWidth.value = Math.max(minPanelWidth, parseInt(rightWidth));
    }
    reduceSideWidthsToFit();
    // always save the widths again so the cookies stay valid
    saveLeftPanelWidth();
    saveRightPanelWidth();
}

function saveLeftPanelWidth() {
    DocumentFunctions.setCookie(props.cookiePrefix + 'LeftPanelWidth', leftPanelWidth.value.toString(), 365);
}

function saveRightPanelWidth() {
    DocumentFunctions.setCookie(props.cookiePrefix + 'RightPanelWidth', rightPanelWidth.value.toString(), 365);
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
    const root = document.getElementById(props.id);
    const leftSpace = root.clientWidth - 2 * dividerWidth - minPanelWidth -
        rightPanelWidth.value;
    if (leftSpace < minPanelWidth)
        return;
    const prefWidth = dragResizeState.left.startWidth + event.x - dragResizeState.left.startX;
    leftPanelWidth.value = Math.max(minPanelWidth, Math.min(leftSpace, prefWidth));
}

function onDragRightDivider(event) {
    const root = document.getElementById(props.id);
    const rightSpace = root.clientWidth - 2 * dividerWidth - minPanelWidth -
        leftPanelWidth.value;
    if (rightSpace < minPanelWidth)
        return;
    const prefWidth = dragResizeState.right.startWidth + dragResizeState.right.startX - event.x;
    rightPanelWidth.value = Math.max(minPanelWidth, Math.min(rightSpace, prefWidth));
}

function reduceSideWidthsToFit() {
    const root = document.getElementById(props.id);
    const sideSpace = root.clientWidth - 2 * dividerWidth - minPanelWidth;
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
    <div :id="id" class="bg-background flex" @mousemove.prevent="onMouseMove" @mouseup.prevent="stopDragResize" @mouseleave.prevent="stopDragResize">
        <div class="hidden overflow-x-hidden sm:flex flex-col" :style="{width: leftPanelWidth + 'px'}">
            <slot name="left" />
        </div>
        <div class="cursor-col-resize hidden sm:block" :style="{width: dividerWidth + 'px'}" @mousedown.prevent="onMouseDownLeftDivider"></div>
        <div class="basis-0 grow overflow-x-hidden flex flex-col">
            <slot name="main" />
        </div>
        <div class="cursor-col-resize hidden sm:block" :style="{width: dividerWidth + 'px'}" @mousedown.prevent="onMouseDownRightDivider"></div>
        <div class="hidden overflow-x-hidden sm:flex flex-col" :style="{width: rightPanelWidth + 'px'}">
            <slot name="right" />
        </div>
    </div>
</template>
