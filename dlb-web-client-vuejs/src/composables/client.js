import { inject } from 'vue';
import { DialogueBranchClient } from '../dlb-lib/DialogueBranchClient.js';

export function useClient() {
	const config = inject('config');
	const state = inject('state');
	const client = new DialogueBranchClient(config.baseUrl, state.value.user ? state.value.user.authToken : null);
	return client;
}
