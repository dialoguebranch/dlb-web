<script setup>
import { computed, onMounted, ref, useTemplateRef } from 'vue';
import { BasicReply } from '@/dlb-lib/model/BasicReply';
import { AutoForwardReply } from '@/dlb-lib/model/AutoForwardReply';

const props = defineProps([
    'dialogueSteps',
]);

defineEmits([
    'selectReply',
]);

const breakpoint = ref('sm');

const resize = (newWidth) => {
    if (newWidth < 700) {
        breakpoint.value = 'sm';
    } else {
        breakpoint.value = 'md';
    }
};

defineExpose({
    resize,
});

const root = useTemplateRef('root');

const currentStep = computed(() => {
    return props.dialogueSteps.length == 0 ? null :
        props.dialogueSteps[props.dialogueSteps.length - 1];
});

onMounted(() => {
    resize(root.value.clientWidth);
});
</script>

<template>
    <div ref="root">
        <div v-if="currentStep" ref="root" class="flex flex-col font-title">
            <div
                class="mt-10 flex flex-col"
                :class="{
                    'mx-4': breakpoint !== 'md',
                    'ml-10': breakpoint === 'md',
                    'mr-20': breakpoint === 'md',
                }"
            >
                <div class="bg-speech-bubble text-white text-lg rounded-2xl p-5">{{ currentStep.statement.fullStatement() }}</div>
                <div class="border-20 border-transparent border-t-speech-bubble self-end mr-[10%]"></div>
            </div>
            <div class="flex" :class="{
                'flex-col': breakpoint !== 'md',
                'flex-row-reverse': breakpoint === 'md',
                'items-start': breakpoint === 'md',
            }">
                <img class="w-[300px]" src="@/assets/img/avatar-martin.png"
                    :class="{
                        'self-end': breakpoint !== 'md',
                    }"
                >
                <div
                    class="flex flex-col gap-2"
                    :class="{
                        'mx-4': breakpoint !== 'md',
                        'mt-4': breakpoint !== 'md',
                        'basis-0': breakpoint === 'md',
                        'grow': breakpoint === 'md',
                        'overflow-x-hidden': breakpoint === 'md',
                        'ml-12': breakpoint === 'md',
                        'mr-2': breakpoint === 'md',
                        'items-start': breakpoint === 'md',
                    }"
                >
                    <template v-for="(reply, index) in currentStep.replies">
                        <button
                            v-if="reply instanceof BasicReply"
                            class="block rounded-xl bg-orange-dark hover:bg-orange-medium text-white text-left p-3 cursor-pointer"
                            @click="$emit('selectReply', currentStep, reply)"
                        >
                            {{ reply.statement.fullStatement() }}
                        </button>
                        <button
                            v-if="reply instanceof AutoForwardReply"
                            class="block rounded-xl bg-orange-dark hover:bg-orange-medium text-white uppercase p-3 min-w-[160px] cursor-pointer"
                            @click="$emit('selectReply', currentStep, reply)"
                        >
                            Continue
                        </button>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
