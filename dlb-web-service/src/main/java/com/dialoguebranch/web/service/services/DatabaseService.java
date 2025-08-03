package com.dialoguebranch.web.service.services;

import com.dialoguebranch.web.service.entities.DBVariable;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DatabaseService {
	private static final Object LOCK = new Object();

	private static boolean createdDb = false;

	@Bean
	public SessionFactory sessionFactory() {
		synchronized (LOCK) {
			if (!createdDb) {
				createDatabase();
				createdDb = true;
			}
		}

		com.dialoguebranch.web.service.Configuration cfg =
				com.dialoguebranch.web.service.Configuration.getInstance();

		return new HibernatePersistenceConfiguration("DialogueBranch")
//				.managedClass(DBVariable.class)
				.jdbcUrl("jdbc:mariadb://" + cfg.getMariadbHost() + ":" + cfg.getMariadbPort() +
						"/" + cfg.getMariadbDatabase())
				.jdbcCredentials(cfg.getMariadbUser(), cfg.getMariadbPassword())
				.schemaToolingAction(Action.UPDATE)
				.createEntityManagerFactory();
	}

	private void createDatabase() {
		com.dialoguebranch.web.service.Configuration cfg =
				com.dialoguebranch.web.service.Configuration.getInstance();

		try {
			Class.forName("org.mariadb.jdbc.Driver");

			Connection conn = DriverManager.getConnection(
					"jdbc:mariadb://" + cfg.getMariadbHost() + ":" + cfg.getMariadbPort(),
					cfg.getMariadbUser(), cfg.getMariadbPassword());

			conn.createStatement().execute(
					"CREATE DATABASE IF NOT EXISTS `" + cfg.getMariadbDatabase() +
					"` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create database: " + ex.getMessage(), ex);
		}
	}
}
