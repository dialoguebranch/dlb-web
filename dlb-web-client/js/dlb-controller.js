/**
 * This anonymous function contains all the necessary "bindings" of UI to script, as well as any 
 * function calls that need to be executed after the page has finished loading.
 */
window.onload = function() {

    document.getElementById("login-button").addEventListener("click", (e)=> {
        loginEventHandler(e);
    });

    document.getElementById("toggle-debug-console").addEventListener("click", (e)=> {
        toggleDebugConsole();
    });

    // Initialize the logger
    this.logger = new Logger();
    this.logger.logLevel = LOG_LEVEL_DEBUG;

    // Initialize the ClientState object and take actions
    this.clientState = new ClientState(this.logger);
    this.clientState.loadFromCookie();
    

    // Make a call to the Web Service for service info.
    callInfo();

    updateUIState();
};

// -----------------------------------------------------------
// -------------------- DLB Client Access --------------------
// -----------------------------------------------------------

// ---------- Info ----------

function infoSuccess(data) {
    if('build' in data) {
        this.clientState.serviceVersion = data.serviceVersion;
        this.clientState.protocolVersion = data.protocolVersion;
        this.clientState.build = data.build;
        this.clientState.upTime = data.upTime;
        this.logger.info("Connected to Dialogue Branch Web Service v"+data.serviceVersion+", using protocol version "+data.protocolVersion+" (build: '"+data.build+"' running for "+data.upTime+").");
        updateUIState();
    }
}

function infoError(data) {

}

// ---------- Login ----------

function loginEventHandler(event) {
    event.preventDefault();

    // Remove any possible previous error indications
    document.getElementById("login-form-password-field").classList.remove("login-form-error-class");
    document.getElementById("login-form-username-field").classList.remove("login-form-error-class");

    var formUsername = document.getElementById("login-form-username-field").value;
    var formPassword = document.getElementById("login-form-password-field").value;

    callLogin(formUsername, formPassword, 0);
}

function loginSuccess(data) {
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
        
        document.getElementById("login-form").style.display = 'none';
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

function loginError(data) {
    console.log("loginError called with the following data: ");
    console.log(data);
}

// -----------------------------------------------------------------
// -------------------- User Interface Handling --------------------
// -----------------------------------------------------------------

function updateUIState() {

    // ----- Update Service Info

    versionInfoBox = document.getElementById("version-info");

    if(this.clientState.serviceVersion != null) {
        versionInfoBox.innerHTML = "Connected to Dialogue Branch Web Service v" + this.clientState.serviceVersion;
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
    } else {
        document.getElementById("menu-bar").style.display = 'none';
    }
}


// ---------- Debug Console ----------

/**
 * Toggle the visibility of the Debug Console. If it was visible before, make it invisible, and the other way
 * around. Store the new state in the ClientState.
 * @param {*} event 
 */
function toggleDebugConsole() {
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