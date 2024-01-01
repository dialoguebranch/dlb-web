const LOG_LEVEL_INFO = 0;
const LOG_LEVEL_DEBUG = 1;

class DialogueBranchClient {

    // ------------------------------------
    // ---------- Constructor(s) ----------
    // ------------------------------------

    constructor(baseUrl) {
        this._baseUrl = baseUrl;
    }

    // ---------------------------------------
    // ---------- Getters & Setters ----------
    // ---------------------------------------

    get serverInfo() {
        return this._serverInfo;
    }

    get baseUrl() {
        return this._baseUrl;
    }

    get user() {
        return this._user;
    }

    set user(user) {
        this._user = user;
    }

    // ------------------------------------------
    // ---------- End-Point: /info/all ----------
    // ------------------------------------------

    /**
     * Performs a call to the /info/all end-point. A successfull call will result in a call to the
     * this.infoSuccess() function, while an error will result in a call to this.infoError(). Both
     * of these functions will perform basic actions (see their individual documentation) and then
     * call the 'customInfoSuccess' and 'customInfoError' functions respectively that you may define
     * yourself to perform additional actions. 
     *
     * The /info/all end-point returns information about the running web service, including:
     *  - Service Version - the software version number of the Web Service
     *  - Protocol Version - the latest protocol version supported (e.g. '1')
     *  - Build - the specific build-info string
     *  - Uptime - how long this web service has been running for
     *
     * @param {String} user The username of the Dialogue Branch Web Service user.
     * @param {String} password The password corresponding to the user.
     * @param {Number} tokenExpiration The time (in minutes) after which the authentication token
     *                                 should expire. This can be set to '0' or 'never' if the token
     *                                 should never expire.
     */
    callInfo() {
        console.log("DLB-CLIENT: Calling /info/all/ end-point.");
        const infoUrl = 'http://localhost:8080/dlb-web-service/v1/info/all';

        fetch(infoUrl, {
            method: "GET",
            headers: {
                Accept: "application/json, text/plain, */*",
                "Content-Type": "application/json",
            }
        })
        .then((response) => response.json())
        .then((data) => { 
            this.infoSuccess(data);
        })
        .catch((err) => {
            this.infoError(err);
        });
    }

    /**
     * This function is called upon a succesfull call to the callInfo() function. The data object
     * will include the web service's response, which is an object that contains the
     * 'serviceVersion', 'protocolVersion', 'build', and 'upTime' information of the web service.
     * This information is stored locally (see the get serverInfo() function), after which the
     * 'customInfoSuccess(data)' function is called.
     * 
     * @param {Object} data 
     */
    infoSuccess(data) {
        if('build' in data) {
            this._serverInfo = new ServerInfo(data.serviceVersion, data.protocolVersion, data.build, data.upTime);
        }
        customInfoSuccess(data);
    }

    infoError(err) {
        console.log("DLB-CLIENT: Handling error after call to /info/all end-point.");
        customInfoError(err);
    }

    // --------------------------------------------
    // ---------- End-Point: /auth/login ----------
    // --------------------------------------------

    /**
     * Performs a call to the /auth/login end-point. A successfull call will result in a call to the
     * this.loginSuccess() function, while an error will result in a call to this.loginError(). Both
     * of these functions will perform basic actions (see their individual documentation) and then
     * call the 'customLoginSuccess' and 'customLoginError' functions respectively that you may
     * define yourself to perform additional actions. 
     *
     * Note that "success" does not necessarily mean that the authentication was successful.
     * Providing an invalid username/password combination is deemed a "success", but will result in
     * an error message being delivered to loginSuccess().
     *
     * @param {String} user The username of the Dialogue Branch Web Service user.
     * @param {String} password The password corresponding to the user.
     * @param {Number} tokenExpiration The time (in minutes) after which the authentication token
     *                                 should expire. This can be set to '0' or 'never' if the token
     *                                 should never expire.
     */
    callLogin(user, password, tokenExpiration) {
        const loginUrl = 'http://localhost:8080/dlb-web-service/v1/auth/login';

        fetch(loginUrl, {
            method: "POST",
            headers: {
                Accept: "application/json, text/plain, */*",
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                user: user,
                password: password,
                tokenExpiration: tokenExpiration
            }),
        })
        .then((response) => response.json())
        .then((data) => { 
            this.loginSuccess(data);
        })
        .catch((err) => {
            this.loginError(err);
        });

    }

    loginSuccess(data) {
        if('user' in data && 'token' in data) {    
            this._user = new User(data.user,data.role,data.token);
        }
        customLoginSuccess(data);
    }

    loginError(err) {
        customLoginError(err);
    }

    // -----------------------------------------------
    // ---------- End-Point: /auth/validate ----------
    // -----------------------------------------------

    /**
     * Performs a call to the /auth/validate end-point. 
     */
    callAuthValidate(tokenToValidate) {
        var url = this._baseUrl + "/auth/validate";

        fetch(url, {
            method: "POST",
            headers: {
                'X-Auth-Token': tokenToValidate,
                Accept: "application/json, text/plain, */*",
                "Content-Type": "application/json",
            }
        })
        .then((response) => response.json())
        .then((data) => { 
            this.authValidateSuccess(data);
        })
        .catch((err) => {
            this.authValidateError(err);
        });
    }

    authValidateSuccess(data) {
        console.log("DLB-Client: calling auth validate success.");
        console.log(data);
        if(data == true) {
            console.log("validated!");
        }
        customAuthValidateSuccess(data);
    }

    authValidateError(err) {
        console.log("DLB-Client: calling auth validate error.");
    }

    // ------------------------------------------------------
    // ---------- End-Point: /admin/list-dialogues ----------
    // ------------------------------------------------------

    callListDialogues() {
        var url = this._baseUrl + "/admin/list-dialogues";

        fetch(url, {
            method: "GET",
            headers: {
                'X-Auth-Token': this._user.authToken,
                Accept: "application/json, text/plain, */*",
                "Content-Type": "application/json",
            }
        })
        .then((response) => response.json())
        .then((data) => { 
            this.listDialoguesSuccess(data);
        })
        .catch((err) => {
            this.listDialoguesError(err);
        });
    }

    listDialoguesSuccess(data) {
        console.log(data);
    }

    listDialoguesError(err) {
        console.log(err);
    }

}