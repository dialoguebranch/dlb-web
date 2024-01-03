class AutoForwardReply extends Reply {

    constructor(replyId, endsDialogue, actions) {
        super(replyId, endsDialogue, actions);
    }

    static emptyInstance() {
        return new AutoForwardReply(null, null, new Array());
    }


}