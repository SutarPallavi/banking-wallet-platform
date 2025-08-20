package com.bankingwallet.account.read;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountReadRepository extends MongoRepository<AccountReadModel, String> {
	Optional<AccountReadModel> findByAccountId(UUID accountId);
}

