package com.bankingwallet.account.api;

import com.bankingwallet.account.read.AccountReadModel;
import com.bankingwallet.account.read.AccountReadRepository;
import com.bankingwallet.account.write.AccountCommandService;
import com.bankingwallet.account.write.AccountEventEntity;
import com.bankingwallet.account.write.AccountEventRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@Validated
public class AccountController {
	private final AccountCommandService commandService;
	private final AccountReadRepository readRepository;
	private final AccountEventRepository eventRepository;

	public AccountController(AccountCommandService commandService, AccountReadRepository readRepository, AccountEventRepository eventRepository) {
		this.commandService = commandService;
		this.readRepository = readRepository;
		this.eventRepository = eventRepository;
	}

	@PostMapping
	public Map<String, Object> open(@RequestBody Map<String, String> body) {
		String ownerId = body.get("ownerId");
		UUID id = commandService.openAccount(ownerId);
		return Map.of("id", id.toString());
	}

	@DeleteMapping("/{id}")
	public void close(@PathVariable UUID id) {
		commandService.closeAccount(id);
	}

	@GetMapping("/{id}")
	@PreAuthorize("#jwt.claims['sub'] == @accountSecurity.getOwnerId(#id)")
	public AccountReadModel get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return readRepository.findByAccountId(id).orElseThrow();
	}

	@GetMapping("/{id}/events")
	@PreAuthorize("hasRole('ADMIN')")
	public List<AccountEventEntity> events(@PathVariable UUID id) {
		return eventRepository.findByAccountIdOrderByVersionAsc(id);
	}
}


