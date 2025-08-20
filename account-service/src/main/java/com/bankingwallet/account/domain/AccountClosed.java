package com.bankingwallet.account.domain;

import java.time.Instant;
import java.util.UUID;

public record AccountClosed(UUID accountId, Instant occurredAt) {}


