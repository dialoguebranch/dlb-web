<script setup>
import { computed } from 'vue';
import { BasicReply } from '@/dlb-lib/model/BasicReply';

const props = defineProps([
    'dialogueSteps',
]);

defineEmits([
    'selectReply',
]);

const currentStep = computed(() => {
    return props.dialogueSteps.length == 0 ? null :
        props.dialogueSteps[props.dialogueSteps.length - 1];
});
</script>

<template>
    <div v-if="currentStep" class="font-title p-2">
        <div class="flex gap-5 mb-5">
            <div class="basis-0 grow-1 font-semibold text-right">{{ currentStep.speaker }}:</div>
            <div class="basis-0 grow-4 font-light">{{ currentStep.statement.fullStatement() }}</div>
        </div>
        <div class="text-interaction-reply-option font-semibold">
            <div v-for="(reply, index) in currentStep.replies">
                <div v-if="reply instanceof BasicReply" class="flex gap-2">
                    <div class="basis-0 grow-1 text-right">{{ index + 1 }}: -</div>
                    <div class="basis-0 grow-8">
                        <span
                            class="cursor-pointer hover:text-interaction-reply-option-hover"
                            @click="$emit('selectReply', currentStep, reply)"
                        >
                            {{ reply.statement.fullStatement() }}
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
