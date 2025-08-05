package com.dialoguebranch.web.service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
	name = "variables",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "user_name",
			columnNames = { "user, name" }
		)
	})
public class DBVariable {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	private DBUser user;

	private String name;

	private String value;

	public DBVariable() {
	}

	public DBVariable(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public DBUser getUser() {
		return user;
	}

	public void setUser(DBUser user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
