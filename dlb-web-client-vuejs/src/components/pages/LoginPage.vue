<script setup>
import { inject, ref } from 'vue';
import TextInput from '../widgets/TextInput.vue';
import PushButton from '../widgets/PushButton.vue';

const config = inject('config');
const state = inject('state');

const username = ref('');
const password = ref('');

function onLoginClick() {
    console.log(config.baseUrl);
    state.value.loggedIn = true;
}
</script>

<template>
    <div class="min-w-screen min-h-screen bg-background flex flex-col items-center">
        <img class="block pt-24 w-[300px] sm:w-[690px]" src="../../assets/img/dlb-long.png" alt="Dialogue Branch" />
        <div class="mt-3 font-title font-bold">Web Client Test Application</div>

        <div class="w-full px-4 my-6">
            <div class="sm:w-[448px] bg-box rounded-2xl px-6 py-8 mx-auto">
                <form @submit.prevent>
                    <div class="sm:flex sm:items-center">
                        <label class="font-title font-bold text-right sm:basis-[130px] sm:pr-4" for="username">Username:</label>
                        <TextInput class="block w-full sm:basis-0 sm:grow mt-2 sm:mt-0" type="text" name="username" placeholder="Username..." v-model="username"></TextInput>
                    </div>
                    <div class="sm:flex sm:items-center mt-6">
                        <label class="font-title font-bold text-right sm:basis-[130px] sm:pr-4" for="password">Password:</label>
                        <TextInput class="block w-full sm:basis-0 sm:grow mt-2 sm:mt-0" type="password" name="password" placeholder="Password..." v-model="password"></TextInput>
                    </div>
                    <div class="text-right mt-6">
                        <PushButton text="Log in" type="submit" @click="onLoginClick" />
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>
