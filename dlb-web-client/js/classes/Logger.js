class Logger {

    constructor() {
        this._logArea = document.getElementById("debug-textarea");
    }

    set logLevel(logLevel) {
        this._logLevel = logLevel;
    }

    info(line) {
        this._logArea.value += "\n" + "INFO: " + line;
    }

    debug(line) {
        if(this._logLevel >= LOG_LEVEL_DEBUG) {
            this._logArea.value += "\n" + "DEBUG: " + line;
        }
    }

}