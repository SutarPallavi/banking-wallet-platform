# Transaction Service

Validates and executes transfers with idempotency using Redis. Events are stored in Postgres (append-only) and a Mongo read model is maintained for queries. On commit, a message is published to `transactions.committed`.

## API
- POST `/transactions` with JSON body `{fromAccountId,toAccountId,amount,currency}` and optional header `Idempotency-Key`.
- GET `/transactions/{id}`
- GET `/accounts/{id}/transactions`

## Security
JWT resource server. Only the owner of `fromAccountId` can create a transfer.


