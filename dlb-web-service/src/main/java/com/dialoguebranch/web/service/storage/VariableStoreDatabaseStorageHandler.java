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

package com.dialoguebranch.web.service.storage;

import com.dialoguebranch.execution.User;
import com.dialoguebranch.execution.Variable;
import com.dialoguebranch.execution.VariableStore;
import com.dialoguebranch.execution.VariableStoreChange;
import com.dialoguebranch.web.service.models.DBUser;
import com.dialoguebranch.web.service.models.DBVariable;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.json.JsonMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.List;

public class VariableStoreDatabaseStorageHandler implements VariableStoreStorageHandler {

    private final Logger logger =
            AppComponents.getLogger(ClassUtils.getUserClass(getClass()).getSimpleName());

    @Override
    public VariableStore read(User user) throws IOException, ParseException {
		getSessionFactory().inTransaction(session -> {
			DBUser dbUser = getDBUser(session, user.getId());

			List<DBVariable> variables = session.createSelectionQuery(
					"from DBVariable where user_id = :userId", DBVariable.class)
					.setParameter("userId", dbUser.getId())
					.getResultList();
		});

		Variable[] variables = new Variable[0];

		return new VariableStore(user, variables);
    }

    @Override
    public void write(VariableStore variableStore) throws IOException {
		getSessionFactory().inTransaction(session -> {
			DBUser dbUser = getDBUser(session, variableStore.getUser().getId());

			for (Variable variable : variableStore.getVariables()) {
				DBVariable dbVariable = session.createSelectionQuery(
						"from DBVariable where user_id = :userId and name = :name", DBVariable.class)
						.setParameter("userId", dbUser.getId())
						.setParameter("name", variable.getName())
						.getSingleResultOrNull();

				if (dbVariable == null) {
					dbVariable = new DBVariable(variable.getName(), null);
					dbVariable.setUser(dbUser);
				}

				dbVariable.setValue(JsonMapper.generate(variable.getValue()));
				session.persist(dbVariable);
			}
		});
    }

    @Override
    public void onChange(VariableStore variableStore, List<VariableStoreChange> changes) {
        try {
            write(variableStore);
        } catch(IOException e) {
            logger.error("Failed to write variable store changes: " + e.getMessage(), e);
        }
    }

	private SessionFactory getSessionFactory() {
		return AppComponents.get(SessionFactory.class);
	}

	private DBUser getDBUser(Session session, String username) {
		DBUser dbUser = session.createSelectionQuery("from DBUser where username = :username",
						DBUser.class)
				.setParameter("username", username)
				.getSingleResultOrNull();

		if (dbUser != null) {
			return dbUser;
		}

		dbUser = new DBUser(username);
		session.persist(dbUser);

		return dbUser;
	}
}
