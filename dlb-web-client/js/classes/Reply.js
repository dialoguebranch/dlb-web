class Reply {

    // ------------------------------------
    // ---------- Constructor(s) ----------
    // ------------------------------------

    constructor(replyId, endsDialogue, actions) {
        this._replyId = replyId;
        this._endsDialogue = endsDialogue;
        this._actions = actions;
    }

    // ---------------------------------------
    // ---------- Getters & Setters ----------
    // ---------------------------------------

    set replyId(replyId) {
        this._replyId = replyId;
    }

    get replyId() {
        return this._replyId;
    }

    set endsDialogue(endsDialogue) {
        this._endsDialogue = endsDialogue;
    }

    get endsDialogue() {
        return this._endsDialogue;
    }

    set actions(actions) {
        this._actions = actions;
    }

    get actions() {
        return this._actions;
    }

    // -----------------------------------
    // ---------- Other Methods ----------
    // -----------------------------------

    toString() {
        var result = "";
        result += "\n\t\t{replyId: " + this._replyId;
        result += "\n\t\tendsDialogue: " + this._endsDialogue;
        result += "\n\t\tactions: " + this._actions +"}";
        return result;
    }

}