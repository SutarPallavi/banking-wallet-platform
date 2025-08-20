package com.bankingwallet.transaction.domain;

import java.time.Instant;
import java.util.UUID;

public record TransferInitiated(UUID transactionId, UUID fromAccountId, UUID toAccountId, long amount, String currency, Instant occurredAt) {}


