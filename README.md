# SentinelTrack

A real-time identity/session risk monitoring platform that detects suspicious login and session activity using Kafka, Spring Boot microservices, rule-based risk scoring, and a React dashboard.

## Business Problem

Security teams need real-time visibility into user authentication and session activities to detect and respond to potential security threats. Traditional SIEM solutions are often expensive, complex, and lack real-time risk scoring capabilities. SentinelTrack provides a lightweight, event-driven platform that evaluates login events against configurable risk rules and alerts on suspicious activities.

## Architecture

SentinelTrack follows an event-driven microservices architecture:

```
┌─────────────────────┐
│ Event Simulator     │
│ Service             │
└──────────┬──────────┘
           │ Kafka (login-events)
           ▼
┌─────────────────────┐
│ Risk Evaluation     │
│ Service             │
└──────────┬──────────┘
           │ Kafka (risk-events)
           ▼
┌─────────────────────┐
│ Alert Service       │
└──────────┬──────────┘
           │ Kafka (alerts)
           ▼
┌─────────────────────┐
│ Dashboard API        │
│ Service             │
└──────────┬──────────┘
           │ REST API
           ▼
┌─────────────────────┐
│ React Dashboard UI  │
└─────────────────────┘
```

## Services

### 1. common-events
Shared Java module containing event DTOs and enums used by all backend services.

**Classes:**
- `LoginEvent` - Represents user login/session events
- `RiskEvent` - Represents evaluated risk scores
- `AlertEvent` - Represents security alerts
- `EventType` enum - LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, etc.
- `RiskLevel` enum - LOW, MEDIUM, HIGH, CRITICAL
- `AlertSeverity` enum - INFO, LOW, MEDIUM, HIGH, CRITICAL

### 2. event-simulator-service
Spring Boot service that generates fake login/session events and publishes them to Kafka.

**Endpoints:**
- `POST /simulate/login-normal` - Simulates a normal login event
- `POST /simulate/login-suspicious` - Simulates a suspicious login event
- `POST /simulate/failed-logins` - Simulates multiple failed login attempts

**Port:** 8081

### 3. risk-evaluation-service
Consumes LoginEvent from Kafka, evaluates suspicious behavior using rule-based engine, calculates risk score, and publishes RiskEvent to Kafka.

**Risk Rules:**
- `NewDeviceRule` - Detects logins from new devices
- `FailedLoginRule` - Detects multiple failed login attempts
- `GeoMismatchRule` - Detects logins from unusual geographic locations
- `ImpossibleTravelRule` - Detects impossible travel between locations
- `SuspiciousLoginTimeRule` - Detects logins at unusual times
- `AnomalyLoginFrequencyRule` - Detects unusually high failed-login frequency

**Risk Score Mapping:**
- 0-29: LOW
- 30-69: MEDIUM
- 70-89: HIGH
- 90-100: CRITICAL

**Kafka Topics:**
- Input: `login-events`
- Output: `risk-events`

**Port:** 8082

**Local testing without Kafka:**
- `GET /health`
- `POST /test/risk/evaluate` - Accepts `LoginEvent` JSON and returns a `RiskEvent`

Example:
```bash
curl -X POST http://localhost:8082/test/risk/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt-001",
    "userId": "user-123",
    "sessionId": "session-001",
    "timestamp": "2026-04-28T10:00:00Z",
    "eventType": "LOGIN_SUCCESS",
    "ipAddress": "203.0.113.10",
    "country": "Russia",
    "city": "Moscow",
    "deviceId": "device-new-001",
    "userAgent": "Mozilla/5.0",
    "success": true
  }'
```

### 4. alert-service
Consumes RiskEvent from Kafka and creates AlertEvent when risk level is HIGH or CRITICAL. Publishes alerts to Kafka topic.

**Alert Rules:**
- LOW and MEDIUM risk: No alert created
- HIGH risk: HIGH severity alert
- CRITICAL risk: CRITICAL severity alert

**Port:** 8083

**Local testing without Kafka:**
- `GET /health` - Health check endpoint
- `POST /test/alerts/create` - Accepts `RiskEvent` JSON and returns an `AlertEvent` (200 OK) or 204 No Content if no alert is created

Example HIGH risk test:
```bash
curl -X POST http://localhost:8083/test/alerts/create \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "risk-001",
    "sourceEventId": "evt-001",
    "userId": "user-123",
    "sessionId": "session-001",
    "timestamp": "2026-04-28T10:00:00Z",
    "riskScore": 75,
    "riskLevel": "HIGH",
    "reasons": [
      "Login from unusual geographic location",
      "Login from a new device"
    ]
  }'
```

Example CRITICAL risk test:
```bash
curl -X POST http://localhost:8083/test/alerts/create \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "risk-002",
    "sourceEventId": "evt-002",
    "userId": "user-456",
    "sessionId": "session-002",
    "timestamp": "2026-04-28T10:00:00Z",
    "riskScore": 95,
    "riskLevel": "CRITICAL",
    "reasons": [
      "Impossible travel detected",
      "Multiple failed login attempts detected"
    ]
  }'
```

Example LOW risk test (returns 204 No Content):
```bash
curl -i -X POST http://localhost:8083/test/alerts/create \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "risk-003",
    "sourceEventId": "evt-003",
    "userId": "user-789",
    "sessionId": "session-003",
    "timestamp": "2026-04-28T10:00:00Z",
    "riskScore": 10,
    "riskLevel": "LOW",
    "reasons": []
  }'
```

### 5. dashboard-api-service
Backend API for dashboard. Exposes REST endpoints for recent sessions, risk events, alerts, and summary metrics. Consumes RiskEvent and AlertEvent from Kafka to maintain real-time dashboard data.

**Endpoints:**
- `GET /api/alerts` - Returns recent alerts (sorted newest first, max 100)
- `GET /api/risks` - Returns recent risk events (sorted newest first, max 100)
- `GET /api/sessions` - Returns recent sessions (sorted newest first, max 100)
- `GET /api/summary` - Returns dashboard summary metrics
- `GET /api/health` - Health check endpoint

**Test Endpoints (for local development):**
- `POST /test/dashboard/mock-alert` - Creates a mock alert
- `POST /test/dashboard/mock-risk` - Creates a mock risk event
- `DELETE /test/dashboard/clear` - Clears all in-memory data

**Port:** 8084

**Kafka Topics Consumed:**
- `risk-events` - Consumed by RiskEventDashboardConsumer
- `alerts` - Consumed by AlertEventDashboardConsumer

**Configuration:**
- Mock data initialization can be enabled/disabled via `sentineltrack.dashboard.mock-data-enabled` in application.yml
- CORS enabled for React dashboard (localhost:3000, localhost:5173)

**Note:** Currently uses in-memory data storage with a limit of 100 records per category. PostgreSQL integration and WebSocket live updates planned for future.

Example curl commands:
```bash
# Health check
curl http://localhost:8084/api/health

# Get alerts
curl http://localhost:8084/api/alerts

# Get risk events
curl http://localhost:8084/api/risks

# Get sessions
curl http://localhost:8084/api/sessions

# Get summary
curl http://localhost:8084/api/summary

# Create mock alert
curl -X POST http://localhost:8084/test/dashboard/mock-alert

# Create mock risk
curl -X POST http://localhost:8084/test/dashboard/mock-risk

# Clear all data
curl -X DELETE http://localhost:8084/test/dashboard/clear
```

### 6. dashboard-ui
React dashboard showing real-time security monitoring view.

**Features:**
- Total sessions count
- High risk sessions count
- Critical alerts count
- Recent alerts table with severity indicators
- Recent sessions table with risk scores and status

## Kafka Topics

- `login-events` - Published by event-simulator-service, consumed by risk-evaluation-service
- `risk-events` - Published by risk-evaluation-service, consumed by alert-service
- `alerts` - Published by alert-service, consumed by dashboard-api-service (planned)

## Technology Stack

- **Java 21** - Programming language
- **Spring Boot 3.2.x** - Application framework
- **Maven** - Build tool
- **Apache Kafka** - Event streaming platform
- **PostgreSQL** - Relational database (planned for persistence)
- **Redis** - In-memory data store (planned for caching)
- **React 18** - Frontend framework
- **Docker Compose** - Container orchestration
- **Lombok** - Java library to reduce boilerplate code

## Running the Infrastructure

Start the infrastructure services (Kafka, Zookeeper, PostgreSQL, Redis):

```bash
docker-compose up -d
```

Verify services are running:

```bash
docker-compose ps
```

Stop infrastructure:

```bash
docker-compose down
```

## Running the Application

### Prerequisites
- Java 21
- Maven 3.8+
- Node.js 18+ (for React dashboard)
- Docker and Docker Compose (for infrastructure)

### Build All Services

```bash
mvn clean install
```

### Start Backend Services

Each service can be started individually:

```bash
# Event Simulator Service
cd event-simulator-service
mvn spring-boot:run

# Risk Evaluation Service
cd risk-evaluation-service
mvn spring-boot:run

# Alert Service
cd alert-service
mvn spring-boot:run

# Dashboard API Service
cd dashboard-api-service
mvn spring-boot:run
```

### Start React Dashboard

```bash
cd dashboard-ui
npm install
npm start
```

The dashboard will be available at http://localhost:3000

## Testing the System

1. Start all infrastructure services with Docker Compose
2. Start all backend services
3. Start the React dashboard
4. Use the event simulator endpoints to generate test events:

```bash
# Simulate a normal login
curl -X POST http://localhost:8081/simulate/login-normal

# Simulate a suspicious login
curl -X POST http://localhost:8081/simulate/login-suspicious

# Simulate failed logins
curl -X POST http://localhost:8081/simulate/failed-logins
```

5. View the results in the React dashboard at http://localhost:3000

## Future Improvements

- **Persistence:** Integrate PostgreSQL for persistent storage of events, risks, and alerts
- **Caching:** Use Redis for caching user profiles and device fingerprints
- **Real-time Updates:** Implement WebSocket support for real-time dashboard updates
- **Authentication:** Add authentication and authorization to the dashboard
- **Rule Configuration:** Enable dynamic rule configuration without code changes
- **Machine Learning:** Add ML-based anomaly detection for advanced risk scoring
- **Alert Notifications:** Integrate email, SMS, or Slack notifications for critical alerts
- **User Profiles:** Maintain user profiles with typical login patterns and device history
- **Dashboard Enhancements:** Add charts, graphs, and filtering capabilities
- **API Documentation:** Add OpenAPI/Swagger documentation
- **Monitoring:** Add application metrics and distributed tracing
- **Testing:** Add comprehensive unit and integration tests

## Project Structure

```
sentineltrack/
├── common-events/              # Shared event DTOs and enums
├── event-simulator-service/    # Event generation service
├── risk-evaluation-service/    # Risk evaluation engine
├── alert-service/              # Alert generation service
├── dashboard-api-service/      # Dashboard backend API
├── dashboard-ui/               # React frontend
├── docker-compose.yml          # Infrastructure configuration
└── README.md                  # This file
```

## Package Naming Convention

All Java packages follow the convention: `com.sentineltrack.{module}`

- `com.sentineltrack.common` - Common events module
- `com.sentineltrack.simulator` - Event simulator service
- `com.sentineltrack.risk` - Risk evaluation service
- `com.sentineltrack.alert` - Alert service
- `com.sentineltrack.dashboard` - Dashboard API service

## License

This project is created for portfolio and demonstration purposes.
