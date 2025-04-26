<script setup>
import { ref } from 'vue';
import { useClient } from '../../composables/client.js';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import IconButton from '../widgets/IconButton.vue';

defineEmits([
    'selectDialogue',
]);

const client = useClient();

const dialogues = ref([]);

function listDialogues() {
    client.listDialogues()
    .then((json) => {
        dialogues.value = json.dialogueNames;
    })
    .catch((error) => {
        console.log(error);
    });
}

listDialogues();
</script>

<template>
    <div class="flex flex-col gap-1">
        <div class="flex mt-2 ml-2 items-center">
            <span class="font-title grow">Dialogue Browser</span>
            <IconButton icon="fa-solid fa-arrows-rotate" @click="listDialogues" />
        </div>
        <div class="basis-0 grow overflow-y-auto ml-1 mb-2 p-1 gap-1 flex flex-col bg-white shadow-[0_0_1px_black]">
            <div
                v-for="dialogue in dialogues"
                class="cursor-pointer bg-grey-lighter text-orange-darker hover:text-orange-dark font-title font-black text-xs p-1"
                @click="$emit('selectDialogue', dialogue)"
            >
                <FontAwesomeIcon icon="fa fa-circle-play" />
                {{ dialogue }}
            </div>
        </div>
    </div>
</template>
