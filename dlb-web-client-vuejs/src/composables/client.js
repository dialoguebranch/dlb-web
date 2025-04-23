import { inject } from 'vue';
import { DialogueBranchClient } from '../dlb-lib/DialogueBranchClient.js';

export function useClient() {
	const config = inject('config');
	const client = new DialogueBranchClient(config.baseUrl);
	return client;
}
