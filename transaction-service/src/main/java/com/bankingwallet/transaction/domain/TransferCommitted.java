package com.bankingwallet.transaction.domain;

import java.time.Instant;
import java.util.UUID;

public record TransferCommitted(UUID transactionId, Instant occurredAt) {}


