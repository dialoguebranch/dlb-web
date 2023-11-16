/**
 * This anonymous function contains all the necessary "bindings" of UI to script, as well as any 
 * function calls that need to be executed after the page has finished loading.
 */
window.onload = function() {

    document.getElementById("login-button").addEventListener("click", (e)=> {
        loginEventHandler(e);
    });

    document.getElementById("toggle-debug-console").addEventListener("click", (e)=> {
        toggleDebugConsole(e);
    });

    callInfo();
};

// ---------- Info ----------

function infoSuccess(data) {
    if('build' in data) {
        logToDebugConsole("Connected to Dialogue Branch Web Service v"+data.serviceVersion+", using protocol version "+data.protocolVersion+" (build: '"+data.build+"' running for "+data.upTime+").");
    }
}

function infoError(data) {

}

// ---------- Debug Console ----------

var debugConsoleVisible = true;

function toggleDebugConsole(event) {
    if(debugConsoleVisible) {
        document.getElementById("debug-console").style.display = 'none';
        document.getElementById("toggle-debug-console").style.bottom = '10px';
        debugConsoleVisible = false;
    } else {
        document.getElementById("debug-console").style.display = 'inline';
        document.getElementById("toggle-debug-console").style.bottom = '220px';
        debugConsoleVisible = true;
    }
}

/**
 * Adds a given line of text to end of the debug console text area.
 * @param {String} line the line of text to add to the debug console textarea.
 */
function logToDebugConsole(line) {
    document.getElementById("debug-textarea").value += "\n" + line;
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
    // A successful login attempt results in a data containing a 'user' and 'token' value
    if('user' in data && 'token' in data) {
        logToDebugConsole("User '"+data.user+"' with role '"+data.role+"' successfully logged in, and received the following token: "+data.token);
        document.getElementById("login-form").style.display = 'none';
   
    // Any other result indicates some type of error
    } else {
        logToDebugConsole("Login attempt failed with errorcode '"+data.code+"' and message '"+data.message+"'.");
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