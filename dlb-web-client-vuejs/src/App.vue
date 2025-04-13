<script setup>
import { ref } from 'vue';
import state from './state.js';
import LoginPage from './components/pages/LoginPage.vue';
import MainPage from './components/pages/MainPage.vue';

const loggedIn = ref(state.loggedIn);

function updateLoggedIn() {
    loggedIn.value = state.loggedIn;
}
</script>

<template>
    <MainPage v-if="loggedIn" @logout="updateLoggedIn" />
    <LoginPage v-else @login="updateLoggedIn" />
</template>
