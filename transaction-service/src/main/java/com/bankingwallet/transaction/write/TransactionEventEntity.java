package com.bankingwallet.transaction.write;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_events")
public class TransactionEventEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "transaction_id", nullable = false)
	private UUID transactionId;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "payload", columnDefinition = "jsonb", nullable = false)
	private String payload;

	@Column(name = "version", nullable = false)
	private Long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public Long getId() { return id; }
	public UUID getTransactionId() { return transactionId; }
	public String getType() { return type; }
	public String getPayload() { return payload; }
	public Long getVersion() { return version; }
	public Instant getCreatedAt() { return createdAt; }

	public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
	public void setType(String type) { this.type = type; }
	public void setPayload(String payload) { this.payload = payload; }
	public void setVersion(Long version) { this.version = version; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}


