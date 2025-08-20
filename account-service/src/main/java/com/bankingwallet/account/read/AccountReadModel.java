package com.bankingwallet.account.read;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document("account_read")
public class AccountReadModel {
	@Id
	private String id;
	private UUID accountId;
	private String ownerId;
	private String status;
	private long balance;
	private Instant updatedAt;

	public String getId() { return id; }
	public UUID getAccountId() { return accountId; }
	public String getOwnerId() { return ownerId; }
	public String getStatus() { return status; }
	public long getBalance() { return balance; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void setId(String id) { this.id = id; }
	public void setAccountId(UUID accountId) { this.accountId = accountId; }
	public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
	public void setStatus(String status) { this.status = status; }
	public void setBalance(long balance) { this.balance = balance; }
	public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

