class Segment {

    // ------------------------------------
    // ---------- Constructor(s) ----------
    // ------------------------------------

    constructor(type, text) {
        this._type = type;
        this._text = text;
    }

    // ---------------------------------------
    // ---------- Getters & Setters ----------
    // ---------------------------------------

    set type(type) {
        this._type = type;
    }

    get type() {
        return this._type;
    }

    set text(text) {
        this._text = text;
    }

    get text() {
        return this._text;
    }

}