package com.bankingwallet.transaction.domain;

import java.time.Instant;
import java.util.UUID;

public record TransferFailed(UUID transactionId, String reason, Instant occurredAt) {}


