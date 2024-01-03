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
 * A ClientState object models the state of the Dialogue Branch Web Client.
 * It should be passed a reference to a custom Logger object so that it may
 * log its actions to the client's custom debug console.
 *
 * Author: Harm op den Akker (Fruit Tree Labs)
 */
class ClientState {

    constructor(logger) {
        this._logger = logger;
    }

    // ----- loggedIn

    /**
     * @param {boolean} loggedIn 
     */
    set loggedIn(loggedIn) {
        this._loggedIn = loggedIn;
        this._logger.debug("ClientState updated: loggedIn = "+loggedIn);
    }

    get loggedIn() {
        return this._loggedIn;
    }

    // ----- User

    /**
     * @param {User} user
     */
    set user(user) {
        this._user = user;   
    }

    get user() {
        return this._user;
    }

    // ----- ServerInfo

    /**
     * @param {ServerInfo} serverInfo
     */
    set serverInfo(serverInfo) {
        this._serverInfo = serverInfo;   
    }

    get serverInfo() {
        return this._serverInfo;
    }

    // ----- debugConsoleVisible

    /**
     * @param {boolean} debugConsoleVisible
     */
    set debugConsoleVisible(debugConsoleVisible) {
        this._debugConsoleVisible = debugConsoleVisible;
        setCookie('state.debugConsoleVisible', this._debugConsoleVisible, 365);
        this._logger.debug("ClientState updated: debugConsoleVisible = " + debugConsoleVisible);
    }

    get debugConsoleVisible() {
        return this._debugConsoleVisible;
    }

    // ----- Initialize from Cookie

    loadFromCookie() {
        var cookieValue = getCookie('state.debugConsoleVisible');
        if(cookieValue == "true") this._debugConsoleVisible = true;
        else this._debugConsoleVisible = false;

        var cookieUserName = getCookie('user.name');
        var cookieUserRole = getCookie('user.role');
        var cookieUserAuthToken = getCookie('user.authToken');

        // All variables are non-empty / non-null
        if(cookieUserName && cookieUserRole && cookieUserAuthToken) {
            var user = new User(cookieUserName, cookieUserRole, cookieUserAuthToken);
            this._user = user;
            this._loggedIn = true;
        }
    }

}