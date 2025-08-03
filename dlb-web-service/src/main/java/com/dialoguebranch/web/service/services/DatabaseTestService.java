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
//		sessionFactory.inTransaction(session -> {
//			session.persist(new DBVariable("varname", UUID.randomUUID().toString()));
//		});
	}
}
