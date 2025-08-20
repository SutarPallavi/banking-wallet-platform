$headers = @{ 'Content-Type' = 'application/x-www-form-urlencoded' }
$body = 'grant_type=password&client_id=demo-client&username=alice@example.com&password=password'
$tokenResp = Invoke-RestMethod -Method Post -Uri 'http://localhost:9000/oauth2/password' -Headers $headers -Body $body
$token = $tokenResp.access_token
Write-Host ("Token: {0}..." -f $token.Substring(0,20))

$authHeaders = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }
$acctResp = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/accounts' -Headers $authHeaders -Body '{"ownerId":"alice@example.com"}'
$acctId = $acctResp.id
Write-Host ("Account created: {0}" -f $acctId)

$getResp = Invoke-RestMethod -Method Get -Uri ("http://localhost:8080/api/accounts/" + $acctId) -Headers $authHeaders
$getResp | ConvertTo-Json -Depth 5 | Write-Output


