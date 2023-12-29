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

    /**
     * @param {boolean} debugConsoleVisible
     */
    set debugConsoleVisible(debugConsoleVisible) {
        this._debugConsoleVisible = debugConsoleVisible;
        setCookie('state.debugConsoleVisible',this._debugConsoleVisible,365);
        this._logger.debug("ClientState updated: debugConsoleVisible = "+debugConsoleVisible);
    }

    get debugConsoleVisible() {
        return this._debugConsoleVisible;
    }

    loadFromCookie() {
        var cookieValue = getCookie('state.debugConsoleVisible');
        if(cookieValue == "true") this._debugConsoleVisible = true;
        else this._debugConsoleVisible = false;
    }

}