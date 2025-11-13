package com.dialoguebranch.web.service.services;

import jakarta.annotation.PostConstruct;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseTestService {
	@Autowired
	private SessionFactory sessionFactory;

	@PostConstruct
	public void start() {
		sessionFactory.inTransaction(session -> {
//			session.createMutationQuery("delete from DBVariable").executeUpdate();
//			session.createMutationQuery("delete from DBUser").executeUpdate();

//			DBUser user = new DBUser("user");
//			session.persist(user);
//
//			for (int i = 1; i <= 10; i++) {
//				DBVariable variable = new DBVariable("varname" + i, UUID.randomUUID().toString());
//				variable.setUser(user);
//				session.persist(variable);
//			}
		});

//		sessionFactory.inTransaction(session -> {
//			Logger logger = AppComponents.getLogger(getClass().getSimpleName());
//
//			List<DBVariable> variables = session.createSelectionQuery(
//					"from DBVariable v where v.user.username = :username", DBVariable.class)
//					.setParameter("username", "user")
//					.getResultList();
//			for (DBVariable readVariable : variables) {
//				logger.info("VARIABLE: " + JsonMapper.generate(readVariable));
//			}
//
//			List<DBUser> users = session.createSelectionQuery(
//					"from DBUser u join fetch u.variables where u.username = :username", DBUser.class)
//					.setParameter("username", "user")
//					.getResultList();
//			for (DBVariable readVariable : users.get(0).getVariables()) {
//				logger.info("USER VARIABLE: " + JsonMapper.generate(readVariable));
//			}
//		});
	}
}
