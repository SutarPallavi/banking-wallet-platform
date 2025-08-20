# Auth Service

Spring Authorization Server demo with in-memory users and JWKS. OIDC issuer is configured via config-server.

## Demo users
- alice@example.com / password → ROLE_USER
- admin@example.com / password → ROLE_ADMIN

## Endpoints
- `/.well-known/jwks.json` – JWKS
- `/oauth2/token` – Token endpoint (client_credentials + password demo)
- `/me` – Returns JWT claims
- OpenAPI: `/swagger-ui/index.html`

## Example: client_credentials
```bash
curl -u demo-client:demo-secret \
  -d 'grant_type=client_credentials' \
  http://localhost:9000/oauth2/token
```

## Example: password (demo)
```bash
curl -u demo-client:demo-secret \
  -d 'grant_type=password' \
  -d 'username=alice@example.com' \
  -d 'password=password' \
  http://localhost:9000/oauth2/token
```

## Call /me
```bash
ACCESS_TOKEN="$(curl -u demo-client:demo-secret -d 'grant_type=client_credentials' http://localhost:9000/oauth2/token | jq -r .access_token)"
curl -H "Authorization: Bearer $ACCESS_TOKEN" http://localhost:9000/me
```


