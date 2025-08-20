package com.bankingwallet.transaction.write;

import com.bankingwallet.transaction.domain.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionCommandService {
	private final TransactionEventRepository repository;
	private final ObjectMapper objectMapper;
	private final StringRedisTemplate redisTemplate;
	private final KafkaTemplate<String, String> kafkaTemplate;

	public TransactionCommandService(TransactionEventRepository repository, ObjectMapper objectMapper, StringRedisTemplate redisTemplate, KafkaTemplate<String, String> kafkaTemplate) {
		this.repository = repository;
		this.objectMapper = objectMapper;
		this.redisTemplate = redisTemplate;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Transactional
	public UUID createTransfer(String idempotencyKey, UUID from, UUID to, long amount, String currency) {
		ensureIdempotency(idempotencyKey);
		Transfer transfer = Transfer.create(from, to, amount, currency);
		// TODO: Validate accounts and sufficient funds via account-service (omitted in scaffold)
		transfer.commit();
		saveEvents(transfer.getId(), 0L, transfer.pendingEvents());
		publishCommit(transfer);
		return transfer.getId();
	}

	private void publishCommit(Transfer transfer) {
		try {
			String payload = objectMapper.writeValueAsString(new java.util.LinkedHashMap<String, Object>() {{
				put("transactionId", transfer.getId());
				put("fromAccountId", transfer.getFromAccountId());
				put("toAccountId", transfer.getToAccountId());
				put("amount", transfer.getAmount());
				put("currency", transfer.getCurrency());
				put("ts", Instant.now().toString());
			}});
			kafkaTemplate.send("transactions.committed", transfer.getId().toString(), payload);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private void ensureIdempotency(String key) {
		if (key == null || key.isBlank()) return; // for demo; in production, enforce
		Boolean success = redisTemplate.opsForValue().setIfAbsent("idem:" + key, "1", Duration.ofHours(24));
		if (Boolean.FALSE.equals(success)) {
			throw new DataIntegrityViolationException("Duplicate idempotency key");
		}
	}

	private void saveEvents(UUID txId, Long expectedVersion, List<Object> events) {
		Long version = expectedVersion;
		for (Object event : events) {
			TransactionEventEntity entity = new TransactionEventEntity();
			entity.setTransactionId(txId);
			entity.setType(event.getClass().getSimpleName());
			entity.setPayload(writeJson(event));
			entity.setVersion(++version);
			entity.setCreatedAt(Instant.now());
			repository.save(entity);
		}
	}

	private String writeJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Object> loadEvents(UUID txId) {
		return repository.findByTransactionIdOrderByVersionAsc(txId).stream().map(this::toEvent).toList();
	}

	private Object toEvent(TransactionEventEntity entity) {
		try {
			return switch (entity.getType()) {
				case "TransferInitiated" -> objectMapper.readValue(entity.getPayload(), TransferInitiated.class);
				case "TransferCommitted" -> objectMapper.readValue(entity.getPayload(), TransferCommitted.class);
				case "TransferFailed" -> objectMapper.readValue(entity.getPayload(), TransferFailed.class);
				default -> throw new IllegalArgumentException("Unknown event type: " + entity.getType());
			};
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}

