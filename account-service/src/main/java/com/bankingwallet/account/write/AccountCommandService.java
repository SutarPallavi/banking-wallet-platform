package com.bankingwallet.account.write;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bankingwallet.account.domain.Account;
import com.bankingwallet.account.domain.AccountClosed;
import com.bankingwallet.account.domain.AccountOpened;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccountCommandService {
	private final AccountEventRepository repository;
	private final ObjectMapper objectMapper;

	public AccountCommandService(AccountEventRepository repository, ObjectMapper objectMapper) {
		this.repository = repository;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public UUID openAccount(String ownerId) {
		Account account = Account.open(ownerId);
		saveEvents(account.getId(), 0L, account.pendingEvents());
		return account.getId();
	}

	@Transactional
	public void closeAccount(UUID accountId) {
		List<AccountEventEntity> history = repository.findByAccountIdOrderByVersionAsc(accountId);
		Account aggregate = Account.rehydrate(history.stream().map(this::toEvent).toList());
		Long currentVersion = history.isEmpty() ? 0L : history.get(history.size() - 1).getVersion();
		aggregate.close();
		saveEvents(accountId, currentVersion, aggregate.pendingEvents());
	}

	private void saveEvents(UUID accountId, Long expectedVersion, List<Object> events) {
		Long version = expectedVersion;
		for (Object event : events) {
			AccountEventEntity entity = new AccountEventEntity();
			entity.setAccountId(accountId);
			entity.setType(event.getClass().getSimpleName());
			entity.setPayload(writeJson(event));
			entity.setVersion(++version);
			entity.setCreatedAt(Instant.now());
			repository.save(entity);
		}
	}

	private Object toEvent(AccountEventEntity entity) {
		try {
			return switch (entity.getType()) {
				case "AccountOpened" -> objectMapper.readValue(entity.getPayload(), AccountOpened.class);
				case "AccountClosed" -> objectMapper.readValue(entity.getPayload(), AccountClosed.class);
				default -> throw new IllegalArgumentException("Unknown event type: " + entity.getType());
			};
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private String writeJson(Object event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}


