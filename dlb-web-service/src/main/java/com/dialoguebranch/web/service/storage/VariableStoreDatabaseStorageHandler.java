/*
 *
 *                Copyright (c) 2023-2026 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *                                        as outlined below.
 *
 *                                            ----------
 *
 * Copyright (c) 2023-2026 Fruit Tree Labs (www.fruittreelabs.com)
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
import com.dialoguebranch.web.service.repositories.UserRepository;
import com.dialoguebranch.web.service.repositories.VariableRepository;
import com.dialoguebranch.web.service.services.DatabaseStorageService;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.datetime.DateTimeUtils;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.json.JsonMapper;
import org.slf4j.Logger;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class VariableStoreDatabaseStorageHandler implements VariableStoreStorageHandler {

	private final Logger logger =
			AppComponents.getLogger(ClassUtils.getUserClass(getClass()).getSimpleName());

	@Override
	public VariableStore read(User user) throws IOException, ParseException {
		DatabaseStorageService svc = getService();
		UserRepository userRepo = svc.getUserRepository();
		VariableRepository varRepo = svc.getVariableRepository();
		TransactionTemplate tx = svc.getTransactionTemplate();

		List<DBVariable> dbVariables = Objects.requireNonNull(tx.execute(status -> {
			DBUser dbUser = getOrCreateUser(userRepo, user.getId());
			return varRepo.findByUser_Id(dbUser.getId());
		}));

		List<Variable> variables = new ArrayList<>();
		for (DBVariable dbVariable : dbVariables) {
			ZonedDateTime now = DateTimeUtils.nowMs();
			variables.add(new Variable(
					dbVariable.getName(),
					JsonMapper.parse(dbVariable.getValue(), Object.class),
					now.toInstant().toEpochMilli(),
					now.getZone().getId()));
		}
		return new VariableStore(user, variables.toArray(new Variable[0]));
	}

	@Override
	public void write(VariableStore variableStore) throws IOException {
		DatabaseStorageService svc = getService();
		UserRepository userRepo = svc.getUserRepository();
		VariableRepository varRepo = svc.getVariableRepository();
		TransactionTemplate tx = svc.getTransactionTemplate();

		tx.execute(status -> {
			DBUser dbUser = getOrCreateUser(userRepo, variableStore.getUser().getId());
			List<DBVariable> existingVars = varRepo.findByUser_Id(dbUser.getId());

			Set<UUID> savedIds = new HashSet<>();
			for (Variable variable : variableStore.getVariables()) {
				DBVariable dbVariable = existingVars.stream()
						.filter(v -> v.getName().equals(variable.getName()))
						.findFirst()
						.orElseGet(() -> {
							DBVariable newVar = new DBVariable(variable.getName(), null);
							newVar.setUser(dbUser);
							return newVar;
						});
				dbVariable.setValue(JsonMapper.generate(variable.getValue()));
				savedIds.add(varRepo.save(dbVariable).getId());
			}

			existingVars.stream()
					.filter(v -> v.getId() != null && !savedIds.contains(v.getId()))
					.forEach(varRepo::delete);

			return null;
		});
	}

	@Override
	public void onChange(VariableStore variableStore, List<VariableStoreChange> changes) {
		try {
			write(variableStore);
		} catch (IOException e) {
			logger.error("Failed to write variable store changes: " + e.getMessage(), e);
		}
	}

	private DBUser getOrCreateUser(UserRepository userRepo, String username) {
		return userRepo.findByUsername(username)
				.orElseGet(() -> userRepo.save(new DBUser(username)));
	}

	private DatabaseStorageService getService() {
		return AppComponents.get(DatabaseStorageService.class);
	}
}
