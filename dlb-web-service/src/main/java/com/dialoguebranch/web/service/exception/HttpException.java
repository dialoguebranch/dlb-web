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

package com.dialoguebranch.web.service.exception;

import com.dialoguebranch.web.service.controller.ErrorController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Base class for exceptions that result in an HTTP error response. Subclasses should be annotated
 * with {@link ResponseStatus ResponseStatus}. They are handled by
 * {@link ErrorController ErrorController}.
 * 
 * @author Dennis Hofs (Roessingh Research and Development)
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public abstract class HttpException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	/** The {@link HttpError} to embed within this {@link HttpException}. */
	private final HttpError error;

	// -------------------------------------------------------- //
	// -------------------- Constructor(s) -------------------- //
	// -------------------------------------------------------- //

	/**
	 * Constructs a new HTTP exception with default error code 0.
	 * 
	 * @param message the error message.
	 */
	public HttpException(String message) {
		super(message);
		error = new HttpError(null, message);
	}
	
	/**
	 * Constructs a new HTTP exception.
	 * 
	 * @param code the error code (default null)
	 * @param message the error message
	 */
	public HttpException(String code, String message) {
		super(message);
		error = new HttpError(code, message);
	}
	
	/**
	 * Constructs a new HTTP exception with the specified error.
	 * 
	 * @param error the error
	 */
	public HttpException(HttpError error) {
		super(error.getMessage());
		this.error = error;
	}

	// ----------------------------------------------------------- //
	// -------------------- Getters & Setters -------------------- //
	// ----------------------------------------------------------- //

	/**
	 * Returns the error details.
	 * 
	 * @return the error details
	 */
	public HttpError getError() {
		return error;
	}

	// ------------------------------------------------------- //
	// -------------------- Other Methods -------------------- //
	// ------------------------------------------------------- //
	
	/**
	 * Returns the HTTP exception for the specified HTTP status code. Unsupported status codes will
	 * be mapped to an {@link InternalServerErrorException}.
	 * 
	 * @param statusCode the HTTP status code
	 * @param error the error details
	 * @return the HTTP exception
	 */
	public static HttpException forStatus(int statusCode, HttpError error) {
		return switch (statusCode) {
			case 400 ->
					new BadRequestException(error);
			case 401 ->
					new UnauthorizedException(error);
			case 403 ->
					new ForbiddenException(error);
			case 404 ->
					new NotFoundException(error);
			case 501 ->
					new NotImplementedException(error);
			default ->
					new InternalServerErrorException(error);
		};
	}

}
