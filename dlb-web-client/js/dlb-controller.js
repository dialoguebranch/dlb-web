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

    document.getElementById("menu-bar-list-dialogues").addEventListener("click", (e)=> {
        actionListDialogues();
    });

    // Initialize the logger
    this.logger = new Logger();
    this.logger.logLevel = LOG_LEVEL_DEBUG;

    this.dialogueBranchClient = new DialogueBranchClient("http://localhost:8080/dlb-web-service/v1");

    // Initialize the ClientState object and take actions
    this.clientState = new ClientState(this.logger);
    this.clientState.loadFromCookie();

    // If user info was loaded from Cookie
    if(this.clientState.user != null) {
        validateAuthToken(this.clientState.user.authToken);
    }

    // Make a call to the Web Service for service info.
    this.dialogueBranchClient.callInfo();

    updateUIState();
};

// -----------------------------------------------------------
// -------------------- DLB Client Access --------------------
// -----------------------------------------------------------

// ---------- Info ----------

function customInfoSuccess() {
    serverInfo = this.dialogueBranchClient.serverInfo;
    this.logger.info("Connected to Dialogue Branch Web Service v"+serverInfo.serviceVersion+", using protocol version "+serverInfo.protocolVersion+" (build: '"+serverInfo.build+"' running for "+serverInfo.upTime+").");
    updateUIState();
}

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

function customLoginSuccess(data) {
    // A successful login attempt results in a data containing a 'user', 'role', and 'token' value
    if('user' in data && 'token' in data) {
        var formRemember = document.getElementById("login-form-remember-box").checked;
        this.logger.info("User '"+data.user+"' with role '"+data.role+"' successfully logged in, and received the following token: "+data.token);
        loggedInUser = new User(data.user,data.role,data.authToken);
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

function customLoginError(data) {
    console.log("loginError called with the following data: ");
    console.log(data);
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
        document.getElementById("menu-bar").style.display = 'block';
        document.getElementById("login-form").style.display = 'none';
    } else {
        document.getElementById("menu-bar").style.display = 'none';
        document.getElementById("login-form").style.display = 'block';
    }
}


// ---------- Debug Console ----------

/**
 * Toggle the visibility of the Debug Console. If it was visible before, make it invisible, and the other way
 * around. Store the new state in the ClientState.
 * @param {*} event 
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

// ---------- Logout ----------

function actionLogout() {
    this.logger.info("Logging out user.");
    deleteCookie('user.name');
    deleteCookie('user.authToken');
    deleteCookie('user.role');

    this.clientState.user = null;
    this.clientState.loggedIn = false;
    updateUIState();
}

// ---------- List Dialogues ----------

function actionListDialogues() {

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