<script setup>
import state from './state.js';
import LoginPage from './components/pages/LoginPage.vue';
import MainPage from './components/pages/MainPage.vue';
</script>

<template>
    <MainPage v-if="state.loggedIn" />
    <LoginPage v-else />
</template>
