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

/* eslint-disable no-unused-vars */

/**
 * An AbstractController is an "abstract" class that contains all the methods that should be
 * overridden by a a real implementation of a "Dialogue Branch User Interface Controller". A
 * DialogueBranchClient, which is used for all the communication to a Dialogue Branch Web Service
 * API is instantiated by passing an instance of an implementation of this "abstract class". Its
 * methods will be called as a result of API calls to the Web Service.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
export class AbstractController {

    /**
     * This constructor should not be used, as AbstractController is an "abstract" class.
     */
    constructor() {
        if (this.constructor == AbstractController) {
            throw new Error("Abstract class AbstractController can not be instantiated.");
        }
    }

    /**
     * Called after a successful call to the /auth/login end-point by the DialogueBranchClient.
     */
    handleLoginSuccess(user) {
        throw new Error("Method 'customLoginSuccess()' must be implemented by a subclass.");
    }
    
    /**
     * Called after a failed call to the /auth/login end-point by the DialogueBranchClient.
     */
    handleLoginError(err) {
        throw new Error("Method 'customLoginSuccess()' must be implemented by a subclass.");
    }

    customAuthValidateSuccess(data) {

    }

    customAuthValidateError(err) {

    }


}