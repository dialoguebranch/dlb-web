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

import { User } from './User.js';
import { DocumentFunctions } from './DocumentFunctions.js';

/**
 * A ClientState object models the state of the Dialogue Branch Web Client.
 * It should be passed a reference to a custom Logger object so that it may
 * log its actions to the client's custom debug console.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
export class ClientState {

    // ------------------------------------
    // ---------- Constructor(s) ----------
    // ------------------------------------

    /**
     * Creates an instance of a ClientState object to keep track of the state of the web client app.
     * Log information is passed through the provided Logger instance.
     * @param {Logger} logger A Logger instance that may be used to log information.
     */
    constructor(logger) {
        this._logger = logger;
        this._variableBrowserExtended = false;
        this._dialogueBrowserExtended = false;
    }

    // ---------------------------------------
    // ---------- Getters & Setters ----------
    // ---------------------------------------

    /**
     * Sets whether or not a user is currently logged in to the client app.
     * @param {boolean} loggedIn whether or not a user is currently logged in to the client app.
     */
    set loggedIn(loggedIn) {
        this._loggedIn = loggedIn;
    }

    /**
     * Returns whether or not a user is currently logged in to the client app.
     * @returns whether or not a user is currently logged in to the client app.
     */
    get loggedIn() {
        return this._loggedIn;
    }

    /**
     * Sets the User object, representing the user information of a logged in user.
     * @param {User} user the User object, representing the user information of a logged in user.
     */
    set user(user) {
        this._user = user;   
    }

    /**
     * Returns the User object, representing the user information of a logged in user.
     * @returns the User object, representing the user information of a logged in user.
     */
    get user() {
        return this._user;
    }

    // ----- ServerInfo

    /**
     * Sets the ServerInfo object, containing information about the server to which the client is connected.
     * @param {ServerInfo} serverInfo the ServerInfo object, containing information about the server to which the client is connected.
     */
    set serverInfo(serverInfo) {
        this._serverInfo = serverInfo;   
    }

    /**
     * Returns the ServerInfo object, containing information about the server to which the client is connected.
     * @returns the ServerInfo object, containing information about the server to which the client is connected.
     */
    get serverInfo() {
        return this._serverInfo;
    }

    // ----- debugConsoleVisible

    /**
     * Sets a boolean value indicating whether or not the debug console is currently visible.
     * @param {boolean} debugConsoleVisible a boolean value indicating whether or not the debug console is currently visible.
     */
    set debugConsoleVisible(debugConsoleVisible) {
        this._debugConsoleVisible = debugConsoleVisible;
        DocumentFunctions.setCookie('state.debugConsoleVisible', this._debugConsoleVisible, 365);
    }

    /**
     * Returns a boolean value indicating whether or not the debug console is currently visible.
     * @returns a boolean value indicating whether or not the debug console is currently visible.
     */
    get debugConsoleVisible() {
        return this._debugConsoleVisible;
    }

    /**
     * Sets a boolean value indicating whether the Variable Browser is currently extended or not.
     * @param {Boolean} variableBrowserExtended - true if the Variable Browser is currently extended, false otherwise.
     */
    set variableBrowserExtended(variableBrowserExtended) {
        this._variableBrowserExtended = variableBrowserExtended;
        DocumentFunctions.setCookie('state.variableBrowserExtended', this._variableBrowserExtended, 365);
    }

    /**
     * Returns a boolean value indicating whether the Variable Browser is currently extended or not.
     * @returns a boolean value indicating whether the Variable Browser is currently extended or not.
     */
    get variableBrowserExtended() {
        return this._variableBrowserExtended;
    }

    /**
     * Sets a boolean value indicating whether the Dialogue Browser is currently extended or not.
     * @param {Boolean} dialogueBrowserExtended - true if the Dialogue Browser is currently extended, false otherwise.
     */
    set dialogueBrowserExtended(dialogueBrowserExtended) {
        this._dialogueBrowserExtended = dialogueBrowserExtended;
        DocumentFunctions.setCookie('state.dialogueBrowserExtended', this._dialogueBrowserExtended, 365);
    }

    /**
     * Returns a boolean value indicating whether the Dialogue Browser is currently extended or not.
     * @returns a boolean value indicating whether the Dialogue Browser is currently extended or not.
     */
    get dialogueBrowserExtended() {
        return this._dialogueBrowserExtended;
    }

    // -----------------------------------
    // ---------- Other Methods ----------
    // -----------------------------------

    /**
     * Loads information about the ClientState from a cookie, if set.
     */
    loadFromCookie() {
        var cookieValue = DocumentFunctions.getCookie('state.debugConsoleVisible');
        if(cookieValue == "true") this._debugConsoleVisible = true;
        else this._debugConsoleVisible = false;

        cookieValue = DocumentFunctions.getCookie('state.variableBrowserExtended');
        if(cookieValue == "true") this._variableBrowserExtended = true;
        else this._variableBrowserExtended = false;

        cookieValue = DocumentFunctions.getCookie('state.dialogueBrowserExtended');
        if(cookieValue == "true") this._dialogueBrowserExtended = true;
        else this._dialogueBrowserExtended = false;

        var cookieUserName = DocumentFunctions.getCookie('user.name');
        var cookieUserRole = DocumentFunctions.getCookie('user.role');
        var cookieUserAuthToken = DocumentFunctions.getCookie('user.authToken');

        // All variables are non-empty / non-null
        if(cookieUserName && cookieUserRole && cookieUserAuthToken) {
            var user = new User(cookieUserName, cookieUserRole, cookieUserAuthToken);
            this._user = user;
        }
    }

}
