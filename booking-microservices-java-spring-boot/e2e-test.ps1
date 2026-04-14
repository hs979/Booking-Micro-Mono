# =============================================================================
# End-to-End Test Script for Booking Microservices (PowerShell)
# =============================================================================
# Prerequisites:
#   - All infrastructure (PostgreSQL, RabbitMQ, Keycloak) running
#   - All microservices (Flight:8082, Passenger:8083, Booking:8084) running
#   - Keycloak realm/client configured (see guide)
# =============================================================================

$ErrorActionPreference = "Continue"

$KEYCLOAK_URL = "http://localhost:8080"
$FLIGHT_API = "http://localhost:8082"
$PASSENGER_API = "http://localhost:8083"
$BOOKING_API = "http://localhost:8084"
$GATEWAY_API = "http://localhost:8081"

$REALM = "keycloak-realm"
$CLIENT_ID = "booking-client-credentials"
$CLIENT_SECRET = "secret"

$PASS_COUNT = 0
$FAIL_COUNT = 0

function Print-Header($msg) {
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "  $msg" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
}

function Print-Test($msg) {
    Write-Host "`n[TEST] $msg" -ForegroundColor Yellow
}

function Check-Response($testName, $statusCode, $expectedCode, $body) {
    if ($statusCode -eq $expectedCode) {
        Write-Host "[PASS] $testName (HTTP $statusCode)" -ForegroundColor Green
        $script:PASS_COUNT++
    } else {
        Write-Host "[FAIL] $testName (Expected HTTP $expectedCode, got HTTP $statusCode)" -ForegroundColor Red
        Write-Host "       Response: $body" -ForegroundColor Red
        $script:FAIL_COUNT++
    }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    try {
        $params = @{
            Method = $Method
            Uri = $Uri
            Headers = $Headers
            ContentType = "application/json"
            UseBasicParsing = $true
        }
        if ($Body) { $params.Body = $Body }
        $response = Invoke-WebRequest @params -ErrorAction Stop
        return @{ StatusCode = $response.StatusCode; Body = $response.Content }
    } catch {
        $statusCode = 0
        $body = $_.Exception.Message
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $body = $reader.ReadToEnd()
                $reader.Close()
            } catch {}
        }
        return @{ StatusCode = $statusCode; Body = $body }
    }
}

# ============================================================================
# Step 0: Health checks
# ============================================================================
Print-Header "Step 0: Service Health Checks"

foreach ($port in @(8082, 8083, 8084)) {
    try {
        $null = Invoke-WebRequest -Uri "http://localhost:$port" -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
        Write-Host "[OK] Service on port $port is reachable" -ForegroundColor Green
    } catch {
        Write-Host "[WARN] Service on port $port may not be fully reachable (this is okay if it responds to API paths)" -ForegroundColor Yellow
    }
}

# ============================================================================
# Step 1: Get JWT Token from Keycloak
# ============================================================================
Print-Header "Step 1: Authenticate with Keycloak"

try {
    $tokenResponse = Invoke-RestMethod -Method Post `
        -Uri "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" `
        -ContentType "application/x-www-form-urlencoded" `
        -Body "grant_type=client_credentials&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&scope=openid"
    $ACCESS_TOKEN = $tokenResponse.access_token
} catch {
    Write-Host "[FAIL] Could not obtain access token from Keycloak" -ForegroundColor Red
    Write-Host "       Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Make sure Keycloak is running and configured. See the guide." -ForegroundColor Yellow
    exit 1
}

if ([string]::IsNullOrEmpty($ACCESS_TOKEN)) {
    Write-Host "[FAIL] Access token is empty" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Got access token (first 50 chars): $($ACCESS_TOKEN.Substring(0, [Math]::Min(50, $ACCESS_TOKEN.Length)))..." -ForegroundColor Green

$authHeaders = @{ "Authorization" = "Bearer $ACCESS_TOKEN" }

# ============================================================================
# Step 2: Flight Service - Create Airports
# ============================================================================
Print-Header "Step 2: Flight Service - Create Airports"

Print-Test "Create Departure Airport"
$r = Invoke-Api -Method POST -Uri "${FLIGHT_API}/api/v1/flight/airport" -Headers $authHeaders -Body '{"name":"Beijing Capital Airport","address":"Beijing, China","code":"PEK1"}'
Check-Response "Create Departure Airport" $r.StatusCode 200 $r.Body
$DEPARTURE_AIRPORT_ID = ($r.Body | ConvertFrom-Json).id
Write-Host "  Departure Airport ID: $DEPARTURE_AIRPORT_ID"

Print-Test "Create Arrival Airport"
$r = Invoke-Api -Method POST -Uri "${FLIGHT_API}/api/v1/flight/airport" -Headers $authHeaders -Body '{"name":"Shanghai Pudong Airport","address":"Shanghai, China","code":"PVG1"}'
Check-Response "Create Arrival Airport" $r.StatusCode 200 $r.Body
$ARRIVE_AIRPORT_ID = ($r.Body | ConvertFrom-Json).id
Write-Host "  Arrival Airport ID: $ARRIVE_AIRPORT_ID"

# ============================================================================
# Step 3: Flight Service - Create Aircraft
# ============================================================================
Print-Header "Step 3: Flight Service - Create Aircraft"

Print-Test "Create Aircraft"
$r = Invoke-Api -Method POST -Uri "${FLIGHT_API}/api/v1/flight/aircraft" -Headers $authHeaders -Body '{"name":"Boeing 737","model":"737-800","manufacturingYear":2020}'
Check-Response "Create Aircraft" $r.StatusCode 200 $r.Body
$AIRCRAFT_ID = ($r.Body | ConvertFrom-Json).id
Write-Host "  Aircraft ID: $AIRCRAFT_ID"

# ============================================================================
# Step 4: Flight Service - Create Flight
# ============================================================================
Print-Header "Step 4: Flight Service - Create Flight"

Print-Test "Create Flight"
$flightBody = @{
    flightNumber = "CA1234"
    aircraftId = $AIRCRAFT_ID
    departureAirportId = $DEPARTURE_AIRPORT_ID
    departureDate = "2026-06-01T08:00:00.000Z"
    arriveDate = "2026-06-01T10:30:00.000Z"
    arriveAirportId = $ARRIVE_AIRPORT_ID
    durationMinutes = 150
    flightDate = "2026-06-01T08:00:00.000Z"
    status = "Flying"
    price = 1500
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${FLIGHT_API}/api/v1/flight" -Headers $authHeaders -Body $flightBody
Check-Response "Create Flight" $r.StatusCode 200 $r.Body
$FLIGHT_ID = ($r.Body | ConvertFrom-Json).id
Write-Host "  Flight ID: $FLIGHT_ID"

# ============================================================================
# Step 5: Flight Service - Get Flight by ID
# ============================================================================
Print-Header "Step 5: Flight Service - Get Flight by ID"

Print-Test "Get Flight by ID"
$r = Invoke-Api -Method GET -Uri "${FLIGHT_API}/api/v1/flight/${FLIGHT_ID}" -Headers $authHeaders
Check-Response "Get Flight by ID" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 6: Flight Service - Get Available Flights
# ============================================================================
Print-Header "Step 6: Flight Service - Get Available Flights"

Print-Test "Get Available Flights"
$r = Invoke-Api -Method GET -Uri "${FLIGHT_API}/api/v1/flight" -Headers $authHeaders
Check-Response "Get Available Flights" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 7: Flight Service - Update Flight
# ============================================================================
Print-Header "Step 7: Flight Service - Update Flight"

Print-Test "Update Flight"
$updateBody = @{
    flightNumber = "CA1234-U"
    aircraftId = $AIRCRAFT_ID
    departureAirportId = $DEPARTURE_AIRPORT_ID
    departureDate = "2026-06-01T09:00:00.000Z"
    arriveDate = "2026-06-01T11:30:00.000Z"
    arriveAirportId = $ARRIVE_AIRPORT_ID
    durationMinutes = 150
    flightDate = "2026-06-01T09:00:00.000Z"
    status = "Delay"
    price = 1800
    isDeleted = $false
} | ConvertTo-Json
$r = Invoke-Api -Method PUT -Uri "${FLIGHT_API}/api/v1/flight/${FLIGHT_ID}" -Headers $authHeaders -Body $updateBody
Check-Response "Update Flight" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 8: Flight Service - Create Seat
# ============================================================================
Print-Header "Step 8: Flight Service - Create Seat"

Print-Test "Create Seat 12A"
$seatBody = @{
    seatNumber = "12A"
    seatType = "Window"
    seatClass = "FirstClass"
    flightId = $FLIGHT_ID
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${FLIGHT_API}/api/v1/flight/seat" -Headers $authHeaders -Body $seatBody
Check-Response "Create Seat" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 9: Flight Service - Get Available Seats
# ============================================================================
Print-Header "Step 9: Flight Service - Get Available Seats"

Print-Test "Get Available Seats"
$r = Invoke-Api -Method GET -Uri "${FLIGHT_API}/api/v1/flight/get-available-seats/${FLIGHT_ID}" -Headers $authHeaders
Check-Response "Get Available Seats" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 10: Passenger Service - Register Passenger
# ============================================================================
Print-Header "Step 10: Passenger Service - Register Passenger"

Print-Test "Register Passenger"
$r = Invoke-Api -Method POST -Uri "${PASSENGER_API}/api/v1/passenger" -Headers $authHeaders -Body '{"name":"John Doe","passportNumber":"CN12345678","passengerType":1,"age":35}'
Check-Response "Register Passenger" $r.StatusCode 200 $r.Body
$PASSENGER_ID = ($r.Body | ConvertFrom-Json).id
Write-Host "  Passenger ID: $PASSENGER_ID"

# ============================================================================
# Step 11: Passenger Service - Get Passenger by ID
# ============================================================================
Print-Header "Step 11: Passenger Service - Get Passenger by ID"

Print-Test "Get Passenger by ID"
$r = Invoke-Api -Method GET -Uri "${PASSENGER_API}/api/v1/passenger/${PASSENGER_ID}" -Headers $authHeaders
Check-Response "Get Passenger by ID" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 12: Booking Service - Create Booking (Cross-service via gRPC)
# ============================================================================
Print-Header "Step 12: Booking Service - Create Booking (Cross-service)"

Print-Test "Create Booking"
$bookingBody = @{
    passengerId = $PASSENGER_ID
    flightId = $FLIGHT_ID
    description = "E2E test booking"
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${BOOKING_API}/api/v1/booking" -Headers $authHeaders -Body $bookingBody
Check-Response "Create Booking" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 13: Verify Seat Reserved
# ============================================================================
Print-Header "Step 13: Verify Seat Reserved After Booking"

Print-Test "Get Available Seats After Booking"
$r = Invoke-Api -Method GET -Uri "${FLIGHT_API}/api/v1/flight/get-available-seats/${FLIGHT_ID}" -Headers $authHeaders
Check-Response "Get Available Seats After Booking" $r.StatusCode 200 $r.Body
Write-Host "  (Should show fewer available seats now)"

# ============================================================================
# Step 14: Reserve Seat Manually
# ============================================================================
Print-Header "Step 14: Flight Service - Reserve Seat Manually"

Print-Test "Create Another Seat for Manual Reserve"
$seat2Body = @{
    seatNumber = "12B"
    seatType = "Window"
    seatClass = "Economy"
    flightId = $FLIGHT_ID
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${FLIGHT_API}/api/v1/flight/seat" -Headers $authHeaders -Body $seat2Body
Check-Response "Create Seat 12B" $r.StatusCode 200 $r.Body

Print-Test "Reserve Seat 12B"
$reserveBody = @{
    seatNumber = "12B"
    flightId = $FLIGHT_ID
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${FLIGHT_API}/api/v1/flight/reserve-seat" -Headers $authHeaders -Body $reserveBody
Check-Response "Reserve Seat 12B" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 15: Delete Flight
# ============================================================================
Print-Header "Step 15: Flight Service - Delete Flight"

Print-Test "Create a Temporary Flight for Deletion"
$tempFlightBody = @{
    flightNumber = "DEL001"
    aircraftId = $AIRCRAFT_ID
    departureAirportId = $DEPARTURE_AIRPORT_ID
    departureDate = "2026-07-01T08:00:00.000Z"
    arriveDate = "2026-07-01T10:30:00.000Z"
    arriveAirportId = $ARRIVE_AIRPORT_ID
    durationMinutes = 150
    flightDate = "2026-07-01T08:00:00.000Z"
    status = "Flying"
    price = 500
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${FLIGHT_API}/api/v1/flight" -Headers $authHeaders -Body $tempFlightBody
Check-Response "Create Temp Flight" $r.StatusCode 200 $r.Body
$TEMP_FLIGHT_ID = ($r.Body | ConvertFrom-Json).id

Print-Test "Delete Flight"
$r = Invoke-Api -Method DELETE -Uri "${FLIGHT_API}/api/v1/flight/${TEMP_FLIGHT_ID}" -Headers $authHeaders
Check-Response "Delete Flight" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 16: Gateway Tests
# ============================================================================
Print-Header "Step 16: API Gateway Routing Tests"

Print-Test "Gateway -> Flight: Get Available Flights"
$r = Invoke-Api -Method GET -Uri "${GATEWAY_API}/api/v1/flight" -Headers $authHeaders
Check-Response "Gateway -> Flight Service" $r.StatusCode 200 $r.Body

Print-Test "Gateway -> Passenger: Get Passenger by ID"
$r = Invoke-Api -Method GET -Uri "${GATEWAY_API}/api/v1/passenger/${PASSENGER_ID}" -Headers $authHeaders
Check-Response "Gateway -> Passenger Service" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 17: Swagger UI
# ============================================================================
Print-Header "Step 17: Swagger UI Accessibility"

foreach ($port in @(8082, 8083, 8084)) {
    Print-Test "Swagger UI on port $port"
    $r = Invoke-Api -Method GET -Uri "http://localhost:${port}/swagger-ui/index.html"
    Check-Response "Swagger UI (port $port)" $r.StatusCode 200 "(html)"
}

# ============================================================================
# Summary
# ============================================================================
Print-Header "Test Summary"
Write-Host "PASSED: $PASS_COUNT" -ForegroundColor Green
Write-Host "FAILED: $FAIL_COUNT" -ForegroundColor Red
$total = $PASS_COUNT + $FAIL_COUNT
Write-Host "TOTAL:  $total"
Write-Host ""

if ($FAIL_COUNT -eq 0) {
    Write-Host "All tests passed!" -ForegroundColor Green
} else {
    Write-Host "Some tests failed. Check the output above for details." -ForegroundColor Red
}
