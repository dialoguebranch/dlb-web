class User {

    constructor(name, role, authToken) {
        this._name = name;
        this._role = role;
        this._authToken = authToken;
    }

    get name() {
        return this._name;
    }

    get role() {
        return this._role;
    }

    get authToken() {
        return this._authToken;
    }

}