package com.bankingwallet.transaction.api;

import com.bankingwallet.transaction.domain.Transfer;
import com.bankingwallet.transaction.read.TransactionReadModel;
import com.bankingwallet.transaction.read.TransactionReadRepository;
import com.bankingwallet.transaction.write.TransactionCommandService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
public class TransactionController {
	private final TransactionCommandService commandService;
	private final TransactionReadRepository readRepository;

	public TransactionController(TransactionCommandService commandService, TransactionReadRepository readRepository) {
		this.commandService = commandService;
		this.readRepository = readRepository;
	}

	@PostMapping("/transactions")
	@PreAuthorize("#body[fromAccountId] == #jwt.claims['sub']")
	public Map<String, Object> create(@RequestHeader(name = "Idempotency-Key", required = false) String idemKey,
	                                  @RequestBody Map<String, String> body,
	                                  @AuthenticationPrincipal Jwt jwt) {
		UUID from = UUID.fromString(body.get("fromAccountId"));
		UUID to = UUID.fromString(body.get("toAccountId"));
		long amount = Long.parseLong(body.get("amount"));
		String currency = body.getOrDefault("currency", "USD");
		UUID id = commandService.createTransfer(idemKey, from, to, amount, currency);
		return Map.of("transactionId", id.toString());
	}

	@GetMapping("/transactions/{id}")
	public TransactionReadModel get(@PathVariable UUID id) {
		return readRepository.findByTransactionId(id).orElseThrow();
	}

	@GetMapping("/accounts/{id}/transactions")
	public Page<TransactionReadModel> list(@PathVariable UUID id,
	                                      @RequestParam(defaultValue = "0") int page,
	                                      @RequestParam(defaultValue = "20") int size) {
		return readRepository.findByFromAccountIdOrToAccountId(id, id, PageRequest.of(page, size));
	}
}


