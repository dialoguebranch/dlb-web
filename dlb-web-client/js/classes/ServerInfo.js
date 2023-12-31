class ServerInfo {

    constructor(serviceVersion, protocolVersion, build, upTime) {
        this._serviceVersion = serviceVersion;
        this._protocolVersion = protocolVersion;
        this._build = build;
        this._upTime = upTime;
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

}