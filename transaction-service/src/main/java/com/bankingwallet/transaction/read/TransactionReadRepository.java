package com.bankingwallet.transaction.read;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionReadRepository extends MongoRepository<TransactionReadModel, String> {
	Optional<TransactionReadModel> findByTransactionId(UUID transactionId);
	Page<TransactionReadModel> findByFromAccountIdOrToAccountId(UUID from, UUID to, Pageable pageable);
}


