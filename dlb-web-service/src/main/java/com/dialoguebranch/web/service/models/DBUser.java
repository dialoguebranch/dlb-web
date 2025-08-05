package com.dialoguebranch.web.service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
	name = "users",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "username",
			columnNames = "username"
		)
	}
)
public class DBUser {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String username;

	@OneToMany(mappedBy = "user")
	@JsonIgnore
	private Set<DBVariable> variables = new HashSet<>();

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
