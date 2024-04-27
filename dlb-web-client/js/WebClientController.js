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

import { ClientState } from './dialoguebranch-lib/ClientState.js';
import { TextAreaLogger } from './dialoguebranch-lib/util/TextAreaLogger.js';
import { LOG_LEVEL_NAMES } from './dialoguebranch-lib/util/AbstractLogger.js';
import { AutoForwardReply } from './dialoguebranch-lib/model/AutoForwardReply.js';
import { DocumentFunctions } from './dialoguebranch-lib/util/DocumentFunctions.js';
import { AbstractController } from './dialoguebranch-lib/AbstractController.js';
import { DialogueBranchConfig } from './dialoguebranch-lib/DialogueBranchConfig.js';
import { DialogueBranchClient } from './dialoguebranch-lib/DialogueBranchClient.js';

export class WebClientController extends AbstractController {

    // ------------------------------------
    // ---------- Constructor(s) ----------
    // ------------------------------------

    constructor() {
        super();

        this._LOGTAG = "WebClientController";

        this._dialogueReplyElements = new Array();
        this._dialogueReplyNumbers = new Array();

        // Initialize the Configuration and Logger objects
        this._dialogueBranchConfig = new DialogueBranchConfig(1,'http://localhost:8080/dlb-web-service/v1');
        this._logger = new TextAreaLogger(this._dialogueBranchConfig.logLevel, document.getElementById("debug-textarea"));
        this._logger.info(this._LOGTAG,"Initialized Logger with log level '" 
            + this._dialogueBranchConfig.logLevel 
            + "' ('" 
            + LOG_LEVEL_NAMES[this._dialogueBranchConfig.logLevel] 
            + "').");

        // Initialize the DialogueBranchClient object, used for communication to the Dialogue Branch Web Service
        this._dialogueBranchClient = new DialogueBranchClient(this._dialogueBranchConfig.baseUrl, this._logger, this);
        this._logger.info(this._LOGTAG,"Initalized DialogueBranchClient directed to the Web Service at '"+ this._dialogueBranchConfig.baseUrl + "'.");

        // Initialize the ClientState object and take actions
        this._clientState = new ClientState(this._logger);
        this._clientState.loadFromCookie();

        // If user info was loaded from Cookie, validate the authToken that was found
        if(this._clientState.user != null) {
            this._logger.info(this._LOGTAG, "Existing user info found in cookie - username: " + this._clientState.user.name + ", role: " + this._clientState.user.role);
            this._dialogueBranchClient.user = this._clientState.user;
            this._dialogueBranchClient.callAuthValidate(this._clientState.user.authToken);
        }
        
        // Make a call to the Web Service for service info.
        this._dialogueBranchClient.callInfo();

        // Update the UI State
        this.updateUIState();

        // Update the size of the interaction tester / variable/dialogue browsers
        this.updateInteractionTesterSize();
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

        this._dialogueBranchClient.callLogin(formUsername, formPassword, 0);
    }

    // ---------- Logout ----------

    actionLogout(event) {
        event.preventDefault();

        this._logger.info(this._LOGTAG,"Logging out user '" + this._clientState.user.name + "'.");
        DocumentFunctions.deleteCookie('user.name');
        DocumentFunctions.deleteCookie('user.authToken');
        DocumentFunctions.deleteCookie('user.role');

        this._clientState.user = null;
        this._clientState.loggedIn = false;
        this._dialogueBranchClient.user = null;
        this.updateUIState();
    }

    // ---------- Toggle Debug Console ----------

    /**
     * Toggle the visibility of the Debug Console. If it was visible before, make it invisible, and the other way
     * around. Store the new state in the ClientState.
     */
    actionToggleDebugConsole(event) {
        event.preventDefault();

        if(this._clientState.debugConsoleVisible) {
            this.setDebugConsoleVisibility(false);
            this._clientState.debugConsoleVisible = false;
        } else {
            this.setDebugConsoleVisibility(true);
            this._clientState.debugConsoleVisible = true;
        }
    }

    // ------------------------------------------------------------
    // -------------------- Interaction Tester --------------------
    // ------------------------------------------------------------

    actionCancelDialogue(loggedDialogueId) {
        this._logger.info(this._LOGTAG,"Cancelling the current dialogue with loggedDialogueId: '"+loggedDialogueId+"'.");
        this._dialogueBranchClient.callCancelDialogue(loggedDialogueId);
    }

    handleCancelDialogue() {
        this.renderDialogueStep(null, "Dialogue Cancelled");
    }

    handleCancelDialogueError(errorMessage) {
        this._logger.error(this._LOGTAG,"Cancelling dialogue failed with the following result: "+errorMessage);
    }

    // ----------------------------------------------------------
    // -------------------- Dialogue Browser --------------------
    // ----------------------------------------------------------

    actionRefreshDialogueBrowser(event) {
        if(event != null) event.preventDefault();
        this._dialogueBranchClient.callListDialogues();
    }

    actionResizeDialogueBrowser() {

        if(this._clientState.dialogueBrowserExtended) {
            this._clientState.dialogueBrowserExtended = false;
        } else {
            this._clientState.dialogueBrowserExtended = true;
        }
        
        this.updateInteractionTesterSize();
    }

    handleListDialogues(dialogueNames) {
        var dialogueBrowserContentField = document.getElementById("dialogue-browser-content");

        // Empty the content field before (re-)populating
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

        this._logger.info(this._LOGTAG,"Updated the contents of the Dialogue Browser, showing "+dialogueNames.length+" available dialogues.");
    }

    handleListDialoguesError(errorMessage) {
        this._logger.error(this._LOGTAG,errorMessage);
    }

    // ----------------------------------------------------------
    // -------------------- Variable Browser --------------------
    // ----------------------------------------------------------

    actionRefreshVariableBrowser() {
        this._dialogueBranchClient.callGetVariables();
    }

    actionResizeVariableBrowser() {
        if(this._clientState.variableBrowserExtended) {
            this._clientState.variableBrowserExtended = false;
        } else {
            this._clientState.variableBrowserExtended = true;
        }
        this.updateInteractionTesterSize();
    }

    handleGetVariables(variables) {
        var variableBrowserContentField = document.getElementById("variable-browser-content");

        // Empty the content field before populating
        variableBrowserContentField.innerHTML = "";

        // Add some headers to the Variables 'table'
        var headerElement = document.createElement("div");
        headerElement.classList.add("variable-browser-entry");
        headerElement.classList.add("variable-browser-header");
        variableBrowserContentField.appendChild(headerElement);

        var variableButtonsLabel = document.createElement("div");
        variableButtonsLabel.classList.add("variable-buttons-box");
        variableButtonsLabel.innerHTML = "Actions";
        headerElement.appendChild(variableButtonsLabel);

        var variableNameLabel = document.createElement("div");
        variableNameLabel.classList.add("variable-entry-name");
        variableNameLabel.innerHTML = "Name";
        headerElement.appendChild(variableNameLabel);

        var variableValueLabel = document.createElement("div");
        variableValueLabel.classList.add("variable-entry-value");
        variableValueLabel.innerHTML = "Value";
        headerElement.appendChild(variableValueLabel);

        var variableUpdatedLabel = document.createElement("div");
        variableUpdatedLabel.classList.add("variable-entry-value");
        variableUpdatedLabel.innerHTML = "Last Updated";
        headerElement.appendChild(variableUpdatedLabel);

        variables.forEach(variable => {
            var variableEntryElement = document.createElement("div");
            variableEntryElement.classList.add("variable-browser-entry");
            variableBrowserContentField.appendChild(variableEntryElement);

            var variableButtonsBox = document.createElement("div");
            variableButtonsBox.classList.add("variable-buttons-box");
            variableEntryElement.appendChild(variableButtonsBox);
            
            var variableDeleteIcon = document.createElement("button");
            variableDeleteIcon.classList.add("variable-delete-icon");
            variableDeleteIcon.innerHTML = "<i class='fa-solid fa-trash'></i>";
            variableDeleteIcon.addEventListener("click", this.actionDeleteVariable.bind(this, variable.name), false);
            variableButtonsBox.appendChild(variableDeleteIcon);

            var variableNameElement = document.createElement("div");
            variableNameElement.classList.add("variable-entry-name");
            variableNameElement.innerHTML = variable.name;
            variableEntryElement.appendChild(variableNameElement);

            var variableValueElement = document.createElement("div");
            variableValueElement.classList.add("variable-entry-value");
            variableValueElement.innerHTML = variable.value;
            variableEntryElement.appendChild(variableValueElement);

            var variableUpdatedElement = document.createElement("div");
            variableUpdatedElement.classList.add("variable-entry-updated");
            variableUpdatedElement.innerHTML = variable.getReadableTimeSinceLastUpdate();
            variableEntryElement.appendChild(variableUpdatedElement);
        });

        this._logger.info(this._LOGTAG,"Updated the contents of the Variable Browser, showing "+variables.length+" available variables.");

    }

    handleGetVariablesError(errorMessage) {
        this._logger.error(this._LOGTAG,errorMessage);
    }

    actionDeleteVariable(variableName) {
        this._logger.info(this._LOGTAG,"Requesting to delete variable: '" + variableName + "'.");
        this._dialogueBranchClient.callSetVariable(variableName,null);
    }

    handleSetVariable(variableName) {
        this._logger.info(this._LOGTAG,"Succesfully updated variable '" + variableName + "'.");
        this.actionRefreshVariableBrowser();
    }

    handleSetVariableError(variableName, errorMessage) {
        this._logger.error(this._LOGTAG,"Error updating variable '"+variableName+"', with the following message: " + errorMessage);
    }

    // ---------- Start Dialogue ----------

    actionStartDialogue(dialogueName) {
        this._logger.info(this._LOGTAG, "Starting dialogue '" + dialogueName + "'.");

        var contentBlock = document.getElementById("interaction-tester-content");
        contentBlock.innerHTML = "";
        this._dialogueBranchClient.callStartDialogue(dialogueName,"en");
    }

    actionSelectReply(replyNumber, reply, dialogueStep) {
        // Add a class to the selected reply option, so it can be visualised in the dialogue history which options were chosen
        this._dialogueReplyElements[replyNumber-1].classList.add("user-selected-reply-option");
        this._dialogueReplyNumbers[replyNumber-1].classList.add("user-selected-reply-option");
        this._dialogueBranchClient.callProgressDialogue(dialogueStep.loggedDialogueId, dialogueStep.loggedInteractionIndex, reply.replyId);
    }

    // ----------------------------------------------------------------------
    // -------------------- Handling DLB Client Response --------------------
    // ----------------------------------------------------------------------

    // ---------- Info ----------

    handleServerInfo(serverInfo) {
        this._logger.info(this._LOGTAG,"Connected to Dialogue Branch Web Service v"+serverInfo.serviceVersion+", using protocol version "+serverInfo.protocolVersion+" (build: '"+serverInfo.build+"' running for "+serverInfo.upTime+").");
        this.updateServerInfoBox();
    }

    handleServerInfoError(errorMessage) {
        this._logger.error(this._LOGTAG,errorMessage);
    }

    // ---------- Login ----------

    handleLoginSuccess(user) {
        var formRemember = document.getElementById("login-form-remember-box").checked;
        this._clientState.user = user;
        this._clientState.loggedIn = true;
            
        if(formRemember) {
            DocumentFunctions.setCookie('user.name',this._clientState.user.name,365);
            DocumentFunctions.setCookie('user.authToken',this._clientState.user.authToken,365);
            DocumentFunctions.setCookie('user.role',this._clientState.user.role,365);

            this._logger.debug(this._LOGTAG,
                "Stored user info in cookie: user.name '" + 
                DocumentFunctions.getCookie('user.name') + 
                "', user.role '" + 
                DocumentFunctions.getCookie('user.role') + 
                "', user.authToken '" + 
                DocumentFunctions.getCookie('user.authToken') + 
                "'."
            );
        }

        // Clear the login error message box
        var errorMessageBox = document.getElementById("login-error-message-box");
        errorMessageBox.innerHTML = '';
        errorMessageBox.style.display = 'none';

        this._logger.info(this._LOGTAG,"User '"+this._clientState.user.name+"' successfully logged in with role '"+this._clientState.user.role+"'.");
        
        this.updateServerInfoBox();
        this.updateUIState();
    }

    handleLoginError(httpStatusCode, errorCode, errorMessage, fieldErrors) {
        
        // Show the error message box
        var errorMessageBox = document.getElementById("login-error-message-box");
        errorMessageBox.style.display = 'block';
        errorMessageBox.innerHTML = errorMessage;
        
        if(errorCode == "INVALID_CREDENTIALS") {
            document.getElementById("login-form-password-field").classList.add("login-form-error-class");
            document.getElementById("login-form-username-field").classList.add("login-form-error-class");
        } else if (errorCode == "INVALID_INPUT") {
            for(let i=0; i<fieldErrors.length; i++) {
                if(fieldErrors[i].field == "user") document.getElementById("login-form-username-field").classList.add("login-form-error-class");
                if(fieldErrors[i].field == "password") document.getElementById("login-form-password-field").classList.add("login-form-error-class");
            }
        }
    }

    // ---------- Validate Authentication ----------

    handleAuthValidate(valid, message) {
        
        // All is well, mark user as logged in and proceed
        if(valid == true) {
            this._clientState.loggedIn = true;
        
        // There is an invalid authToken in cookie, delete all info and assume user is logged out
        } else {
            this._logger.warn(this._LOGTAG,"Unable to validate stored authentication token: '" + message + "'. Requiring new login.");
            this._clientState.loggedIn = false;
            this._clientState.user = null;
            this._dialogueBranchClient.user = null;
            DocumentFunctions.deleteCookie('user.name');
            DocumentFunctions.deleteCookie('user.authToken');
            DocumentFunctions.deleteCookie('user.role');
        }

        // Finally, update the User Interface State
        this.updateUIState();
    }
    
    // ---------- Start Dialogue

    handleStartDialogue(dialogueStep) {
        // Add the name of the newly started dialogue to the "Interaction Tester" title field
        var titleElement = document.getElementById("interaction-tester-title");
        titleElement.innerHTML = "Interaction Tester <i>(" + dialogueStep.dialogueName + ".dlb)</i>";

        // Enable the "cancel dialogue" button
        var cancelButton = document.getElementById("button-cancel-dialogue");
        cancelButton.addEventListener("click", this.actionCancelDialogue.bind(this, dialogueStep.loggedDialogueId), false);
        cancelButton.setAttribute('title',"Cancel the current ongoing dialogue.");
        cancelButton.classList.remove("button-cancel-dialogue-disabled");

        // Render the newly created DialogueStep in the UI
        this.renderDialogueStep(dialogueStep);
    }

    handleStartDialogueError(errorMessage) {
        this._logger.error(this._LOGTAG,"Starting dialogue failed with the following result: "+errorMessage);
    }

    // ---------- Progress Dialogue

    handleProgressDialogue(dialogueContinues, dialogueStep) {
        if(dialogueContinues) {
            this.renderDialogueStep(dialogueStep);
        } else {
            this.renderDialogueStep(null);
        }
    }

    handleProgressDialogueError(errorMessage) {
        this._logger.error(this._LOGTAG,errorMessage);
    }

    // -----------------------------------------------------------------
    // -------------------- User Interface Handling --------------------
    // -----------------------------------------------------------------

    /**
     * Updates the text displayed in the "server info box", which may changed when a new call to the
     * service info end-point is done, or when a new user is logged in.
     */
    updateServerInfoBox() {

        var versionInfoBox = document.getElementById("version-info");
        var versionInfoString = "Not connected.";

        if(this._dialogueBranchClient.serverInfo != null) {
            var serverInfo = this._dialogueBranchClient.serverInfo;
            versionInfoString = "Connected to Dialogue Branch Web Service v" + serverInfo.serviceVersion;
            if(this._clientState.loggedIn) {
                versionInfoString += " as user '" + this._clientState.user.name +"'";
                if(this._clientState.user.role == 'admin') {
                    versionInfoString += " (with admin rights).";
                } else {
                    versionInfoString += ".";
                }
            }
        }
        versionInfoBox.innerHTML = versionInfoString;
    }

    updateUIState() {

        this.setDebugConsoleVisibility(this._clientState.debugConsoleVisible);

        if(this._clientState.loggedIn) {
            document.getElementById("navbar").style.display = 'block';
            document.getElementById("dialogue-container").style.display = 'block';
            document.getElementById("login-form").style.display = 'none';
            document.getElementById("dlb-splash-logo").style.display = 'none';
            document.getElementById("dlb-splash-text").style.display = 'none';
            var dialogueListButton = document.getElementById("button-refresh-dialogue-list");
            if(this._clientState.user.role == "admin") {
                dialogueListButton.classList.remove("button-refresh-dialogue-list-disabled");
                dialogueListButton.setAttribute('title',"Refresh the content of the Dialogue Browser.");
            } else {
                dialogueListButton.classList.add("button-refresh-dialogue-list-disabled");
                dialogueListButton.setAttribute('title',"Retrieving a dialogue list is only available for 'admin' users.");
            }

            // Refresh the Dialogue List
            if(this._clientState.user.role == 'admin')
            this.actionRefreshDialogueBrowser(null);

            // Refresh the Variable Browser
            this.actionRefreshVariableBrowser();

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

    updateInteractionTesterSize() {

        //<button id="button-resize-variable-list" class="circle-button-small" title="Resize the contents of the Variable Browser."><i class="fa-solid fa-caret-left"></i></button>
        var resizeDialogueBrowserButton = document.getElementById("button-resize-dialogue-list");
        var resizeVariableBrowserButton = document.getElementById("button-resize-variable-list");

        var dialogueBrowserContainer = document.getElementById("dialogue-browser-container");
        var variableBrowserContainer = document.getElementById("variable-browser-container");
        var interactionTesterContainer = document.getElementById("interaction-tester-container");


        var sideBarsExtended = 0;

        if(this._clientState.dialogueBrowserExtended) {
            sideBarsExtended++;
            resizeDialogueBrowserButton.innerHTML = "<i class=\"fa-solid fa-caret-left\"></i>";
            dialogueBrowserContainer.classList.remove("browser-size-default");
            dialogueBrowserContainer.classList.add("browser-size-extended");

        } else {
            resizeDialogueBrowserButton.innerHTML = "<i class=\"fa-solid fa-caret-right\"></i>";
            dialogueBrowserContainer.classList.remove("browser-size-extended");
            dialogueBrowserContainer.classList.add("browser-size-default");
        }

        if(this._clientState.variableBrowserExtended) {
            sideBarsExtended++;
            resizeVariableBrowserButton.innerHTML = "<i class=\"fa-solid fa-caret-right\"></i>";
            variableBrowserContainer.classList.remove("browser-size-default");
            variableBrowserContainer.classList.add("browser-size-extended");
        } else {
            resizeVariableBrowserButton.innerHTML = "<i class=\"fa-solid fa-caret-left\"></i>";
            variableBrowserContainer.classList.remove("browser-size-extended");
            variableBrowserContainer.classList.add("browser-size-default");
        }

        if(sideBarsExtended == 0) {
            interactionTesterContainer.classList.remove("tester-size-small");
            interactionTesterContainer.classList.remove("tester-size-smaller");
            interactionTesterContainer.classList.add("tester-size-default");
        } else if(sideBarsExtended == 1) {
            interactionTesterContainer.classList.remove("tester-size-smaller");
            interactionTesterContainer.classList.remove("tester-size-default");
            interactionTesterContainer.classList.add("tester-size-small");
        } else {
            interactionTesterContainer.classList.remove("tester-size-small");
            interactionTesterContainer.classList.remove("tester-size-default");
            interactionTesterContainer.classList.add("tester-size-smaller");
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
                this._dialogueReplyElements[i].classList.remove("reply-option-with-listener");
                this._dialogueReplyElements[i].replaceWith(this._dialogueReplyElements[i].cloneNode(true));
            }
            // Finally, empty the set of dialogueReplyElements
            this._dialogueReplyElements = new Array();
            this._dialogueReplyNumbers = new Array();
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
                            // Add an empty element to the list, so that adding the 'user-selected-reply' class won't break.
                            this._dialogueReplyNumbers.push(document.createElement("div"));
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
                            this._dialogueReplyNumbers.push(replyOptionNumberElement);
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
            this.actionRefreshVariableBrowser();
        }

    }

}
