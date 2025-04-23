/* @license
 *
 *                Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *                                        as outlined below.
 *
 *                                            ----------
 *
 * Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

export class DialogueBranchClient {
    constructor(baseUrl) {
        this._baseUrl = baseUrl;
    }

    // -----------------------------------------------------------------------------
    // ---------- 1. Authentication: End-points related to Authentication ----------
    // -----------------------------------------------------------------------------

    // --------------------------------------------
    // ---------- End-Point: /auth/login ----------
    // --------------------------------------------

    /**
     * Performs a call to the /auth/login end-point. A successfull call will result in a call to the
     * this.loginSuccess() function, while an error will result in a call to this.loginError(). Both
     * of these functions will perform basic actions (see their individual documentation) and then
     * call the 'customLoginSuccess' and 'customLoginError' functions respectively that you may
     * define yourself to perform additional actions. 
     *
     * Note that "success" does not necessarily mean that the authentication was successful.
     * Providing an invalid username/password combination is deemed a "success", but will result in
     * an error message being delivered to loginSuccess().
     *
     * @param {String} user The username of the Dialogue Branch Web Service user.
     * @param {String} password The password corresponding to the user.
     * @param {Number} tokenExpiration The time (in minutes) after which the authentication token
     *                                 should expire. This can be set to '0' or 'never' if the token
     *                                 should never expire.
     */
    login(user, password, tokenExpiration) {
        const loginUrl = this._baseUrl + "/auth/login";

        return fetch(loginUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                user: user,
                password: password,
                tokenExpiration: tokenExpiration
            }),
        })
        .then((response) => {
            if(response.ok) {
                return response.json();
            }
            return Promise.reject(response);
        });
    }
}
