class BasicReply extends Reply {

    constructor(replyId, endsDialogue, actions, statement) {
        super(replyId, endsDialogue, actions);
        this._statement = statement;
    }

    static emptyInstance() {
        return new BasicReply(null, null, new Array(), null);
    }

    set statement(statement) {
        this._statement = statement;
    }

    get statement() {
        return this._statement;
    }

}