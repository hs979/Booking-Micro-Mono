#!/bin/bash
# =============================================================================
# End-to-End Test Script for Booking Microservices
# =============================================================================
# Prerequisites:
#   - All infrastructure (PostgreSQL, RabbitMQ, Keycloak) running
#   - All microservices (Flight:8082, Passenger:8083, Booking:8084) running
#   - Keycloak realm/client configured (see guide)
# =============================================================================

set -e

KEYCLOAK_URL="http://localhost:8080"
FLIGHT_API="http://localhost:8082"
PASSENGER_API="http://localhost:8083"
BOOKING_API="http://localhost:8084"
GATEWAY_API="http://localhost:8081"

REALM="keycloak-realm"
CLIENT_ID="booking-client-credentials"
CLIENT_SECRET="secret"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASS_COUNT=0
FAIL_COUNT=0

print_header() {
    echo ""
    echo -e "${BLUE}============================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}============================================================${NC}"
}

print_test() {
    echo -e "\n${YELLOW}[TEST] $1${NC}"
}

check_response() {
    local test_name="$1"
    local http_code="$2"
    local expected_code="$3"
    local body="$4"

    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}[PASS] $test_name (HTTP $http_code)${NC}"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        echo -e "${RED}[FAIL] $test_name (Expected HTTP $expected_code, got HTTP $http_code)${NC}"
        echo -e "${RED}       Response: $body${NC}"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

# ============================================================================
# Step 0: Health checks
# ============================================================================
print_header "Step 0: Service Health Checks"

for svc_url in "$FLIGHT_API" "$PASSENGER_API" "$BOOKING_API"; do
    svc_name=$(echo "$svc_url" | sed 's|http://localhost:||')
    if curl -s --connect-timeout 3 "$svc_url" > /dev/null 2>&1; then
        echo -e "${GREEN}[OK] Service on port $svc_name is reachable${NC}"
    else
        echo -e "${RED}[WARN] Service on port $svc_name is NOT reachable${NC}"
    fi
done

# ============================================================================
# Step 1: Get JWT Token from Keycloak
# ============================================================================
print_header "Step 1: Authenticate with Keycloak"

TOKEN_RESPONSE=$(curl -s -X POST \
    "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=client_credentials" \
    -d "client_id=${CLIENT_ID}" \
    -d "client_secret=${CLIENT_SECRET}" \
    -d "scope=openid")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('access_token', ''))" 2>/dev/null || echo "")

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "" ]; then
    echo -e "${RED}[FAIL] Could not obtain access token from Keycloak${NC}"
    echo -e "${RED}       Response: $TOKEN_RESPONSE${NC}"
    echo ""
    echo -e "${YELLOW}Make sure Keycloak is running and configured. See the guide.${NC}"
    exit 1
fi

echo -e "${GREEN}[OK] Got access token (first 50 chars): ${ACCESS_TOKEN:0:50}...${NC}"
AUTH_HEADER="Authorization: Bearer ${ACCESS_TOKEN}"

# ============================================================================
# Step 2: Flight Service - Create Airport (Departure)
# ============================================================================
print_header "Step 2: Flight Service - Create Airports"

print_test "Create Departure Airport"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${FLIGHT_API}/api/v1/flight/airport" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d '{
        "name": "Beijing Capital Airport",
        "address": "Beijing, China",
        "code": "PEK1"
    }')
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Create Departure Airport" "$HTTP_CODE" "200" "$BODY"
DEPARTURE_AIRPORT_ID=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null || echo "")
echo "  Departure Airport ID: $DEPARTURE_AIRPORT_ID"

print_test "Create Arrival Airport"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${FLIGHT_API}/api/v1/flight/airport" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d '{
        "name": "Shanghai Pudong Airport",
        "address": "Shanghai, China",
        "code": "PVG1"
    }')
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Create Arrival Airport" "$HTTP_CODE" "200" "$BODY"
ARRIVE_AIRPORT_ID=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null || echo "")
echo "  Arrival Airport ID: $ARRIVE_AIRPORT_ID"

# ============================================================================
# Step 3: Flight Service - Create Aircraft
# ============================================================================
print_header "Step 3: Flight Service - Create Aircraft"

print_test "Create Aircraft"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${FLIGHT_API}/api/v1/flight/aircraft" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d '{
        "name": "Boeing 737",
        "model": "737-800",
        "manufacturingYear": 2020
    }')
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Create Aircraft" "$HTTP_CODE" "200" "$BODY"
AIRCRAFT_ID=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null || echo "")
echo "  Aircraft ID: $AIRCRAFT_ID"

# ============================================================================
# Step 4: Flight Service - Create Flight
# ============================================================================
print_header "Step 4: Flight Service - Create Flight"

print_test "Create Flight"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${FLIGHT_API}/api/v1/flight" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d "{
        \"flightNumber\": \"CA1234\",
        \"aircraftId\": \"${AIRCRAFT_ID}\",
        \"departureAirportId\": \"${DEPARTURE_AIRPORT_ID}\",
        \"departureDate\": \"2026-06-01T08:00:00.000Z\",
        \"arriveDate\": \"2026-06-01T10:30:00.000Z\",
        \"arriveAirportId\": \"${ARRIVE_AIRPORT_ID}\",
        \"durationMinutes\": 150,
        \"flightDate\": \"2026-06-01T08:00:00.000Z\",
        \"status\": \"Flying\",
        \"price\": 1500
    }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Create Flight" "$HTTP_CODE" "200" "$BODY"
FLIGHT_ID=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null || echo "")
echo "  Flight ID: $FLIGHT_ID"

# ============================================================================
# Step 5: Flight Service - Get Flight by ID
# ============================================================================
print_header "Step 5: Flight Service - Get Flight by ID"

print_test "Get Flight by ID"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${FLIGHT_API}/api/v1/flight/${FLIGHT_ID}" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Get Flight by ID" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 6: Flight Service - Get Available Flights
# ============================================================================
print_header "Step 6: Flight Service - Get Available Flights"

print_test "Get Available Flights"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${FLIGHT_API}/api/v1/flight" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Get Available Flights" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 7: Flight Service - Update Flight
# ============================================================================
print_header "Step 7: Flight Service - Update Flight"

print_test "Update Flight"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "${FLIGHT_API}/api/v1/flight/${FLIGHT_ID}" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d "{
        \"flightNumber\": \"CA1234-U\",
        \"aircraftId\": \"${AIRCRAFT_ID}\",
        \"departureAirportId\": \"${DEPARTURE_AIRPORT_ID}\",
        \"departureDate\": \"2026-06-01T09:00:00.000Z\",
        \"arriveDate\": \"2026-06-01T11:30:00.000Z\",
        \"arriveAirportId\": \"${ARRIVE_AIRPORT_ID}\",
        \"durationMinutes\": 150,
        \"flightDate\": \"2026-06-01T09:00:00.000Z\",
        \"status\": \"Delay\",
        \"price\": 1800,
        \"isDeleted\": false
    }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Update Flight" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 8: Flight Service - Create Seat
# ============================================================================
print_header "Step 8: Flight Service - Create Seat"

print_test "Create Seat"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${FLIGHT_API}/api/v1/flight/seat" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d "{
        \"seatNumber\": \"12A\",
        \"seatType\": \"Window\",
        \"seatClass\": \"FirstClass\",
        \"flightId\": \"${FLIGHT_ID}\"
    }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Create Seat" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 9: Flight Service - Get Available Seats
# ============================================================================
print_header "Step 9: Flight Service - Get Available Seats"

print_test "Get Available Seats"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${FLIGHT_API}/api/v1/flight/get-available-seats/${FLIGHT_ID}" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Get Available Seats" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 10: Passenger Service - Register Passenger
# ============================================================================
print_header "Step 10: Passenger Service - Register Passenger"

print_test "Register Passenger"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${PASSENGER_API}/api/v1/passenger" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d '{
        "name": "John Doe",
        "passportNumber": "CN12345678",
        "passengerType": 1,
        "age": 35
    }')
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Register Passenger" "$HTTP_CODE" "200" "$BODY"
PASSENGER_ID=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null || echo "")
echo "  Passenger ID: $PASSENGER_ID"

# ============================================================================
# Step 11: Passenger Service - Get Passenger by ID
# ============================================================================
print_header "Step 11: Passenger Service - Get Passenger by ID"

print_test "Get Passenger by ID"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${PASSENGER_API}/api/v1/passenger/${PASSENGER_ID}" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Get Passenger by ID" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 12: Booking Service - Create Booking (Cross-service via gRPC)
# ============================================================================
print_header "Step 12: Booking Service - Create Booking (Cross-service)"

print_test "Create Booking"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BOOKING_API}/api/v1/booking" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d "{
        \"passengerId\": \"${PASSENGER_ID}\",
        \"flightId\": \"${FLIGHT_ID}\",
        \"description\": \"E2E test booking\"
    }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Create Booking" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 13: Flight Service - Verify Seat Reserved
# ============================================================================
print_header "Step 13: Flight Service - Verify Seat Reserved After Booking"

print_test "Verify Available Seats After Booking"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${FLIGHT_API}/api/v1/flight/get-available-seats/${FLIGHT_ID}" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Get Available Seats After Booking" "$HTTP_CODE" "200" "$BODY"
echo "  (Should show fewer available seats now)"

# ============================================================================
# Step 14: Flight Service - Reserve Seat Manually
# ============================================================================
print_header "Step 14: Flight Service - Reserve Seat Manually"

print_test "Create Another Seat for Manual Reserve"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${FLIGHT_API}/api/v1/flight/seat" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d "{
        \"seatNumber\": \"12B\",
        \"seatType\": \"Window\",
        \"seatClass\": \"Economy\",
        \"flightId\": \"${FLIGHT_ID}\"
    }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Create Seat 12B" "$HTTP_CODE" "200" "$BODY"

print_test "Reserve Seat 12B"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${FLIGHT_API}/api/v1/flight/reserve-seat" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d "{
        \"seatNumber\": \"12B\",
        \"flightId\": \"${FLIGHT_ID}\"
    }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Reserve Seat 12B" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 15: Flight Service - Delete Flight
# ============================================================================
print_header "Step 15: Flight Service - Delete Flight"

print_test "Create a Temporary Flight for Deletion"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${FLIGHT_API}/api/v1/flight" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d "{
        \"flightNumber\": \"DEL001\",
        \"aircraftId\": \"${AIRCRAFT_ID}\",
        \"departureAirportId\": \"${DEPARTURE_AIRPORT_ID}\",
        \"departureDate\": \"2026-07-01T08:00:00.000Z\",
        \"arriveDate\": \"2026-07-01T10:30:00.000Z\",
        \"arriveAirportId\": \"${ARRIVE_AIRPORT_ID}\",
        \"durationMinutes\": 150,
        \"flightDate\": \"2026-07-01T08:00:00.000Z\",
        \"status\": \"Flying\",
        \"price\": 500
    }")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
TEMP_FLIGHT_ID=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null || echo "")
check_response "Create Temp Flight" "$HTTP_CODE" "200" "$BODY"

print_test "Delete Flight"
RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "${FLIGHT_API}/api/v1/flight/${TEMP_FLIGHT_ID}" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Delete Flight" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 16: Gateway Tests (same requests via API Gateway on port 8081)
# ============================================================================
print_header "Step 16: API Gateway Routing Tests"

print_test "Gateway -> Flight: Get Available Flights"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${GATEWAY_API}/api/v1/flight" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Gateway -> Flight Service" "$HTTP_CODE" "200" "$BODY"

print_test "Gateway -> Passenger: Get Passenger by ID"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${GATEWAY_API}/api/v1/passenger/${PASSENGER_ID}" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
check_response "Gateway -> Passenger Service" "$HTTP_CODE" "200" "$BODY"

# ============================================================================
# Step 17: Swagger UI Access (no auth needed)
# ============================================================================
print_header "Step 17: Swagger UI Accessibility"

for port in 8082 8083 8084; do
    print_test "Swagger UI on port $port"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "http://localhost:${port}/swagger-ui/index.html")
    HTTP_CODE=$(echo "$RESPONSE" | tail -1)
    check_response "Swagger UI (port $port)" "$HTTP_CODE" "200" "(html)"
done

# ============================================================================
# Summary
# ============================================================================
print_header "Test Summary"
echo -e "${GREEN}PASSED: $PASS_COUNT${NC}"
echo -e "${RED}FAILED: $FAIL_COUNT${NC}"
TOTAL=$((PASS_COUNT + FAIL_COUNT))
echo -e "TOTAL:  $TOTAL"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
else
    echo -e "${RED}Some tests failed. Check the output above for details.${NC}"
fi
