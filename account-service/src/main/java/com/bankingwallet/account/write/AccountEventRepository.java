package com.bankingwallet.account.write;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountEventRepository extends JpaRepository<AccountEventEntity, Long> {
	List<AccountEventEntity> findByAccountIdOrderByVersionAsc(UUID accountId);
	Optional<AccountEventEntity> findTopByAccountIdOrderByVersionDesc(UUID accountId);
}

