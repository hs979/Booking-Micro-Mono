# =============================================================================
# End-to-End Test Script for Booking Monolith
# =============================================================================
# Prerequisites:
#   - PostgreSQL running (localhost:5432), database booking_monolith created
#   - Keycloak running (localhost:8080), realm/client configured
#   - Monolith app running (localhost:8085)
# =============================================================================

$ErrorActionPreference = "Continue"

$KEYCLOAK_URL = "http://localhost:8080"
$APP_API = "http://localhost:8085"

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
# Step 1: Authenticate with Keycloak
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
    exit 1
}

if ([string]::IsNullOrEmpty($ACCESS_TOKEN)) {
    Write-Host "[FAIL] Access token is empty" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Got access token (first 50 chars): $($ACCESS_TOKEN.Substring(0, [Math]::Min(50, $ACCESS_TOKEN.Length)))..." -ForegroundColor Green

$authHeaders = @{ "Authorization" = "Bearer $ACCESS_TOKEN" }

# ============================================================================
# Step 2: Verify seed data - Get Available Flights (should have seed flight)
# ============================================================================
Print-Header "Step 2: Verify Seed Data - Get Available Flights"

Print-Test "Get Available Flights (seed data)"
$r = Invoke-Api -Method GET -Uri "${APP_API}/api/v1/flight" -Headers $authHeaders
Check-Response "Get Available Flights" $r.StatusCode 200 $r.Body
if ($r.StatusCode -eq 200) {
    Write-Host "  Response: $($r.Body.Substring(0, [Math]::Min(200, $r.Body.Length)))..." -ForegroundColor Gray
}

# Use seed data IDs
$SEED_FLIGHT_ID = "3c5c0000-97c6-fc34-2eb9-08db322230c9"
$SEED_AIRCRAFT_ID = "3c5c0000-97c6-fc34-fcd3-08db322230c8"
$SEED_DEPARTURE_AIRPORT_ID = "3c5c0000-97c6-fc34-a0cb-08db322230c8"
$SEED_ARRIVE_AIRPORT_ID = "3c5c0000-97c6-fc34-fc3c-08db322230c8"
$SEED_PASSENGER_ID = "4c5c0000-97c6-fc34-a0cb-08db322230c0"

# ============================================================================
# Step 3: Get Seed Flight by ID
# ============================================================================
Print-Header "Step 3: Get Seed Flight by ID"

Print-Test "Get Flight by ID (seed)"
$r = Invoke-Api -Method GET -Uri "${APP_API}/api/v1/flight/${SEED_FLIGHT_ID}" -Headers $authHeaders
Check-Response "Get Seed Flight by ID" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 4: Get Available Seats for Seed Flight
# ============================================================================
Print-Header "Step 4: Get Available Seats (seed flight)"

Print-Test "Get Available Seats"
$r = Invoke-Api -Method GET -Uri "${APP_API}/api/v1/flight/get-available-seats/${SEED_FLIGHT_ID}" -Headers $authHeaders
Check-Response "Get Available Seats (seed)" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 5: Create Airport
# ============================================================================
Print-Header "Step 5: Create Airport"

$ts = Get-Date -Format "HHmmss"
Print-Test "Create Airport"
$r = Invoke-Api -Method POST -Uri "${APP_API}/api/v1/flight/airport" -Headers $authHeaders -Body "{`"name`":`"Test Airport $ts`",`"code`":`"T$ts`",`"address`":`"Test City`"}"
Check-Response "Create Airport" $r.StatusCode 200 $r.Body
$NEW_AIRPORT_ID = ""
if ($r.StatusCode -eq 200) { $NEW_AIRPORT_ID = ($r.Body | ConvertFrom-Json).id }
Write-Host "  Airport ID: $NEW_AIRPORT_ID"

# ============================================================================
# Step 6: Create Aircraft
# ============================================================================
Print-Header "Step 6: Create Aircraft"

Print-Test "Create Aircraft"
$r = Invoke-Api -Method POST -Uri "${APP_API}/api/v1/flight/aircraft" -Headers $authHeaders -Body "{`"name`":`"TestPlane $ts`",`"model`":`"M$ts`",`"manufacturingYear`":2020}"
Check-Response "Create Aircraft" $r.StatusCode 200 $r.Body
$NEW_AIRCRAFT_ID = ""
if ($r.StatusCode -eq 200) { $NEW_AIRCRAFT_ID = ($r.Body | ConvertFrom-Json).id }
Write-Host "  Aircraft ID: $NEW_AIRCRAFT_ID"

# ============================================================================
# Step 7: Create Flight
# ============================================================================
Print-Header "Step 7: Create Flight"

Print-Test "Create Flight"
$flightBody = @{
    flightNumber = "FL$ts"
    aircraftId = $NEW_AIRCRAFT_ID
    departureAirportId = $SEED_DEPARTURE_AIRPORT_ID
    departureDate = "2026-06-01T08:00:00"
    arriveDate = "2026-06-01T10:30:00"
    arriveAirportId = $SEED_ARRIVE_AIRPORT_ID
    durationMinutes = 150
    flightDate = "2026-06-01T08:00:00"
    status = "Flying"
    price = 1500
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${APP_API}/api/v1/flight" -Headers $authHeaders -Body $flightBody
Check-Response "Create Flight" $r.StatusCode 200 $r.Body
$NEW_FLIGHT_ID = ""
if ($r.StatusCode -eq 200) { $NEW_FLIGHT_ID = ($r.Body | ConvertFrom-Json).id }
Write-Host "  Flight ID: $NEW_FLIGHT_ID"

# ============================================================================
# Step 8: Get Flight by ID (new)
# ============================================================================
Print-Header "Step 8: Get New Flight by ID"

Print-Test "Get New Flight by ID"
$r = Invoke-Api -Method GET -Uri "${APP_API}/api/v1/flight/${NEW_FLIGHT_ID}" -Headers $authHeaders
Check-Response "Get New Flight by ID" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 9: Update Flight
# ============================================================================
Print-Header "Step 9: Update Flight"

Print-Test "Update Flight"
$updateBody = @{
    flightNumber = "FL${ts}U"
    aircraftId = $NEW_AIRCRAFT_ID
    departureAirportId = $SEED_DEPARTURE_AIRPORT_ID
    departureDate = "2026-06-01T09:00:00"
    arriveDate = "2026-06-01T11:30:00"
    arriveAirportId = $SEED_ARRIVE_AIRPORT_ID
    durationMinutes = 150
    flightDate = "2026-06-01T09:00:00"
    status = "Delay"
    price = 1800
    isDeleted = $false
} | ConvertTo-Json
$r = Invoke-Api -Method PUT -Uri "${APP_API}/api/v1/flight/${NEW_FLIGHT_ID}" -Headers $authHeaders -Body $updateBody
Check-Response "Update Flight" $r.StatusCode 204 $r.Body

# ============================================================================
# Step 10: Create Seat
# ============================================================================
Print-Header "Step 10: Create Seat"

Print-Test "Create Seat 1A"
$seatBody = @{
    seatNumber = "1A"
    seatType = "Window"
    seatClass = "FirstClass"
    flightId = $NEW_FLIGHT_ID
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${APP_API}/api/v1/flight/seat" -Headers $authHeaders -Body $seatBody
Check-Response "Create Seat 1A" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 11: Get Available Seats (new flight)
# ============================================================================
Print-Header "Step 11: Get Available Seats (new flight)"

Print-Test "Get Available Seats"
$r = Invoke-Api -Method GET -Uri "${APP_API}/api/v1/flight/get-available-seats/${NEW_FLIGHT_ID}" -Headers $authHeaders
Check-Response "Get Available Seats (new flight)" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 12: Reserve Seat
# ============================================================================
Print-Header "Step 12: Reserve Seat"

Print-Test "Reserve Seat 1A"
$reserveBody = @{
    seatNumber = "1A"
    flightId = $NEW_FLIGHT_ID
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${APP_API}/api/v1/flight/reserve-seat" -Headers $authHeaders -Body $reserveBody
Check-Response "Reserve Seat 1A" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 13: Register Passenger
# ============================================================================
Print-Header "Step 13: Register Passenger"

Print-Test "Register Passenger"
$r = Invoke-Api -Method POST -Uri "${APP_API}/api/v1/passenger" -Headers $authHeaders -Body "{`"name`":`"Passenger $ts`",`"PassportNumber`":`"PP$ts`",`"passengerType`":`"Male`",`"age`":30}"
Check-Response "Register Passenger" $r.StatusCode 200 $r.Body
$NEW_PASSENGER_ID = ""
if ($r.StatusCode -eq 200) { $NEW_PASSENGER_ID = ($r.Body | ConvertFrom-Json).id }
Write-Host "  Passenger ID: $NEW_PASSENGER_ID"

# ============================================================================
# Step 14: Get Passenger by ID
# ============================================================================
Print-Header "Step 14: Get Passenger by ID"

Print-Test "Get Passenger by ID"
$r = Invoke-Api -Method GET -Uri "${APP_API}/api/v1/passenger/${NEW_PASSENGER_ID}" -Headers $authHeaders
Check-Response "Get Passenger by ID" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 15: Create Booking (uses seed flight with available seats)
# ============================================================================
Print-Header "Step 15: Create Booking"

Print-Test "Create Booking"
$bookingBody = @{
    passengerId = $NEW_PASSENGER_ID
    flightId = $SEED_FLIGHT_ID
    description = "E2E monolith test booking"
} | ConvertTo-Json
$r = Invoke-Api -Method POST -Uri "${APP_API}/api/v1/booking" -Headers $authHeaders -Body $bookingBody
Check-Response "Create Booking" $r.StatusCode 200 $r.Body

# ============================================================================
# Step 16: Delete Flight
# ============================================================================
Print-Header "Step 16: Delete Flight (new flight)"

Print-Test "Delete Flight"
$r = Invoke-Api -Method DELETE -Uri "${APP_API}/api/v1/flight/${NEW_FLIGHT_ID}" -Headers $authHeaders
Check-Response "Delete Flight" $r.StatusCode 204 $r.Body

# ============================================================================
# Step 17: Swagger UI
# ============================================================================
Print-Header "Step 17: Swagger UI Accessibility"

Print-Test "Swagger UI"
$r = Invoke-Api -Method GET -Uri "${APP_API}/swagger-ui/index.html"
Check-Response "Swagger UI" $r.StatusCode 200 "(html)"

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
