package com.dialoguebranch.web.service.services;

import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.List;
import java.util.Set;

@Configuration
public class DatabaseService {
	@Bean
	public SessionFactory sessionFactory() {
		com.dialoguebranch.web.service.Configuration cfg =
				com.dialoguebranch.web.service.Configuration.getInstance();

		List<? extends Class<?>> entityClasses = findEntityClasses();

		HibernatePersistenceConfiguration hibernateConfig =
				new HibernatePersistenceConfiguration("DialogueBranch");

		for (Class<?> entityClass : entityClasses) {
			hibernateConfig.managedClass(entityClass);
		}

		return hibernateConfig
				.jdbcUrl("jdbc:mariadb://" + cfg.getMariadbHost() + ":" + cfg.getMariadbPort() +
						"/" + cfg.getMariadbDatabase() + "?createDatabaseIfNotExist=true")
				.jdbcCredentials(cfg.getMariadbUser(), cfg.getMariadbPassword())
				.schemaToolingAction(Action.UPDATE)
				.createEntityManagerFactory();
	}

	private List<? extends Class<?>> findEntityClasses() {
		ClassPathScanningCandidateComponentProvider scanner =
				new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

		Set<BeanDefinition> entities = scanner.findCandidateComponents(
				"com.dialoguebranch.web.service.models");
		return entities.stream().map((entity) -> {
			try {
				return Class.forName(entity.getBeanClassName());
			} catch (ClassNotFoundException ex) {
				throw new RuntimeException("Entity class not found: " + ex.getMessage(), ex);
			}
		}).toList();
	}
}
