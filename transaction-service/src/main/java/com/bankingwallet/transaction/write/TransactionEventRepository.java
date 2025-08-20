package com.bankingwallet.transaction.write;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionEventRepository extends JpaRepository<TransactionEventEntity, Long> {
	List<TransactionEventEntity> findByTransactionIdOrderByVersionAsc(UUID transactionId);
	Optional<TransactionEventEntity> findTopByTransactionIdOrderByVersionDesc(UUID transactionId);
}

