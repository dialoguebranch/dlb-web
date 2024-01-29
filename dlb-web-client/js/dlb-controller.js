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

/**
 * This anonymous function contains all the necessary "bindings" of UI to script, as well as any 
 * function calls that need to be executed after the page has finished loading.
 */
window.onload = function() {

    document.getElementById("login-button").addEventListener("click", (e)=> {
        actionLogin(e);
    });

    document.getElementById("toggle-debug-console").addEventListener("click", (e)=> {
        actionToggleDebugConsole();
    });

    document.getElementById("menu-bar-logout").addEventListener("click", (e)=> {
        actionLogout();
    });

    document.getElementById("button-refresh-dialogue-list").addEventListener("click", (e)=> {
        actionListDialogues();
    });

    // Initialize the logger
    this.logger = new Logger();
    this.logger.logLevel = LOG_LEVEL_DEBUG;
    this.logger.info("Initialized Logger with log level '" + dialogueBranchConfig.logLevel + "'.");

    this.dialogueBranchClient = new DialogueBranchClient(dialogueBranchConfig.baseUrl);
    this.logger.info("Initalized DialogueBranchClient for base url '"+ dialogueBranchConfig.baseUrl + "'.");

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

    updateUIState();
};

// ------------------------------------------------------
// -------------------- User Actions --------------------
// ------------------------------------------------------

// ---------- Login ----------

function actionLogin(event) {
    event.preventDefault();

    // Remove any possible previous error indications
    document.getElementById("login-form-password-field").classList.remove("login-form-error-class");
    document.getElementById("login-form-username-field").classList.remove("login-form-error-class");

    var formUsername = document.getElementById("login-form-username-field").value;
    var formPassword = document.getElementById("login-form-password-field").value;

    this.dialogueBranchClient.callLogin(formUsername, formPassword, 0);
}

// ---------- Logout ----------

function actionLogout() {
    this.logger.info("Logging out user.");
    deleteCookie('user.name');
    deleteCookie('user.authToken');
    deleteCookie('user.role');

    this.clientState.user = null;
    this.clientState.loggedIn = false;
    this.dialogueBranchClient.user = null;
    updateUIState();
}

// ---------- Toggle Debug Console ----------

/**
 * Toggle the visibility of the Debug Console. If it was visible before, make it invisible, and the other way
 * around. Store the new state in the ClientState.
 */
function actionToggleDebugConsole() {
    if(this.clientState.debugConsoleVisible) {
        setDebugConsoleVisibility(false);
        this.clientState.debugConsoleVisible = false;
    } else {
        setDebugConsoleVisibility(true);
        this.clientState.debugConsoleVisible = true;
    }
}

// ---------- List Dialogues ----------

function actionListDialogues() {
    this.logger.info("Refreshing the Dialogue Browser.");
    this.dialogueBranchClient.callListDialogues();
}

// ---------- Start Dialogue ----------

function actionStartDialogue(dialogueName) {
    this.logger.info("Starting dialogue '" + dialogueName + "'.");

    var contentBlock = document.getElementById("interaction-tester-content");
    contentBlock.innerHTML = "";
    this.dialogueBranchClient.callStartDialogue(dialogueName,"en");
}

function actionSelectReply(replyNumber, reply, dialogueStep) {
    this.logger.info("Selected reply number "+replyNumber);
    this.logger.info("Which boils down to this reply: "+reply);

    this.logger.info("replyId: "+reply.replyId);

    this.dialogueBranchClient.callProgressDialogue(dialogueStep.loggedDialogueId, dialogueStep.loggedInteractionIndex, reply.replyId);
}

// ----------------------------------------------------------------------
// -------------------- Handling DLB Client Response --------------------
// ----------------------------------------------------------------------

// ---------- Info ----------

function customInfoSuccess() {
    serverInfo = this.dialogueBranchClient.serverInfo;
    this.logger.info("Connected to Dialogue Branch Web Service v"+serverInfo.serviceVersion+", using protocol version "+serverInfo.protocolVersion+" (build: '"+serverInfo.build+"' running for "+serverInfo.upTime+").");
    updateUIState();
}

function customInfoError(err) {
    this.logger.error("Requesting server info failed with the following result: "+err);
}

// ---------- Login ----------

function customLoginSuccess(data) {
    // A successful login attempt results in a data containing a 'user', 'role', and 'token' value
    if('user' in data && 'token' in data) {
        var formRemember = document.getElementById("login-form-remember-box").checked;
        this.logger.info("User '"+data.user+"' with role '"+data.role+"' successfully logged in, and received the following token: "+data.token);
        loggedInUser = new User(data.user,data.role,data.token);
        this.clientState.user = loggedInUser;
        this.clientState.loggedIn = true;
        
        if(formRemember) {
            setCookie('user.name',data.user,365);
            setCookie('user.authToken',data.token,365);
            setCookie('user.role',data.role,365);

            this.logger.info("Stored user info in cookie: user.name '"+getCookie('user.name')+"', user.role '"+getCookie('user.role')+"', user.authToken '"+getCookie('user.authToken')+"'.");
        }
        
        updateUIState();
   
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

function customLoginError(err) {
    this.logger.error("Login failed with the following result: "+err);
}

// ---------- Validate Authentication ----------

function customAuthValidateSuccess(data) {
    if(data == true) {
        this.clientState.loggedIn = true;
    
    // There is an invalid authToken in cookie, delete all info and assume user logged out
    } else {
        this.clientState.loggedIn = false;
        this.clientState.user = null;
        this.dialogueBranchClient.user = null;
        deleteCookie('user.name');
        deleteCookie('user.authToken');
        deleteCookie('user.role');
    }
    updateUIState();
}

function customAuthValidateError(err) {
    this.logger.error("Validating authentication token failed with the following result: "+err);
}

// ---------- List Dialogues ----------

function customListDialoguesSuccess(data) {
    var dialogueBrowserContentField = document.getElementById("dialogue-browser-content");

    // Empty the content field before populating
    dialogueBrowserContentField.innerHTML = "";
    
    if('dialogueNames' in data) {

        for(var i=0; i< data.dialogueNames.length; i++) {
            if(i != 0) dialogueBrowserContentField.innerHTML += "<br/>";
            dialogueBrowserContentField.innerHTML += "<span class=\"dialogue-browser-entry\">" 
                + "<a id=\"myLink\" title=\"Click to do start Dialogue\" href=\"#\" onclick=\"actionStartDialogue('" 
                + data.dialogueNames[i] 
                + "');return false;\">" 
                + data.dialogueNames[i] 
                + "</a></span>";
        }
    }
}

function customListDialoguesError(err) {
    this.logger.error("Retrieving dialogue list failed with the following result: "+err);
}

// ---------- Start Dialogue

function customStartDialogueSuccess(data) {
    if('dialogue' in data) {

        dialogueStep = createDialogueStepObject(data);

        this.logger.debug(dialogueStep.toString());
        renderDialogueStep(dialogueStep);
    }
}

function createDialogueStepObject(data) {
     // Instantiate an empty DialogueStep
     dialogueStep = DialogueStep.emptyInstance();

     // Add the simple parameters
     dialogueStep.dialogueName = data.dialogue;
     dialogueStep.node = data.node;
     dialogueStep.speaker = data.speaker;
     dialogueStep.loggedDialogueId = data.loggedDialogueId;
     dialogueStep.loggedInteractionIndex = data.loggedInteractionIndex;

     // Add the statement (consisting of a list of segments)
     statement = Statement.emptyInstance();
     data.statement.segments.forEach(
         (element) => {
             segment = new Segment(element.segmentType,element.text);
             statement.addSegment(segment);
         }
     );
     dialogueStep.statement = statement;

     // Add the replies
     data.replies.forEach(
         (element) => {
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
                         segment = new Segment(segmentElement.segmentType,segmentElement.text);
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

function customStartDialogueError(err) {
    this.logger.error("Starting dialogue failed with the following result: "+err);
}

// ---------- Progress Dialogue

function customProgressDialogueSuccess(data) {
    logger.debug("Progressing dialogue.");
    console.log(data);
    if('value' in data) {

        if('dialogue' in data.value) {

            logger.debug("Yes there is some 'dialogue' in this data.");

            dialogueStep = createDialogueStepObject(data.value);

            this.logger.debug(dialogueStep.toString());
            renderDialogueStep(dialogueStep);
        }
    }
}

// -----------------------------------------------------------------
// -------------------- User Interface Handling --------------------
// -----------------------------------------------------------------

function updateUIState() {

    // ----- Update Service Info

    versionInfoBox = document.getElementById("version-info");

    if(this.dialogueBranchClient.serverInfo != null) {
        serverInfo = this.dialogueBranchClient.serverInfo;
        versionInfoBox.innerHTML = "Connected to Dialogue Branch Web Service v" + serverInfo.serviceVersion;
        if(this.clientState.loggedIn) {
            versionInfoBox.innerHTML += " as user '" + this.clientState.user.name + "'.";
        } else {
            versionInfoBox.innerHTML += ".";
        }
    } else {
        document.getElementById("version-info").innerHTML = "Not connected.";
    }

    setDebugConsoleVisibility(this.clientState.debugConsoleVisible);

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
function setDebugConsoleVisibility(visible) {
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

function renderDialogueStep(dialogueStep) {

    var contentBlock = document.getElementById("interaction-tester-content");

    // Create the container element for the Statement
    const statementContainer = document.createElement("div");
    statementContainer.classList.add("dialogue-step-statement-container");
    contentBlock.appendChild(statementContainer);

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
    if(dialogueStep.replies.length > 0) {
        const replyContainer = document.createElement("div");
        replyContainer.classList.add("dialogue-step-reply-container");
        contentBlock.appendChild(replyContainer);

        let replyNumber = 1;

        dialogueStep.replies.forEach(
            (reply) => {

                const replyOptionContainer = document.createElement("div");
                replyOptionContainer.classList.add("dialogue-step-reply-option-container");
                replyContainer.appendChild(replyOptionContainer);
                
                const replyOptionNumberElement = document.createElement("div");
                replyOptionNumberElement.classList.add("dialogue-step-reply-number");
                replyOptionNumberElement.innerHTML = replyNumber + ": - ";
                replyOptionContainer.appendChild(replyOptionNumberElement);

                if(reply instanceof AutoForwardReply) {
                    const replyOptionElement = document.createElement("div");
                    replyOptionElement.classList.add("dialogue-step-reply-autoforward");
                    replyOptionElement.innerHTML = "AUTOFORWARD";
                    replyOptionContainer.appendChild(replyOptionElement);
                } else {
                    const replyOptionElement = document.createElement("div");
                    replyOptionElement.classList.add("dialogue-step-reply-basic");
                    replyOptionElement.innerHTML = reply.statement;
                    replyOptionElement.addEventListener("click", actionSelectReply.bind(this, replyNumber, reply, dialogueStep), false);
                    replyOptionContainer.appendChild(replyOptionElement);
                }
                replyNumber++;
            }
            
        );

    }

}

// -----------------------------------------------------------
// -------------------- Utility Functions --------------------
// -----------------------------------------------------------

// ---------- Cookie Storing / Loading ----------

function setCookie(cname, cvalue, exdays) {
    const d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    let expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

function deleteCookie(cname) {
    setCookie(cname,"",0);
}
  
function getCookie(cname) {
    let name = cname + "=";
    let ca = document.cookie.split(';');
    for(let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}