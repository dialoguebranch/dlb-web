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

package com.dialoguebranch.web.service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.rrd.utils.AppComponents;
import com.dialoguebranch.web.service.exception.HttpException;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller handles all errors in the application. It looks at the request attributes for a
 * status code, error message and exception. It distinguishes the following cases:
 * 
 * <p><ul>
 * <li>The request has no exception. It sets the status code and returns the error message or an
 * empty string.</li>
 * <li>The exception has a {@link ResponseStatus} annotation, such as a {@link HttpException}. It
 * sets the status code and returns the exception message.</li>
 * <li>The exception is a {@link ServletException}, which is usually the result of Spring
 * validation. It sets the status code and returns the error message.</li>
 * <li>Any other exception. It logs the exception, sets the status code to 500 Internal Server Error
 * and returns "Internal Server Error". This means that any details about the exception, which may
 * contain sensitive information, are hidden for the client.</li>
 * </ul></p>
 * 
 * @author Dennis Hofs (Roessingh Research and Development)
 * @author Harm op den Akker (Fruit Tree Labs)
 */

@Hidden
@RestController
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

	/** Used for writing logging information */
	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	// -------------------------------------------------------- //
	// -------------------- Constructor(s) -------------------- //
	// -------------------------------------------------------- //

	/**
	 * Instances of this class are constructed through Spring.
	 */
	public ErrorController() { }

	/**
	 * This controller handles all errors in the application. It looks at the request attributes for
	 * a status code, error message and exception. It distinguishes the following cases:
	 *
	 * <p>
	 *   <ul>
	 *     <li>The request has no exception. It sets the status code and returns the error message or
	 * 	   an empty string.</li>
	 *     <li>The exception has a {@link ResponseStatus} annotation, such as a {@link
	 *     HttpException}. It sets the status code and returns the exception message.</li>
	 *     <li>The exception is a {@link ServletException}, which is usually the result of Spring
	 *     validation. It sets the status code and returns the error message.</li>
	 *     <li>Any other exception. It logs the exception, sets the status code to 500 Internal
	 *     Server Error and returns "Internal Server Error". This means that any details about the
	 *     exception, which may contain sensitive information, are hidden for the client.</li>
	 *   </ul>
	 * </p>
	 *
	 * @param request the HTTPRequest object (to retrieve authentication headers and optional body
	 *                parameters).
	 * @param response the HTTP response (to add header WWW-Authenticate in case of a 401
	 *                 Unauthorized error).
	 * @return the appropriate error message
	 */
	@RequestMapping("/error")
	public Object error(HttpServletRequest request, HttpServletResponse response) {
		int statusCode = (Integer)request.getAttribute("jakarta.servlet.error.status_code");
		Object obj = request.getAttribute(
				"org.springframework.web.servlet.DispatcherServlet.EXCEPTION");
		Throwable exception = null;
		if (obj instanceof Throwable)
			exception = (Throwable)obj;
		String message = (String)request.getAttribute("jakarta.servlet.error.message");
		if (message == null)
			message = "";
		if (exception == null) {
            logger.error("Error {}: {}", statusCode, message);
			response.setStatus(statusCode);
			return "Unknown error";
		}

		ResponseStatus status = exception.getClass().getAnnotation(ResponseStatus.class);
		if (status != null && exception instanceof HttpException httpException) {
			response.setStatus(status.value().value());
			return httpException.getError();
		} else if (status != null) {
			response.setStatus(status.value().value());
			return exception.getMessage();
		} else if (exception instanceof ServletException) {
			response.setStatus(statusCode);
			return message;
		}

		Throwable cause = null;
		if (exception instanceof HttpMessageNotReadableException) {
			cause = exception.getCause();
		}
		if (cause instanceof JsonProcessingException) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return cause.getMessage();
		} else {
			String msg = "Internal Server Error";
            logger.error("{}: {}", msg, exception.getMessage(), exception);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return msg;
		}
	}

	/**
	 * Returns the error path for this Dialogue Branch Web Service.
	 * @return the error path for this Dialogue Branch Web Service.
	 */
	public String getErrorPath() {
		return "/error";
	}

}
