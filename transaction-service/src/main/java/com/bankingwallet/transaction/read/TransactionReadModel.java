package com.bankingwallet.transaction.read;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document("transaction_read")
public class TransactionReadModel {
	@Id
	private String id;
	private UUID transactionId;
	private UUID fromAccountId;
	private UUID toAccountId;
	private long amount;
	private String currency;
	private String status;
	private Instant updatedAt;

	public String getId() { return id; }
	public UUID getTransactionId() { return transactionId; }
	public UUID getFromAccountId() { return fromAccountId; }
	public UUID getToAccountId() { return toAccountId; }
	public long getAmount() { return amount; }
	public String getCurrency() { return currency; }
	public String getStatus() { return status; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void setId(String id) { this.id = id; }
	public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
	public void setFromAccountId(UUID fromAccountId) { this.fromAccountId = fromAccountId; }
	public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }
	public void setAmount(long amount) { this.amount = amount; }
	public void setCurrency(String currency) { this.currency = currency; }
	public void setStatus(String status) { this.status = status; }
	public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

