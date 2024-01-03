/*
 *
 *                Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *      as outlined below. Based on original source code licensed under the following terms:
 *
 *                                            ----------
 *
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
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

import com.dialoguebranch.model.LoggedInteraction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ServerLoggedDialogue implements com.dialoguebranch.model.LoggedDialogue {

	private String id;
	private String sessionId;
	private long sessionStartTime;
	private String user;
	private String localTime;
	private long utcTime;
	private String timezone;
	private String dialogueName;
	private String language;
	private boolean completed;
	private boolean cancelled;
	private List<LoggedInteraction> interactionList = new ArrayList<>();

	public ServerLoggedDialogue() {
	}

	/**
	 * Constructs a new instance at the specified time. It should define the
	 * local time and location-based time zone (not an offset).
	 *
	 * @param user the identifier of the user for which the dialogue is being logged.
	 * @param dialogueStartTime the time that this dialogue started in the time zone of the user.
	 * @param sessionId an optional externally provided id to be added to the logs (or
	 *                    {@code null}).
	 */
	public ServerLoggedDialogue(String user, ZonedDateTime dialogueStartTime, String sessionId,
								long sessionStartTime) {
		this.user = user;
		this.utcTime = dialogueStartTime.toInstant().toEpochMilli();
		this.timezone = dialogueStartTime.getZone().toString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		this.localTime = dialogueStartTime.format(formatter);
		this.sessionId = sessionId;
		this.sessionStartTime = sessionStartTime;
		this.id = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the optional custom logging identifier, or {@code null} if none is set.
	 * @return the optional custom logging identifier, or {@code null} if none is set.
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets an optional custom logging identifier that may be used to cross-reference DialogueBranch Web
	 * Service dialogue logs for a session with logs from an external system.
	 * @param sessionId an optional custom logging identifier.
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public long getSessionStartTime() {
		return sessionStartTime;
	}

	public void setSessionStartTime(long sessionStartTime) {
		this.sessionStartTime = sessionStartTime;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String getLocalTime() {
		return localTime;
	}

	@Override
	public void setLocalTime(String localTime) {
		this.localTime = localTime;
	}

	@Override
	public long getUtcTime() {
		return utcTime;
	}

	@Override
	public void setUtcTime(long utcTime) {
		this.utcTime = utcTime;
	}

	@Override
	public String getTimezone() {
		return timezone;
	}

	@Override
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	@Override
	public String getDialogueName() {
		return dialogueName;
	}

	@Override
	public void setDialogueName(String dialogueName) {
		this.dialogueName = dialogueName;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

	@Override
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public List<LoggedInteraction> getInteractionList() {
		return interactionList;
	}

	@Override
	public void setInteractionList(List<LoggedInteraction> interactionList) {
		this.interactionList = interactionList;
	}

	/**
	 * Returns the timestamp (milliseconds since Jan 1st 1970 UTC) of the latest step in this
	 * {@link ServerLoggedDialogue}.
	 * @return the timestamp of the latest step in this {@link ServerLoggedDialogue}.
	 */
	@JsonIgnore
	public long getLatestInteractionTimestamp() {
		if(interactionList.isEmpty()) return this.getUtcTime();
		else {
			return interactionList.get(interactionList.size()-1).getTimestamp();
		}
	}
}
