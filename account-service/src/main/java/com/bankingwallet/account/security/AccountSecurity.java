package com.bankingwallet.account.security;

import com.bankingwallet.account.read.AccountReadRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("accountSecurity")
public class AccountSecurity {
	private final AccountReadRepository repository;

	public AccountSecurity(AccountReadRepository repository) {
		this.repository = repository;
	}

	public String getOwnerId(UUID accountId) {
		return repository.findByAccountId(accountId).map(m -> m.getOwnerId()).orElse("");
	}
}

