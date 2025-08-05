package com.dialoguebranch.web.service.services;

import com.dialoguebranch.web.service.models.DBUser;
import com.dialoguebranch.web.service.models.DBVariable;
import jakarta.annotation.PostConstruct;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.json.JsonMapper;
import org.hibernate.SessionFactory;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DatabaseTestService {
	@Autowired
	private SessionFactory sessionFactory;

	@PostConstruct
	public void start() {
		sessionFactory.inTransaction(session -> {
			HibernateCriteriaBuilder builder = session.getCriteriaBuilder();

			session.createMutationQuery(builder.createCriteriaDelete(DBUser.class));
			session.createMutationQuery(builder.createCriteriaDelete(DBVariable.class));

			DBUser user = new DBUser("user");
			session.persist(user);

			for (int i = 1; i <= 10; i++) {
				DBVariable variable = new DBVariable("varname" + i, UUID.randomUUID().toString());
				variable.setUser(user);
				session.persist(variable);
			}

			Logger logger = AppComponents.getLogger(getClass().getSimpleName());

			List<DBVariable> variables = session.createSelectionQuery(
					"from DBVariable v where v.user.id = :user", DBVariable.class)
					.setParameter("user", user.getId())
					.getResultList();
			for (DBVariable readVariable : variables) {
				logger.info("VARIABLE: " + JsonMapper.generate(readVariable));
			}
		});
	}
}
