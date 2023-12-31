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

    /**
     * @param {User} user
     */
    set user(user) {
        this._user = user;   
    }

    get user() {
        return this._user;
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

    // ----- serviceVersion

    set serviceVersion(serviceVersion) {
        this._serviceVersion = serviceVersion;
        this._logger.debug("ClientState updated: serviceVersion = " + serviceVersion);
    }

    get serviceVersion() {
        return this._serviceVersion;
    }

    // ----- protocolVersion

    set protocolVersion(protocolVersion) {
        this._protocolVersion = protocolVersion;
        this._logger.debug("ClientState updated: protocolVersion = " + protocolVersion);
    }

    get protocolVersion() {
        return this._protocolVersion;
    }

    // ----- build

    set build(build) {
        this._build = build;
        this._logger.debug("ClientState updated: build = " + build);
    }

    get build() {
        return this._build;
    }

    // ----- upTime

    set upTime(upTime) {
        this._upTime = upTime;
        this._logger.debug("ClientState updated: upTime = " + upTime);
    }

    get upTime() {
        return this._upTime;
    }

    // ----- Initialize from Cookie

    loadFromCookie() {
        var cookieValue = getCookie('state.debugConsoleVisible');
        if(cookieValue == "true") this._debugConsoleVisible = true;
        else this._debugConsoleVisible = false;
    }

}