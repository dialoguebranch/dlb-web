class Logger {

    constructor() {
        this._logArea = document.getElementById("debug-textarea");
    }

    set logLevel(logLevel) {
        this._logLevel = logLevel;
    }

    info(line) {
        this._logArea.value += "\n" + "INFO: " + line;
        this.scrollToBottom();
    }

    error(line) {
        this._logArea.value += "\n" + "ERROR: " + line;
        this.scrollToBottom();
    }

    debug(line) {
        if(this._logLevel >= LOG_LEVEL_DEBUG) {
            this._logArea.value += "\n" + "DEBUG: " + line;
            this.scrollToBottom();
        }
    }

    scrollToBottom() {
        this._logArea.scrollTop = this._logArea.scrollHeight;
    }

}