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

import {ServerInfo} from './ServerInfo.js';
import {User} from './User.js';

/** 
 * The following constants may be used throughout the web app for logging purposes.
 */
export const LOG_LEVEL_INFO = 0;
export const LOG_LEVEL_DEBUG = 1;
export const LOG_LEVEL_NAMES = [
    "INFO",
    "DEBUG"
];

export class DialogueBranchClient {

    // ------------------------------------
    // ---------- Constructor(s) ----------
    // ------------------------------------

    constructor(baseUrl, logger, dialogueBranchController) {
        this._baseUrl = baseUrl;
        this._logger = logger;
        this.dialogueBranchController = dialogueBranchController;
        // eslint-disable-next-line no-undef
        this._timeZone = new Intl.DateTimeFormat().resolvedOptions().timeZone;
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

    get timeZone() {
        return this._timeZone;
    }

    get user() {
        return this._user;
    }

    set user(user) {
        this._user = user;
    }

    get logger() {
        return this._logger;
    }

    // -----------------------------------------------------------------------------
    // ---------- 1. Authentication: End-points related to Authentication ----------
    // -----------------------------------------------------------------------------

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
        const loginUrl = this._baseUrl + "/auth/login";

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
        this.dialogueBranchController.customLoginSuccess(data);
    }

    loginError(err) {
        this.dialogueBranchController.customLoginError(err);
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
        this.dialogueBranchController.customAuthValidateSuccess(data);
    }

    authValidateError(err) {
        console.log("DLB-Client: calling auth validate error.");
        this.dialogueBranchController.customAuthValidateError(err);
    }

    // -----------------------------------------------------------------------------------------------------------------------
    // ---------- 2. Dialogue: End-points for starting and controlling the lifecycle of remotely executed dialogues. ----------
    // ------------------------------------------------------------------------------------------------------------------------

    // ------------------------------------------------
    // ---------- End-Point: /dialogue/start ----------
    // ------------------------------------------------

    callStartDialogue(dialogueName, language) {
        var url = this._baseUrl + "/dialogue/start";

        url += "?dialogueName="+dialogueName;
        url += "&language="+language;
        url += "&timeZone="+this._timeZone;

        fetch(url, {
            method: "POST",
            headers: {
                'X-Auth-Token': this._user.authToken,
                Accept: "application/json, text/plain, */*",
                "Content-Type": "application/json",
            }
        })
        .then((response) => response.json())
        .then((data) => { 
            this.startDialogueSuccess(data);
        })
        .catch((err) => {
            this.startDialogueError(err);
        });
    }

    startDialogueSuccess(data) {
        console.log(data);
        this.dialogueBranchController.customStartDialogueSuccess(data);
    }

    startDialogueError(err) {
        console.log(err)
        this.dialogueBranchController.customStartDialogueError(err);
    }

    // ---------------------------------------------------
    // ---------- End-Point: /dialogue/progress ----------
    // ---------------------------------------------------

    callProgressDialogue(loggedDialogueId, loggedInteractionIndex, replyId) {
        var url = this._baseUrl + "/dialogue/progress";

        url += "?loggedDialogueId="+loggedDialogueId;
        url += "&loggedInteractionIndex="+loggedInteractionIndex;
        url += "&replyId="+replyId;

        console.log("callProgressDialogue: "+url);

        fetch(url, {
            method: "POST",
            headers: {
                'X-Auth-Token': this._user.authToken,
                Accept: "application/json, text/plain, */*",
                "Content-Type": "application/json",
            }
        })
        .then((response) => response.json())
        .then((data) => { 
            this.progressDialogueSuccess(data);
        })
        .catch((err) => {
            this.progressDialogueError(err);
        });

    }

    progressDialogueSuccess(data) {
        this.dialogueBranchController.customProgressDialogueSuccess(data);
    }

    progressDialogueError(err) {
        console.log(err)
    }

    // -------------------------------------------------
    // ---------- End-Point: /dialogue/cancel ----------
    // -------------------------------------------------

    callCancelDialogue(loggedDialogueId) {
        var url = this._baseUrl + "/dialogue/cancel";

        url += "?loggedDialogueId="+loggedDialogueId;

        console.log("callCancelDialogue: "+url);

        fetch(url, {
            method: "POST",
            headers: {
                'X-Auth-Token': this._user.authToken,
                Accept: "application/json, text/plain, */*",
                "Content-Type": "application/json",
            }
        })
        .then((response) => {
            if(response.ok) {
                return;
            }
            return Promise.reject(response);
        })
        .then(() => { 
            this.cancelDialogueSuccess();
        })
        .catch((err) => {
            this.cancelDialogueError(err);
        });

    }

    cancelDialogueSuccess() {
        this.dialogueBranchController.customCancelDialogueSuccess();
    }

    cancelDialogueError(err) {
        console.log(err)
    }

    // -------------------------------------------------------------------------------------------------
    // ---------- 3. Variables: End-points for retrieving or setting DialogueBranch Variables. ----------
    // --------------------------------------------------------------------------------------------------

    // ---------------------------------------------------------
    // ---------- End-Point: /variables/get-variables ----------
    // ---------------------------------------------------------

    callGetVariables() {
        var url = this._baseUrl + "/variables/get-variables";

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
            this.getVariablesSuccess(data);
        })
        .catch((err) => {
            this.getVariablesError(err);
        });

    }

    getVariablesSuccess(data) {
        this.dialogueBranchController.customGetVariablesSuccess(data);
    }

    getVariablesError(err) {
        console.log(err)
    }

    // --------------------------------------------------------
    // ---------- End-Point: /variables/set-variable ----------
    // --------------------------------------------------------

    callSetVariable(variableName, variableValue) {
        var url = this._baseUrl + "/variables/set-variable";

        url += "?name="+variableName;
        if(variableValue != null) url += "&value="+variableValue;
        url += "&timeZone="+this._timeZone;

        console.log("Calling /variables/set-variable/ :"+url);

        fetch(url, {
            method: "POST",
            headers: {
                'X-Auth-Token': this._user.authToken,
                Accept: "application/json, text/plain, */*",
                "Content-Type": "application/json",
            }
        })
        .then((response) => {
            if(response.ok) {
                return;
            }
            return Promise.reject(response);
        })
        .then(() => { 
            this.setVariableSuccess();
        })
        .catch((err) => {
            this.setVariableError(err);
        });

    }

    setVariableSuccess() {
        console.log("setVariableSuccess");
        this.dialogueBranchController.customSetVariableSuccess();
    }

    setVariableError(err) {
        console.log("setVariableError");
        console.log(err)
    }

    // ---------------------------------------------------------------------------------------------------
    // ---------- 5. Information: End-points that provide information about the running service ----------
    // ---------------------------------------------------------------------------------------------------

    // --------------------------------------------
    // ---------- End-Point: /info/all ----------
    // --------------------------------------------

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
        this.dialogueBranchController.customInfoSuccess(data);
    }

    infoError(err) {
        console.log("DLB-CLIENT: Handling error after call to /info/all end-point.");
        this.dialogueBranchController.customInfoError(err);
    }

     // --------------------------------------------------------------------------------------------------------
    // ---------- 6. Admin: End-points for administrative control of the Dialogue Branch Web Service. ----------
    // ---------------------------------------------------------------------------------------------------------

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

        if(data == null) {
            // A null response is unexpected, but should not break the client
            this.logger.warn("DBC: Call to /admin/list-dialogues returned null response.");
            this.dialogueBranchController.customListDialoguesSuccess(new Array());
        } else {
            if('dialogueNames' in data) {
                this.dialogueBranchController.customListDialoguesSuccess(data.dialogueNames);
            } else {
                // Data without dialogueNames is unexpected, but should not break the client
                this.logger.warn("DBC: Call to /admin/list-dialogues returned unexpected response.");
                this.dialogueBranchController.customListDialoguesSuccess(new Array());
            }
        }
    }

    listDialoguesError(err) {
        console.log(err);
        this.dialogueBranchController.customListDialoguesError(err);
    }

}