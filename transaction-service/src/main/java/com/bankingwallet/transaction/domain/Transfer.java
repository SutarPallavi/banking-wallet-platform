package com.bankingwallet.transaction.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Transfer {
	public enum Status { INITIATED, COMMITTED, FAILED }

	private UUID id;
	private UUID fromAccountId;
	private UUID toAccountId;
	private long amount;
	private String currency;
	private Status status;
	private Instant createdAt;
	private Instant updatedAt;

	private final List<Object> pending = new ArrayList<>();

	public static Transfer create(UUID from, UUID to, long amount, String currency) {
		Transfer t = new Transfer();
		UUID id = UUID.randomUUID();
		t.apply(new TransferInitiated(id, from, to, amount, currency, Instant.now()));
		return t;
	}

	public void commit() {
		if (status == Status.COMMITTED) return;
		apply(new TransferCommitted(id, Instant.now()));
	}

	public void fail(String reason) {
		if (status == Status.COMMITTED) return;
		apply(new TransferFailed(id, reason, Instant.now()));
	}

	private void apply(Object event) {
		when(event);
		pending.add(event);
	}

	private void when(Object event) {
		if (event instanceof TransferInitiated e) {
			this.id = e.transactionId();
			this.fromAccountId = e.fromAccountId();
			this.toAccountId = e.toAccountId();
			this.amount = e.amount();
			this.currency = e.currency();
			this.status = Status.INITIATED;
			this.createdAt = e.occurredAt();
			this.updatedAt = e.occurredAt();
		} else if (event instanceof TransferCommitted e) {
			this.status = Status.COMMITTED;
			this.updatedAt = e.occurredAt();
		} else if (event instanceof TransferFailed e) {
			this.status = Status.FAILED;
			this.updatedAt = e.occurredAt();
		}
	}

	public static Transfer rehydrate(List<Object> events) {
		Transfer t = new Transfer();
		for (Object event : events) t.when(event);
		return t;
	}

	public List<Object> pendingEvents() { return pending; }
	public UUID getId() { return id; }
	public UUID getFromAccountId() { return fromAccountId; }
	public UUID getToAccountId() { return toAccountId; }
	public long getAmount() { return amount; }
	public String getCurrency() { return currency; }
	public Status getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}

