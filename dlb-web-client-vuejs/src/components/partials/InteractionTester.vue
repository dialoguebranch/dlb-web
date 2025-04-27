<script setup>
import { ref } from 'vue';
import { useClient } from '@/composables/client.js';
import IconButton from '../widgets/IconButton.vue';
import InteractionDialogueComponent from './InteractionDialogueComponent.vue';
import MainPagePanelHeader from '../widgets/MainPagePanelHeader.vue';
import MainPagePanelContainer from '../widgets/MainPagePanelContainer.vue';

const dialogueName = ref(null);
const dialogueSteps = ref([]);

const client = useClient();

const loadDialogue = (name) => {
    dialogueName.value = name;
    client.startDialogue(name, 'en')
    .then((dialogueStep) => {
        dialogueSteps.value.push(dialogueStep);
    });
};

defineExpose({
    loadDialogue,
});

function onSelectReply(dialogueStep, reply) {
    client.progressDialogue(dialogueStep.loggedDialogueId, dialogueStep.loggedInteractionIndex,
        reply.replyId)
    .then((dialogueStep) => {
        if (dialogueStep) {
            dialogueSteps.value.push(dialogueStep);
        }
    });
}
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
            <InteractionDialogueComponent :dialogueSteps="dialogueSteps" @selectReply="onSelectReply" />
        </MainPagePanelContainer>
    </div>
</template>
