/*
 *
 *                Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
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
 * Factory class for creating {@link UserService} objects. Defines how to generate a new {@link
 * UserService} given a {@code userId} and an {@link ApplicationManager}.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class UserServiceFactory {

	/** The ApplicationManager that created this UserServiceFactory */
	private final ApplicationManager applicationManager;

	/** The VariableStorageHandler that all created UserServices should use */
	private final VariableStoreStorageHandler storageHandler;

	// -------------------------------------------------------- //
	// -------------------- Constructor(s) -------------------- //
	// -------------------------------------------------------- //

	/**
	 * Creates an instance of a {@link UserServiceFactory} with a given {@link ApplicationManager}
	 * acting as this factory's owner, and a {@link VariableStoreStorageHandler} that is used to
	 * read and write Dialogue Branch variables to persistent storage.
	 *
	 * @param applicationManager the {@link ApplicationManager} that is the 'owner' of this {@link
	 *                           UserServiceFactory}.
	 * @param storageHandler the {@link VariableStoreStorageHandler} that is passed on to the {@link
	 *                       UserService} for reading and writing Dialogue Branch variables to
	 *                       persistent storage.
	 */
	public UserServiceFactory(ApplicationManager applicationManager,
							  VariableStoreStorageHandler storageHandler) {
		this.applicationManager = applicationManager;
		this.storageHandler = storageHandler;
	}

	// ----------------------------------------------------------- //
	// -------------------- Getters & Setters -------------------- //
	// ----------------------------------------------------------- //

	/**
	 * Returns the {@link ApplicationManager} that created this {@link UserServiceFactory}.
	 *
	 * @return the {@link ApplicationManager} that created this {@link UserServiceFactory}.
	 */
	public ApplicationManager getApplicationManager() {
		return applicationManager;
	}

	// ------------------------------------------------- //
	// -------------------- Methods -------------------- //
	// ------------------------------------------------- //

	/**
	 * Creates a {@link UserService} instance for the user identified by the given  {@code userId}.
	 *
	 * @param userId the identifier of the user for which to create the {@link UserService}.
	 * @return a {@link UserService} instance for the given user.
	 * @throws DatabaseException In case of an error loading in the known variables for the User.
	 * @throws IOException In case of an error loading in the known variables for the User.
	 */
	//TODO: When creating the User, the timeZone should be taken into account
	public UserService createUserService(String userId)
			throws DatabaseException, IOException {
		return new UserService(new User(userId), applicationManager, storageHandler);
	}

}
