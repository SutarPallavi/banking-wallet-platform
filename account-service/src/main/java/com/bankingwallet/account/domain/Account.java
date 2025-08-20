package com.bankingwallet.account.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Account {
	public enum Status { OPEN, CLOSED }

	private UUID id;
	private String ownerId;
	private Status status;
	private Instant createdAt;
	private Instant updatedAt;

	private final List<Object> pendingEvents = new ArrayList<>();

	public static Account open(String ownerId) {
		Account account = new Account();
		UUID id = UUID.randomUUID();
		account.apply(new AccountOpened(id, ownerId, Instant.now()));
		return account;
	}

	public void close() {
		if (status == Status.CLOSED) {
			return;
		}
		apply(new AccountClosed(id, Instant.now()));
	}

	public void apply(Object event) {
		when(event);
		this.pendingEvents.add(event);
	}

	public void when(Object event) {
		if (event instanceof AccountOpened e) {
			this.id = e.accountId();
			this.ownerId = e.ownerId();
			this.status = Status.OPEN;
			this.createdAt = e.occurredAt();
			this.updatedAt = e.occurredAt();
		} else if (event instanceof AccountClosed e) {
			this.status = Status.CLOSED;
			this.updatedAt = e.occurredAt();
		}
	}

	public static Account rehydrate(List<Object> events) {
		Account account = new Account();
		for (Object event : events) {
			account.when(event);
		}
		return account;
	}

	public List<Object> pendingEvents() { return pendingEvents; }

	public UUID getId() { return id; }
	public String getOwnerId() { return ownerId; }
	public Status getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}


