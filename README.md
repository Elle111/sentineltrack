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

**Risk Score Mapping:**
- 0-29: LOW
- 30-69: MEDIUM
- 70-99: HIGH
- 100+: CRITICAL

### 4. alert-service
Consumes RiskEvent from Kafka and creates AlertEvent when risk level is HIGH or CRITICAL. Publishes alerts to Kafka topic.

**Alert Rules:**
- LOW and MEDIUM risk: No alert created
- HIGH risk: HIGH severity alert
- CRITICAL risk: CRITICAL severity alert

### 5. dashboard-api-service
Backend API for dashboard. Exposes REST endpoints for recent sessions, risk events, and alerts.

**Endpoints:**
- `GET /api/alerts` - Returns all alerts
- `GET /api/risks` - Returns all risk events
- `GET /api/sessions` - Returns all sessions
- `GET /api/health` - Health check endpoint

**Port:** 8082

**Note:** Currently uses in-memory data storage. PostgreSQL integration planned for future.

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
