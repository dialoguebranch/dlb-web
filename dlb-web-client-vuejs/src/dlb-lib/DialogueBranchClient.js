/* @license
 *
 *                Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *                                        as outlined below.
 *
 *                                            ----------
 *
 * Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import { AutoForwardReply } from "./model/AutoForwardReply";
import { BasicReply } from "./model/BasicReply";
import { DialogueStep } from "./model/DialogueStep";
import { Segment } from "./model/Segment";
import { Statement } from "./model/Statement";

export class DialogueBranchClient {
    constructor(baseUrl, authToken) {
        this._baseUrl = baseUrl;
        this._authToken = authToken;
        this._timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
    }

    onUnauthorized(onUnauthorized) {
        this._onUnauthorized = onUnauthorized;
    }

    login(user, password, tokenExpiration) {
        const loginUrl = this._baseUrl + "/auth/login";

        return fetch(loginUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                user: user,
                password: password,
                tokenExpiration: tokenExpiration
            }),
        })
        .then((response) => {
            if (response.ok) {
                return response.json();
            } else {
                return Promise.reject(response);
            }
        });
    }

    listDialogues() {
        const url = this._baseUrl + "/admin/list-dialogues";

        return fetch(url, {
            method: "GET",
            headers: {
                'X-Auth-Token': this._authToken,
                "Content-Type": "application/json",
            }
        })
        .then((response) => this._handleResponse(response));
    }

    startDialogue(dialogueName, language) {
        var url = this._baseUrl + "/dialogue/start";

        url += "?dialogueName="+dialogueName;
        url += "&language="+language;
        url += "&timeZone="+this._timeZone;

        return fetch(url, {
            method: "POST",
            headers: {
                'X-Auth-Token': this._authToken,
                "Content-Type": "application/json",
            }
        })
        .then((response) => this._handleResponse(response))
        .then((json) => this.createDialogueStepObject(json));
    }

    progressDialogue(loggedDialogueId, loggedInteractionIndex, replyId) {
        var url = this._baseUrl + "/dialogue/progress";

        url += "?loggedDialogueId="+loggedDialogueId;
        url += "&loggedInteractionIndex="+loggedInteractionIndex;
        url += "&replyId="+replyId;

        return fetch(url, {
            method: "POST",
            headers: {
                'X-Auth-Token': this._authToken,
                "Content-Type": "application/json",
            }
        })
        .then((response) => this._handleResponse(response))
        .then((json) => json.value ? this.createDialogueStepObject(json.value) : null);
    }

    // ----------------------------------------------------------
    // ---------- Helper functions related to Dialogue ----------
    // ----------------------------------------------------------

    createDialogueStepObject(data) {
        // Instantiate an empty DialogueStep
        var dialogueStep = DialogueStep.emptyInstance();

        // Add the simple parameters
        dialogueStep.dialogueName = data.dialogue;
        dialogueStep.node = data.node;
        dialogueStep.speaker = data.speaker;
        dialogueStep.loggedDialogueId = data.loggedDialogueId;
        dialogueStep.loggedInteractionIndex = data.loggedInteractionIndex;

        // Add the statement (consisting of a list of segments)
        var statement = Statement.emptyInstance();
        data.statement.segments.forEach(
            (element) => {
                var segment = new Segment(element.segmentType,element.text);
                statement.addSegment(segment);
            }
        );
        dialogueStep.statement = statement;

        // Add the replies
        data.replies.forEach(
            (element) => {
                var reply = null;
                if(element.statement == null) {
                    reply = AutoForwardReply.emptyInstance();
                } else {
                    reply = BasicReply.emptyInstance();
                }
                reply.replyId = element.replyId;
                reply.endsDialogue = element.endsDialogue;
                
                if(reply instanceof BasicReply) {
                    statement = Statement.emptyInstance();
                    element.statement.segments.forEach(
                        (segmentElement) => {
                            var segment = new Segment(segmentElement.segmentType,segmentElement.text);
                            statement.addSegment(segment);
                        }
                    );
                    reply.statement = statement;
                }
                reply.actions = element.actions; // TODO: Unfold 'actions' into Action-objects
                dialogueStep.addReply(reply);
            }
        );
        return dialogueStep;
    }

    _handleResponse(response) {
        if (response.ok) {
            return response.json();
        } else if (response.status == 401) {
            if (this._onUnauthorized) {
                this._onUnauthorized(response);
            }
            return Promise.reject(response);
        } else {
            return Promise.reject(response);
        }
    }
}
