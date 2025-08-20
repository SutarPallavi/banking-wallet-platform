package com.bankingwallet.account.domain;

import java.time.Instant;
import java.util.UUID;

public record AccountOpened(UUID accountId, String ownerId, Instant occurredAt) {}


