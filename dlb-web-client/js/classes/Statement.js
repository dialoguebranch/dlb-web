class Statement {

    // ------------------------------------
    // ---------- Constructor(s) ----------
    // ------------------------------------

    constructor(segments) {
        this._segments = segments;
    }

    static emptyInstance() {
        return new Statement(new Array());
    }

    // ---------------------------------------
    // ---------- Getters & Setters ----------
    // ---------------------------------------

    set segments(segments) {
        this._segments = segments;
    }

    get segments() {
        return this._segments;
    }

    // -----------------------------------
    // ---------- Other Methods ----------
    // -----------------------------------

    addSegment(segment) {
        this._segments.push(segment);
    }

    fullStatement() {
        var resultStatement = "";
        this._segments.forEach(
            (element) => {
                if(element.type == "TEXT") {
                    resultStatement += element.text;
                }
            }
        );
        return resultStatement;
    }

    toString() {
        var result = "";
        result += this.fullStatement();
        return result;
    }

}