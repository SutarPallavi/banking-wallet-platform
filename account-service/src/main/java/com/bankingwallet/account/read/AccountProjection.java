package com.bankingwallet.account.read;

import com.bankingwallet.account.domain.AccountClosed;
import com.bankingwallet.account.domain.AccountOpened;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AccountProjection {
	private final AccountReadRepository repository;

	public AccountProjection(AccountReadRepository repository) {
		this.repository = repository;
	}

	public void on(AccountOpened event) {
		AccountReadModel doc = new AccountReadModel();
		doc.setAccountId(event.accountId());
		doc.setOwnerId(event.ownerId());
		doc.setStatus("OPEN");
		doc.setBalance(0);
		doc.setUpdatedAt(Instant.now());
		repository.save(doc);
	}

	public void on(AccountClosed event) {
		repository.findByAccountId(event.accountId()).ifPresent(doc -> {
			doc.setStatus("CLOSED");
			doc.setUpdatedAt(Instant.now());
			repository.save(doc);
		});
	}

	@KafkaListener(topics = "transactions.committed", groupId = "account-service")
	public void onTransactionCommitted(ConsumerRecord<String, String> record) {
		// Expected payload: {"accountId":"...","amount":123}
		try {
			var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(record.value());
			var accountId = java.util.UUID.fromString(json.get("accountId").asText());
			long amount = json.get("amount").asLong();
			repository.findByAccountId(accountId).ifPresent(doc -> {
				doc.setBalance(doc.getBalance() + amount);
				doc.setUpdatedAt(Instant.now());
				repository.save(doc);
			});
		} catch (Exception ignored) {}
	}
}

