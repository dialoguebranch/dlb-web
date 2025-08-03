package com.dialoguebranch.web.service.services;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseService {
	@Bean
	public SessionFactory sessionFactory() {
		com.dialoguebranch.web.service.Configuration cfg =
				com.dialoguebranch.web.service.Configuration.getInstance();

		return new HibernatePersistenceConfiguration("DialogueBranch")
//				.managedClass(DBVariable.class)
				.jdbcUrl("jdbc:mariadb://" + cfg.getMariadbHost() + ":" + cfg.getMariadbPort() +
						"/" + cfg.getMariadbDatabase() + "?createDatabaseIfNotExist=true")
				.jdbcCredentials(cfg.getMariadbUser(), cfg.getMariadbPassword())
				.schemaToolingAction(Action.UPDATE)
				.createEntityManagerFactory();
	}
}
