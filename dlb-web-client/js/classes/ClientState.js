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
            user = new User(cookieUserName, cookieUserRole, cookieUserAuthToken);
            this._user = user;
        }
    }

}