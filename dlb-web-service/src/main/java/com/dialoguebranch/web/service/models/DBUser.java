package com.dialoguebranch.web.service.models;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(
	name = "users",
	indexes = {
		@Index(columnList = "username")
	}
)
public class DBUser {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String username;

	@OneToMany(mappedBy = "user")
	private Set<DBVariable> variables;

	public DBUser() {
	}

	public DBUser(String username) {
		this.username = username;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<DBVariable> getVariables() {
		return variables;
	}

	public void setVariables(Set<DBVariable> variables) {
		this.variables = variables;
	}
}
