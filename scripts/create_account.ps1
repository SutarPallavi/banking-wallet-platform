$pair = 'demo-client:demo-secret'
$b64 = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($pair))
$headers = @{ Authorization = "Basic $b64"; 'Content-Type' = 'application/x-www-form-urlencoded' }
$body = 'grant_type=client_credentials'
$tokenResp = Invoke-RestMethod -Method Post -Uri 'http://localhost:9000/oauth2/token' -Headers $headers -Body $body
$token = $tokenResp.access_token
Write-Host ("Token: {0}..." -f $token.Substring(0,20))

$authHeaders = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }
$acctResp = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/accounts' -Headers $authHeaders -Body '{"ownerId":"alice@example.com"}'
$acctResp | ConvertTo-Json -Depth 5 | Write-Output


