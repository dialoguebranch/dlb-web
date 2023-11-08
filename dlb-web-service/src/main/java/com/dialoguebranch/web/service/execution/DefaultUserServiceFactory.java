/*
 *
 *                   Copyright (c) 2023 Fruit Tree Labs (www.fruittreelabs.com)
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

package com.dialoguebranch.web.service.execution;

import com.dialoguebranch.execution.User;
import com.dialoguebranch.web.service.storage.ExternalVariableServiceUpdater;
import com.dialoguebranch.web.service.storage.VariableStoreStorageHandler;
import nl.rrd.utils.exception.DatabaseException;

import java.io.IOException;

/**
 * The implementation of {@link UserServiceFactory} as used in the DialogueBranch Web Service.
 */
public class DefaultUserServiceFactory extends UserServiceFactory {

	private final VariableStoreStorageHandler storageHandler;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of a {@link DefaultUserServiceFactory} with a given
	 * {@link VariableStoreStorageHandler} that is used to read and write DialogueBranch
	 * variables to persistent storage.
	 * @param storageHandler the {@link VariableStoreStorageHandler} that is passed on to the
	 *                       {@link UserService} for reading and writing DialogueBranch variables to
	 *                       persistent storage.
	 */
	public DefaultUserServiceFactory(VariableStoreStorageHandler storageHandler) {
		this.storageHandler = storageHandler;
	}

	// -------------------------------------------------------------------
	// -------------------- Interface Implementations --------------------
	// -------------------------------------------------------------------

	@Override
	public UserService createUserService(String userId, ApplicationManager applicationManager)
			throws DatabaseException, IOException {
		return new UserService(
				new User(userId),
				applicationManager,
				storageHandler,
				new ExternalVariableServiceUpdater(applicationManager));
	}

}
