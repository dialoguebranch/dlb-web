/*
 *
 *                Copyright (c) 2023-2025 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *      as outlined below. Based on original source code licensed under the following terms:
 *
 *                                            ----------
 *
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
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

package com.dialoguebranch.web.varservice.exception;

import com.dialoguebranch.web.varservice.controller.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * This exception results in a HTTP response with status 404 Not Found. The exception message
 * (default "Not Found") will be written to the response. It is handled by the {@link
 * ErrorController}.
 *
 * @author Dennis Hofs (Roessingh Research and Development)
 * @author Harm op den Akker (Fruit Tree Labs)
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND)
public class NotFoundException extends HttpException {

	@Serial
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------- //
	// -------------------- Constructor(s) -------------------- //
	// -------------------------------------------------------- //

	/**
	 * Creates an instance of a {@link NotFoundException} with the simple message "Not Found".
	 */
	public NotFoundException() {
		super("Not Found");
	}

	/**
	 * Creates an instance of a {@link NotFoundException} with the given {@code message}.
	 *
	 * @param message the message describing the cause of the exception.
	 */
	public NotFoundException(String message) {
		super(message);
	}

	/**
	 * Creates an instance of a {@link NotFoundException} with the given {@code code} and {@code
	 * message}.
	 *
	 * @param code the error code for the exception.
	 * @param message the message describing the cause of the exception.
	 */
	public NotFoundException(String code, String message) {
		super(code, message);
	}

	/**
	 * Creates an instance of a {@link NotFoundException} as a wrapper around the given {@link
	 * HttpError}.
	 *
	 * @param error the {@link HttpError}.
	 */
	public NotFoundException(HttpError error) {
		super(error);
	}

}
