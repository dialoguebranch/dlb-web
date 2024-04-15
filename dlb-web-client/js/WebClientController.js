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

import {DialogueBranchController} from './classes/DialogueBranchController.js';
import {DialogueBranchConfig} from './classes/DialogueBranchConfig.js';
import {DialogueBranchClient, LOG_LEVEL_NAMES} from './classes/DialogueBranchClient.js';
import {DialogueStep} from './classes/DialogueStep.js';
import {ClientState} from './classes/ClientState.js';
import {Logger} from './classes/Logger.js';
import {User} from './classes/User.js';
import {DocumentFunctions} from './classes/DocumentFunctions.js';
import {Statement} from './classes/Statement.js';
import {Segment} from './classes/Segment.js';
import {AutoForwardReply} from './classes/AutoForwardReply.js';
import {BasicReply} from './classes/BasicReply.js';

export class WebClientController extends DialogueBranchController {

    constructor() {
        super();

        this._dialogueReplyElements = new Array();

        // Initialize the logger
        this.logger = new Logger();
        this.dialogueBranchConfig = new DialogueBranchConfig(0,'http://localhost:8080/dlb-web-service/v1');
        this.logger.logLevel = this.dialogueBranchConfig.logLevel;
        this.logger.info("Initialized Logger with log level '" 
            + this.dialogueBranchConfig.logLevel 
            + "' ('" 
            + LOG_LEVEL_NAMES[this.dialogueBranchConfig.logLevel] 
            + "').");

        // Initialize the DialogueBranchClient object, used for communication to the Dialogue Branch Web Service
        this.dialogueBranchClient = new DialogueBranchClient(this.dialogueBranchConfig.baseUrl, this.logger, this);
        this.logger.info("Initalized DialogueBranchClient with a connection to Web Service at '"+ this.dialogueBranchConfig.baseUrl + "'.");

        // Initialize the ClientState object and take actions
        this.clientState = new ClientState(this.logger);
        this.clientState.loadFromCookie();
        console.log(this.clientState);

        // If user info was loaded from Cookie, validate the authToken that was found
        if(this.clientState.user != null) {
            this.dialogueBranchClient.user = this.clientState.user;
            this.dialogueBranchClient.callAuthValidate(this.clientState.user.authToken);
        }

        // Make a call to the Web Service for service info.
        this.dialogueBranchClient.callInfo();

        this.updateUIState();
    }
    
    // ------------------------------------------------------
    // -------------------- User Actions --------------------
    // ------------------------------------------------------

    // ---------- Login ----------

    actionLogin(event) {
        event.preventDefault();

        // Remove any possible previous error indications
        document.getElementById("login-form-password-field").classList.remove("login-form-error-class");
        document.getElementById("login-form-username-field").classList.remove("login-form-error-class");

        var formUsername = document.getElementById("login-form-username-field").value;
        var formPassword = document.getElementById("login-form-password-field").value;

        this.dialogueBranchClient.callLogin(formUsername, formPassword, 0);
    }

    // ---------- Logout ----------

    actionLogout(event) {
        event.preventDefault();

        this.logger.info("Logging out user '" + this.clientState.user.name + "'.");
        DocumentFunctions.deleteCookie('user.name');
        DocumentFunctions.deleteCookie('user.authToken');
        DocumentFunctions.deleteCookie('user.role');

        this.clientState.user = null;
        this.clientState.loggedIn = false;
        this.dialogueBranchClient.user = null;
        this.updateUIState();
    }

    // ---------- Toggle Debug Console ----------

    /**
     * Toggle the visibility of the Debug Console. If it was visible before, make it invisible, and the other way
     * around. Store the new state in the ClientState.
     */
    actionToggleDebugConsole(event) {
        event.preventDefault();

        if(this.clientState.debugConsoleVisible) {
            this.setDebugConsoleVisibility(false);
            this.clientState.debugConsoleVisible = false;
        } else {
            this.setDebugConsoleVisibility(true);
            this.clientState.debugConsoleVisible = true;
        }
    }

    // ------------------------------------------------------------
    // -------------------- Interaction Tester --------------------
    // ------------------------------------------------------------

    actionCancelDialogue(loggedDialogueId) {
        this.logger.info("Cancelling the current dialogue with loggedDialogueId: '"+loggedDialogueId+"'.");
        this.dialogueBranchClient.callCancelDialogue(loggedDialogueId);
    }

    customCancelDialogueSuccess() {
        this.logger.info("Custom Cancel Dialogue Success!");

        this.renderDialogueStep(null, "Dialogue Cancelled");
    }

    // ----------------------------------------------------------
    // -------------------- Dialogue Browser --------------------
    // ----------------------------------------------------------

    actionRefreshDialogueBrowser(event) {
        if(event != null) event.preventDefault();
        this.dialogueBranchClient.callListDialogues();
    }

    customListDialoguesSuccess(dialogueNames) {
        var dialogueBrowserContentField = document.getElementById("dialogue-browser-content");

        // Empty the content field before populating
        dialogueBrowserContentField.innerHTML = "";

        // If the list of dialogueNames is empty, present a warning message
        if(dialogueNames.length == 0) {
            var dialogueBrowserEmptyElement = document.createElement("div");
            dialogueBrowserEmptyElement.classList.add("dialogue-browser-empty-warning");
            dialogueBrowserEmptyElement.innerHTML = "No dialogues available. Please make sure that the Dialogue Branch Web Service you are connected to has access to dialogue script resources.";
            dialogueBrowserContentField.appendChild(dialogueBrowserEmptyElement);
        } else {
            for(var i=0; i< dialogueNames.length; i++) {

                var dialogueBrowserEntry = document.createElement("div");
                dialogueBrowserEntry.classList.add("dialogue-browser-entry");
                dialogueBrowserEntry.innerHTML = "<i class='fa-solid fa-circle-play'></i> " + dialogueNames[i];

                dialogueBrowserEntry.addEventListener("click", this.actionStartDialogue.bind(this, dialogueNames[i]), false);
                dialogueBrowserContentField.appendChild(dialogueBrowserEntry);
            }
        }

        this.logger.info("Updated the contents of the Dialogue Browser, showing "+dialogueNames.length+" available dialogues.");
    }

    customListDialoguesError(err) {
        this.logger.error("Retrieving dialogue list failed with the following result: "+err);
    }

    // ----------------------------------------------------------
    // -------------------- Variable Browser --------------------
    // ----------------------------------------------------------

    actionListVariables(event) {
        if(event != null) event.preventDefault();
        this.dialogueBranchClient.callGetVariables();
    }

    customGetVariablesSuccess(data) {
        var variableBrowserContentField = document.getElementById("variable-browser-content");

        // Empty the content field before populating
        variableBrowserContentField.innerHTML = "";

        for (let key in data) {

            const variableEntryElement = document.createElement("div");
            variableEntryElement.classList.add("variable-browser-entry");
            variableBrowserContentField.appendChild(variableEntryElement);

            var variableButtonsBox = document.createElement("div");
            variableButtonsBox.classList.add("variable-buttons-box");
            variableEntryElement.appendChild(variableButtonsBox);
            
            const variableDeleteIcon = document.createElement("button");
            variableDeleteIcon.classList.add("variable-delete-icon");
            variableDeleteIcon.innerHTML = "<i class='fa-solid fa-trash'></i>";
            variableDeleteIcon.addEventListener("click", this.actionDeleteVariable.bind(this, key), false);
            variableButtonsBox.appendChild(variableDeleteIcon);

            const variableNameElement = document.createElement("div");
            variableNameElement.classList.add("variable-entry-name");
            variableNameElement.innerHTML = key;
            variableEntryElement.appendChild(variableNameElement);

            const variableValueElement = document.createElement("div");
            variableValueElement.classList.add("variable-entry-value");
            variableValueElement.innerHTML = data[key];
            variableEntryElement.appendChild(variableValueElement);
        }
    }

    actionDeleteVariable(variableName) {
        this.logger.info("Starting to delete variable: "+variableName);
        this.dialogueBranchClient.callSetVariable(variableName,null);
    }

    customSetVariableSuccess() {
        this.logger.debug("Custom Set Variable Success()");
        this.actionListVariables();
    }

    // ---------- Start Dialogue ----------

    actionStartDialogue(dialogueName) {
        this.logger.info("Starting dialogue '" + dialogueName + "'.");

        var contentBlock = document.getElementById("interaction-tester-content");
        contentBlock.innerHTML = "";
        this.dialogueBranchClient.callStartDialogue(dialogueName,"en");
    }

    actionSelectReply(replyNumber, reply, dialogueStep) {
        // Add a class to the selected reply option, so it can be visualised in the dialogue history which options were chosen
        this._dialogueReplyElements[replyNumber-1].classList.add("user-selected-reply-option");
        this.dialogueBranchClient.callProgressDialogue(dialogueStep.loggedDialogueId, dialogueStep.loggedInteractionIndex, reply.replyId);
    }

    // ----------------------------------------------------------------------
    // -------------------- Handling DLB Client Response --------------------
    // ----------------------------------------------------------------------

    // ---------- Info ----------

    customInfoSuccess() {
        var serverInfo = this.dialogueBranchClient.serverInfo;
        this.logger.info("Connected to Dialogue Branch Web Service v"+serverInfo.serviceVersion+", using protocol version "+serverInfo.protocolVersion+" (build: '"+serverInfo.build+"' running for "+serverInfo.upTime+").");
        this.updateUIState();
    }

    customInfoError(err) {
        this.logger.error("Requesting server info failed with the following result: "+err);
    }

    // ---------- Login ----------

    customLoginSuccess(data) {
        // A successful login attempt results in a data containing a 'user', 'role', and 'token' value
        if('user' in data && 'token' in data) {
            var formRemember = document.getElementById("login-form-remember-box").checked;
            this.logger.info("User '"+data.user+"' with role '"+data.role+"' successfully logged in, and received the following token: "+data.token);
            var loggedInUser = new User(data.user,data.role,data.token);
            this.clientState.user = loggedInUser;
            this.clientState.loggedIn = true;
            
            if(formRemember) {
                DocumentFunctions.setCookie('user.name',data.user,365);
                DocumentFunctions.setCookie('user.authToken',data.token,365);
                DocumentFunctions.setCookie('user.role',data.role,365);

                this.logger.info(
                    "Stored user info in cookie: user.name '" + 
                    DocumentFunctions.getCookie('user.name') + 
                    "', user.role '" + 
                    DocumentFunctions.getCookie('user.role') + 
                    "', user.authToken '" + 
                    DocumentFunctions.getCookie('user.authToken') + 
                    "'."
                );
            }
            
            this.updateUIState();
    
        // Any other result indicates some type of error
        } else {
            this.logger.info("Login attempt failed with errorcode '"+data.code+"' and message '"+data.message+"'.");
            console.log("Login attempt failed with errorcode '"+data.code+"' and message '"+data.message+"'.");

            if(data.code == "INVALID_CREDENTIALS") {
                document.getElementById("login-form-password-field").classList.add("login-form-error-class");
                document.getElementById("login-form-username-field").classList.add("login-form-error-class");
            } else if (data.code == "INVALID_INPUT") {
                const errorMessage = JSON.parse(data.message);
                for(let i=0; i<errorMessage.length; i++) {
                    if(errorMessage[i].field == "user") document.getElementById("login-form-username-field").classList.add("login-form-error-class");
                    if(errorMessage[i].field == "password") document.getElementById("login-form-password-field").classList.add("login-form-error-class");
                }
            }
        }
    }

    customLoginError(err) {
        this.logger.error("Login failed with the following result: "+err);
    }

    // ---------- Validate Authentication ----------

    customAuthValidateSuccess(data) {
        if(data == true) {
            this.clientState.loggedIn = true;
        
        // There is an invalid authToken in cookie, delete all info and assume user logged out
        } else {
            this.clientState.loggedIn = false;
            this.clientState.user = null;
            this.dialogueBranchClient.user = null;
            DocumentFunctions.deleteCookie('user.name');
            DocumentFunctions.deleteCookie('user.authToken');
            DocumentFunctions.deleteCookie('user.role');
        }
        this.updateUIState();
    }

    customAuthValidateError(err) {
        this.logger.error("Validating authentication token failed with the following result: "+err);
    }

    // ---------- Start Dialogue

    customStartDialogueSuccess(data) {
        if('dialogue' in data) {

            // Add the name of the newly started dialogue to the "Interaction Tester" title field
            var titleElement = document.getElementById("interaction-tester-title");
            titleElement.innerHTML = "Interaction Tester <i>(" + data.dialogue + ".dlb)</i>";

            // Enable the "cancel dialogue" button
            var cancelButton = document.getElementById("button-cancel-dialogue");
            cancelButton.addEventListener("click", this.actionCancelDialogue.bind(this, data.loggedDialogueId), false);
            cancelButton.setAttribute('title',"Cancel the current ongoing dialogue.");
            cancelButton.classList.remove("button-cancel-dialogue-disabled");
            
            // Create a DialogueStep object from the received data
            var dialogueStep = this.createDialogueStepObject(data);

            // Render the newly created DialogueStep in the UI
            this.renderDialogueStep(dialogueStep);
        }
    }

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

    customStartDialogueError(err) {
        this.logger.error("Starting dialogue failed with the following result: "+err);
    }

    // ---------- Progress Dialogue

    customProgressDialogueSuccess(data) {
        console.log("customProgressDialogueSuccess");
        console.log(data);
        if('value' in data) {

            // The response is empty, so the dialogue is over.
            if(data.value == null) {
                this.renderDialogueStep(null);
            // There is dialogue, so render the next step.
            } else if('dialogue' in data.value) {
                var dialogueStep = this.createDialogueStepObject(data.value);
                this.renderDialogueStep(dialogueStep);

            // Else, something is wrong.
            } else {
                this.logger.error("The Web Service returned an unexpected response when progressing the dialogue.");
            }
        }
    }

    // -----------------------------------------------------------------
    // -------------------- User Interface Handling --------------------
    // -----------------------------------------------------------------

    updateUIState() {

        // ----- Update Service Info

        var versionInfoBox = document.getElementById("version-info");

        if(this.dialogueBranchClient.serverInfo != null) {
            var serverInfo = this.dialogueBranchClient.serverInfo;
            versionInfoBox.innerHTML = "Connected to Dialogue Branch Web Service v" + serverInfo.serviceVersion;
            if(this.clientState.loggedIn) {
                versionInfoBox.innerHTML += " as user '" + this.clientState.user.name + "'.";
            } else {
                versionInfoBox.innerHTML += ".";
            }
        } else {
            document.getElementById("version-info").innerHTML = "Not connected.";
        }

        this.setDebugConsoleVisibility(this.clientState.debugConsoleVisible);

        if(this.clientState.loggedIn) {
            document.getElementById("navbar").style.display = 'block';
            document.getElementById("dialogue-container").style.display = 'block';
            document.getElementById("login-form").style.display = 'none';
            document.getElementById("dlb-splash-logo").style.display = 'none';
            document.getElementById("dlb-splash-text").style.display = 'none';
            var dialogueListButton = document.getElementById("button-refresh-dialogue-list");
            if(this.clientState.user.role == "admin") {
                dialogueListButton.classList.remove("button-refresh-dialogue-list-disabled");
                dialogueListButton.setAttribute('title',"Refresh the content of the Dialogue Browser.");
            } else {
                dialogueListButton.classList.add("button-refresh-dialogue-list-disabled");
                dialogueListButton.setAttribute('title',"Retrieving a dialogue list is only available for 'admin' users.");
            }

            // Refresh the Dialogue List
            if(this.clientState.user.role == 'admin')
            this.actionRefreshDialogueBrowser(null);

            // Refresh the Variable Browser
            this.actionListVariables();

        } else {
            document.getElementById("navbar").style.display = 'none';
            document.getElementById("dialogue-container").style.display = 'none';
            document.getElementById("dialogue-browser-content").innerHTML = "";
            document.getElementById("interaction-tester-content").innerHTML = "";
            document.getElementById("variable-browser-content").innerHTML = "";
            document.getElementById("dlb-splash-logo").style.display = 'block';
            document.getElementById("dlb-splash-text").style.display = 'block';
            document.getElementById("login-form").style.display = 'block';
        }
    }

    // ---------- Debug Console ----------

    /**
     * Sets the visibility of the Debug Console based on the given parameter 'visible'. If true, the Debug Console
     * will be made visible, and vice versa.
     * @param {boolean} visible 
     */
    setDebugConsoleVisibility(visible) {
        if(visible) {
            document.getElementById("debug-console").style.display = 'inline';
            document.getElementById("toggle-debug-console").style.bottom = '220px';
            document.getElementById("version-info").style.bottom = '220px';
        } else {
            document.getElementById("debug-console").style.display = 'none';
            document.getElementById("toggle-debug-console").style.bottom = '10px';
            document.getElementById("version-info").style.bottom = '10px';
        }
    }

    // ---------- Dialogue Step Rendering ----------

    /**
     * Render a step in the dialogue given the information provided in the given dialogueStep object, or
     * render a "Dialogue Finished" statement if the given dialogueStep is null.
     *
     * @param {DialogueStep} dialogueStep the {@link DialogueStep} object to render, or null
     * @param {String} nullMessage the message explaining why dialogueStep is null.
     */
    renderDialogueStep(dialogueStep, nullMessage) {

        // Remove the previous temporary filler element
        const element = document.getElementById("temp-dialogue-filler");
        if(element != null) element.remove();

        // Remove previous click event listeners
        if(this._dialogueReplyElements.length > 0) {
            for(var i=0; i<this._dialogueReplyElements.length; i++) {
                // Replace the node with a clone, which removes all (bound) event listeners
                this._dialogueReplyElements[i].replaceWith(this._dialogueReplyElements[i].cloneNode(true));
                this._dialogueReplyElements[i].classList.remove("reply-option-with-listener");
            }
            // Finally, empty the set of dialogueReplyElements
            this._dialogueReplyElements = new Array();
        }

        var contentBlock = document.getElementById("interaction-tester-content");
        
        // Create the container element for the Statement
        const statementContainer = document.createElement("div");
        statementContainer.classList.add("dialogue-step-statement-container");
        contentBlock.appendChild(statementContainer);

        if(dialogueStep == null) {
            
            // Create and show a message why the dialogue has ended
            var dialogueOverElement = document.createElement("div");
            dialogueOverElement.classList.add("dialogue-finished-statement");
            if(nullMessage != null) {
                dialogueOverElement.innerHTML = nullMessage;
            } else {
                dialogueOverElement.innerHTML = "Dialogue Finished";
            }
            statementContainer.appendChild(dialogueOverElement);

            // Disable the "cancel dialogue" button
            var cancelButton = document.getElementById("button-cancel-dialogue");
            cancelButton.classList.add("button-cancel-dialogue-disabled");
            cancelButton.setAttribute('title',"You can cancel a dialogue when there is a dialogue in progress.");
            cancelButton.replaceWith(cancelButton.cloneNode(true));
            
            // Create a filler element that fills the "rest" of the scrollable area, to allow a proper
            // scrolling to the top (this element will be removed when rendering the next dialogue step)
            const fillerElement = document.createElement("div");
            fillerElement.setAttribute("id","temp-dialogue-filler");
            fillerElement.classList.add("dialogue-step-filler-element");
            contentBlock.appendChild(fillerElement);

            var contentBlockHeight = contentBlock.getBoundingClientRect().height;
            var statementContainerHeight = statementContainer.getBoundingClientRect().height;
            
            // Set the calculated height of the temporary filler element
            fillerElement.style.height = ((contentBlockHeight - statementContainerHeight) + "px");
            
            // Scroll to the top of scrollable element
            contentBlock.scrollTop = fillerElement.offsetTop;
        } else {
            // Add the speaker to the statement container
            const speakerElement = document.createElement("div");
            speakerElement.classList.add("dialogue-step-speaker");
            speakerElement.innerHTML = dialogueStep.speaker + ":";
            statementContainer.appendChild(speakerElement);

            // Add the statement to the statement container
            const statementElement = document.createElement("div");
            statementElement.classList.add("dialogue-step-statement");
            statementElement.innerHTML = dialogueStep.statement.fullStatement();
            statementContainer.appendChild(statementElement);

            // If there are any reply options
            const replyContainer = document.createElement("div");
            replyContainer.classList.add("dialogue-step-reply-container");
            contentBlock.appendChild(replyContainer);
            if(dialogueStep.replies.length > 0) { 
                
                let replyNumber = 1;

                dialogueStep.replies.forEach(
                    (reply) => {

                        const replyOptionContainer = document.createElement("div");
                        replyOptionContainer.classList.add("dialogue-step-reply-option-container");
                        replyContainer.appendChild(replyOptionContainer);

                        if(reply instanceof AutoForwardReply) {
                            const autoForwardReplyButton = document.createElement("button");
                            autoForwardReplyButton.classList.add("dialogue-step-reply-autoforward");
                            autoForwardReplyButton.classList.add("reply-option-with-listener");
                            if(reply.endsDialogue) {
                                autoForwardReplyButton.innerHTML = "<i class='fa-solid fa-ban'></i> END DIALOGUE";
                            } else {
                                autoForwardReplyButton.innerHTML = "CONTINUE";
                            }
                            autoForwardReplyButton.addEventListener("click", this.actionSelectReply.bind(this, replyNumber, reply, dialogueStep), false);
                            replyOptionContainer.appendChild(autoForwardReplyButton);
                            this._dialogueReplyElements.push(autoForwardReplyButton);
                        } else {
                            const replyOptionNumberElement = document.createElement("div");
                            replyOptionNumberElement.classList.add("dialogue-step-reply-number");
                            replyOptionNumberElement.innerHTML = replyNumber + ": - ";
                            replyOptionContainer.appendChild(replyOptionNumberElement);

                            const replyOptionElement = document.createElement("div");
                            replyOptionElement.classList.add("dialogue-step-reply-basic");
                            replyOptionElement.classList.add("reply-option-with-listener");
                            if(reply.endsDialogue) {
                                replyOptionElement.innerHTML = "<i class='fa-solid fa-ban'></i> " + reply.statement;
                            } else {
                                replyOptionElement.innerHTML = reply.statement;
                            }
                            replyOptionElement.addEventListener("click", this.actionSelectReply.bind(this, replyNumber, reply, dialogueStep), false);
                            replyOptionContainer.appendChild(replyOptionElement);
                            this._dialogueReplyElements.push(replyOptionElement);
                            
                        }
                        replyNumber++;
                    }
                    
                );
            } else {
                // In case there are no reply options, add the "Dialogue over" message
                replyContainer.innerHTML = "The dialogue is over.";

            }
            // Create a spacer element between different dialogue steps (this one stays, so there 
            // will always be some space between different dialogue steps).
            const spacerElement = document.createElement("div");
            spacerElement.classList.add("dialogue-step-spacer-element");
            contentBlock.appendChild(spacerElement);

            // Create a filler element that fills the "rest" of the scrollable area, to allow a proper
            // scrolling to the top (this element will be removed when rendering the next dialogue step)
            const fillerElement = document.createElement("div");
            fillerElement.setAttribute("id","temp-dialogue-filler");
            fillerElement.classList.add("dialogue-step-filler-element");
            contentBlock.appendChild(fillerElement);

            contentBlockHeight = contentBlock.getBoundingClientRect().height;
            statementContainerHeight = statementContainer.getBoundingClientRect().height;
            var replyContainerHeight = replyContainer.getBoundingClientRect().height;
            var spacerElementHeight = spacerElement.getBoundingClientRect().height;
            
            // Set the calculated height of the temporary filler element
            fillerElement.style.height = ((contentBlockHeight - statementContainerHeight - replyContainerHeight - spacerElementHeight) + "px");
            
            // Scroll to the top of scrollable element
            contentBlock.scrollTop = fillerElement.offsetTop;

            // Refresh the Variable Browser
            this.actionListVariables();
        }

    }

}