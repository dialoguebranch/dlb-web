/**
 * Performs a call to the /info/all end-point. 
 */
function callInfo() {
    const infoUrl = 'http://localhost:8080/dlb-web-service/v1/info/all';

    fetch(infoUrl, {
        method: "GET",
        headers: {
            Accept: "application/json, text/plain, */*",
            "Content-Type": "application/json",
        }
    })
    .then((response) => response.json())
    .then((data) => { 
        infoSuccess(data);
    })
    .catch((err) => {
        infoError(err);
    });
}

/**
 * Performs a call to the /auth/login end-point. A successfull call will result in a call to the loginSuccess() function, while
 * an error will result in a call to loginError(). Note that "success" does not necessarily mean that the authentication was
 * successful. Providing an invalid username/password combination is deemed a "success", but will result in an error message 
 * being delivered to loginSuccess().
 * 
 * @param {String} user The username of the Dialogue Branch Web Service user.
 * @param {String} password The password corresponding to the user.
 * @param {Number} tokenExpiration The time (in minutes) after which the authentication token should expire.
 *                                 This can be set to '0' or 'never' if the token should never expire.
 */
function callLogin(user, password, tokenExpiration) {
    const loginUrl = 'http://localhost:8080/dlb-web-service/v1/auth/login';

    fetch(loginUrl, {
        method: "POST",
        headers: {
            Accept: "application/json, text/plain, */*",
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            user: user,
            password: password,
            tokenExpiration: tokenExpiration
        }),
    })
    .then((response) => response.json())
    .then((data) => { 
        loginSuccess(data);
    })
    .catch((err) => {
        loginError(err);
    });

}