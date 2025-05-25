<script setup>
import { onMounted, ref } from 'vue';
import { useClient } from '@/composables/client.js';
import IconButton from '../widgets/IconButton.vue';
import MainPagePanelHeader from '../widgets/MainPagePanelHeader.vue';
import MainPagePanelContainer from '../widgets/MainPagePanelContainer.vue';

const client = useClient();

const variables = ref([]);

const loadVariables = () => {
    client.getVariables()
    .then((vars) => {
        variables.value = vars;
    });
};

defineExpose({
    loadVariables,
});

function deleteVariable(name) {
    client.setVariable(name, null)
    .then(() => {
        loadVariables()
    });
}

onMounted(() => {
    loadVariables();
});
</script>

<template>
    <div class="flex flex-col gap-1">
        <MainPagePanelHeader title="Variable Browser" class="sm:mr-1">
            <template #buttons>
                <IconButton icon="fa-solid fa-arrows-rotate" />
            </template>
        </MainPagePanelHeader>
        <MainPagePanelContainer class="sm:mr-1">
            <div class="flex flex-col gap-1 m-1">
                <div v-for="variable in variables" class="flex bg-grey-lighter p-1">
                    <div class="basis-0 grow overflow-hidden">
                        <div class="font-title font-semibold text-sm text-orange-darker">{{ variable.name }}</div>
                        <div class="font-title text-sm">{{ variable.value }}</div>
                    </div>
                    <div class="flex items-center">
                        <IconButton type="list-item" icon="fa-solid fa-trash" color="warning" @click="deleteVariable(variable.name)" />
                    </div>
                </div>
            </div>
        </MainPagePanelContainer>
    </div>
</template>
