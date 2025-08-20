Write-Host "=== Banking Wallet Platform - Comprehensive Test ===" -ForegroundColor Green
Write-Host ""

# Test 1: Infrastructure Health Checks
Write-Host "1. Testing Infrastructure Services..." -ForegroundColor Yellow
Write-Host "   - Config Server (8888): " -NoNewline
try {
    $configHealth = Invoke-RestMethod -Uri "http://localhost:8888/actuator/health" -TimeoutSec 5
    Write-Host "✓ UP" -ForegroundColor Green
} catch {
    Write-Host "✗ DOWN" -ForegroundColor Red
}

Write-Host "   - Discovery Service (8761): " -NoNewline
try {
    $discoveryHealth = Invoke-RestMethod -Uri "http://localhost:8761/actuator/health" -TimeoutSec 5
    Write-Host "✓ UP" -ForegroundColor Green
} catch {
    Write-Host "✗ DOWN" -ForegroundColor Red
}

Write-Host "   - API Gateway (8080): " -NoNewline
try {
    $gatewayHealth = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "✓ UP" -ForegroundColor Green
} catch {
    Write-Host "✗ DOWN" -ForegroundColor Red
}

Write-Host "   - Auth Service (9000): " -NoNewline
try {
    $authHealth = Invoke-RestMethod -Uri "http://localhost:9000/actuator/health" -TimeoutSec 5
    Write-Host "✓ UP" -ForegroundColor Green
} catch {
    Write-Host "✗ DOWN" -ForegroundColor Red
}

Write-Host ""

# Test 2: Authentication Flow
Write-Host "2. Testing Authentication..." -ForegroundColor Yellow
try {
    $pair = 'demo-client:demo-secret'
    $b64 = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($pair))
    $headers = @{ Authorization = "Basic $b64"; 'Content-Type' = 'application/x-www-form-urlencoded' }
    $body = 'grant_type=client_credentials'
    
    $tokenResp = Invoke-RestMethod -Method Post -Uri 'http://localhost:9000/oauth2/token' -Headers $headers -Body $body -TimeoutSec 10
    $token = $tokenResp.access_token
    Write-Host "   ✓ OAuth2 Token obtained: $($token.Substring(0,20))..." -ForegroundColor Green
} catch {
    Write-Host "   ✗ OAuth2 Token failed: $($_.Exception.Message)" -ForegroundColor Red
    $token = $null
}

Write-Host ""

# Test 3: Account Service via Gateway
Write-Host "3. Testing Account Service..." -ForegroundColor Yellow
if ($token) {
    try {
        $authHeaders = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }
        $accountData = '{"ownerId":"test-user@example.com","accountType":"SAVINGS","initialBalance":1000.00}'
        
        $createResp = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/accounts' -Headers $authHeaders -Body $accountData -TimeoutSec 10
        Write-Host "   ✓ Account created: ID $($createResp.id)" -ForegroundColor Green
        
        # Test retrieving the account
        $getResp = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/accounts/$($createResp.id)" -Headers $authHeaders -TimeoutSec 10
        Write-Host "   ✓ Account retrieved: Balance $($getResp.balance)" -ForegroundColor Green
        
        $accountId = $createResp.id
    } catch {
        Write-Host "   ✗ Account operations failed: $($_.Exception.Message)" -ForegroundColor Red
        $accountId = $null
    }
} else {
    Write-Host "   ⚠ Skipping account tests (no token)" -ForegroundColor Yellow
}

Write-Host ""

# Test 4: Transaction Service
Write-Host "4. Testing Transaction Service..." -ForegroundColor Yellow
if ($token -and $accountId) {
    try {
        $authHeaders = @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' }
        $transactionData = '{"fromAccountId":"' + $accountId + '","toAccountId":"' + $accountId + '","amount":100.00,"type":"TRANSFER","description":"Test transaction"}'
        
        $transResp = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/transactions' -Headers $authHeaders -Body $transactionData -TimeoutSec 10
        Write-Host "   ✓ Transaction created: ID $($transResp.id)" -ForegroundColor Green
        
        # Test retrieving transactions
        $transListResp = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/transactions?accountId=$accountId" -Headers $authHeaders -TimeoutSec 10
        Write-Host "   ✓ Transactions retrieved: $($transListResp.content.Count) found" -ForegroundColor Green
        
    } catch {
        Write-Host "   ✗ Transaction operations failed: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "   ⚠ Skipping transaction tests (no account)" -ForegroundColor Yellow
}

Write-Host ""

# Test 5: Service Discovery
Write-Host "5. Testing Service Discovery..." -ForegroundColor Yellow
try {
    $services = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -TimeoutSec 10
    $registeredServices = $services.applications.application | Where-Object { $_.instance.Count -gt 0 }
    Write-Host "   ✓ Services registered: $($registeredServices.Count)" -ForegroundColor Green
    
    foreach ($service in $registeredServices) {
        $status = if ($service.instance[0].status -eq "UP") { "✓" } else { "⚠" }
        $color = if ($service.instance[0].status -eq "UP") { "Green" } else { "Yellow" }
        Write-Host "     $status $($service.name): $($service.instance[0].status)" -ForegroundColor $color
    }
} catch {
    Write-Host "   ✗ Service discovery failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 6: Database Connectivity
Write-Host "6. Testing Database Connectivity..." -ForegroundColor Yellow
try {
    # Test PostgreSQL
    $pgTest = docker exec banking-wallet-platform-postgres-1 pg_isready -U wallet
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ PostgreSQL: Ready" -ForegroundColor Green
    } else {
        Write-Host "   ✗ PostgreSQL: Not ready" -ForegroundColor Red
    }
} catch {
    Write-Host "   ⚠ PostgreSQL check skipped" -ForegroundColor Yellow
}

try {
    # Test MongoDB
    $mongoTest = docker exec banking-wallet-platform-mongo-1 mongosh --eval "db.runCommand('ping')" --quiet
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ MongoDB: Ready" -ForegroundColor Green
    } else {
        Write-Host "   ✗ MongoDB: Not ready" -ForegroundColor Red
    }
} catch {
    Write-Host "   ⚠ MongoDB check skipped" -ForegroundColor Yellow
}

Write-Host ""

# Test 7: Kafka Connectivity
Write-Host "7. Testing Kafka..." -ForegroundColor Yellow
try {
    $kafkaTest = docker exec banking-wallet-platform-kafka-1 kafka-topics --bootstrap-server localhost:9092 --list
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ Kafka: Ready" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Kafka: Not ready" -ForegroundColor Red
    }
} catch {
    Write-Host "   ⚠ Kafka check skipped" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Test Summary ===" -ForegroundColor Green
Write-Host "All infrastructure tests completed!" -ForegroundColor Green
Write-Host ""
Write-Host "To monitor services in real-time:" -ForegroundColor Cyan
Write-Host "  docker compose logs -f [service-name]" -ForegroundColor White
Write-Host ""
Write-Host "To access the application:" -ForegroundColor Cyan
Write-Host "  - API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "  - Auth Service: http://localhost:9000" -ForegroundColor White
Write-Host "  - Discovery: http://localhost:8761" -ForegroundColor White
Write-Host "  - Config: http://localhost:8888" -ForegroundColor White
