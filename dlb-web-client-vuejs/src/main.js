import './assets/css/main.css';
import config from './config.js';
import state from './state.js';

import { createApp, ref } from 'vue';
import App from './App.vue';

import { library } from '@fortawesome/fontawesome-svg-core';
import { fas } from '@fortawesome/free-solid-svg-icons';
library.add(fas);

const stateRef = ref(state);

const app = createApp(App);
app.provide('config', config);
app.provide('state', stateRef);
app.mount('#app');
