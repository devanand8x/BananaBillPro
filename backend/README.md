# 🍌 BananaBillPro Backend

**Professional Billing System API** - Spring Boot 3.2.9 | Java 17 | MongoDB | Redis

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Coverage](https://img.shields.io/badge/coverage-40%25-yellow)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.9-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-17-orange)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Testing](#testing)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [Contributing](#contributing)

---

## 🎯 Overview

BananaBillPro is an enterprise-grade billing system designed for agricultural businesses. It provides comprehensive bill management, farmer tracking, payment processing, and WhatsApp integration for seamless communication.

**Key Capabilities:**
- 📊 **Bill Management** - Create, track, and manage bills with automatic calculations
- 👨‍🌾 **Farmer Management** - Comprehensive farmer profiles and transaction history
- 💰 **Payment Processing** - Track payments, partial payments, and advances
- 📱 **WhatsApp Integration** - Auto-send bills and statements via Twilio
- 📈 **Reports & Analytics** - Daily, monthly, farmer-wise reports
- 🔐 **Secure Authentication** - JWT-based stateless authentication
- 🚀 **High Performance** - Redis caching, async processing, rate limiting

---

## ✨ Features

### Core Features
- ✅ **Bill Management**
  - Auto-generated bill numbers
  - Weight-based calculations (gross, tare, net, chargeable)
  - Custom rates per bill
  - Deductions (danda, tut)
  - Payment tracking
  
- ✅ **Farmer Management**
  - Farmer profiles with contact info
  - Transaction history
  - Outstanding balance tracking
  - Mobile number-based search

- ✅ **Payment System**
  - Full and partial payment support
  - Payment history audit trail
  - Overpayment/advance tracking
  - Payment status management

- ✅ **WhatsApp Integration**
  - Auto-send bills after creation
  - Send statements on demand
  - Image-based bill sharing
  - Twilio WhatsApp Business API

### Technical Features
- 🔐 **Security**
  - JWT authentication with refresh tokens
  - BCrypt password hashing
  - Rate limiting (Bucket4j)
  - CORS configuration
  - Security headers (CSP, XSS protection)
  
- 🚀 **Performance**
  - Redis caching
  - Async WhatsApp notifications
  - MongoDB indexing
  - Connection pooling

- 📊 **Monitoring**
  - Spring Boot Actuator
  - Prometheus metrics
  - Sentry error tracking
  - Health checks

---

## 🏗️ Architecture

**Layered Architecture** with clean separation of concerns:

```
┌─────────────────┐
│  Controllers    │  REST API Layer
├─────────────────┤
│   Services      │  Business Logic Layer
├─────────────────┤
│  Repositories   │  Data Access Layer
├─────────────────┤
│    Models       │  Domain Layer
└─────────────────┘
```

**Design Patterns:**
- Repository Pattern (Spring Data)
- DTO Pattern (Request/Response separation)
- Service Layer Pattern
- Dependency Injection
- Exception Handling Pattern

**Security Architecture:**
- Stateless JWT authentication
- Multi-layer security filters
- Rate limiting
- Input validation

For detailed architecture documentation, see [Architecture Report](docs/architecture.md).

---

## 🛠️ Tech Stack

### Core Framework
- **Spring Boot** 3.2.9 - Application framework
- **Java** 17 - Programming language
- **Maven** - Build tool

### Database & Caching
- **MongoDB** - Primary database (document store)
- **Redis** - Caching and session management

### Security
- **Spring Security** - Authentication & authorization
- **JWT (JJWT)** 0.12.6 - Token-based authentication
- **BCrypt** - Password hashing

### External Services
- **Twilio** 10.6.3 - WhatsApp Business API
- **Sentry** 7.20.1 - Error tracking

### Performance & Monitoring
- **Bucket4j** 8.10.1 - Rate limiting
- **Micrometer** - Metrics
- **Prometheus** - Metrics aggregation
- **Spring Boot Actuator** - Health checks

### Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Testcontainers** 1.20.4 - Integration testing
- **Spring Test** - Spring-specific testing

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))
- **MongoDB 6.0+** ([Download](https://www.mongodb.com/try/download/community))
- **Redis 6.0+** ([Download](https://redis.io/download))

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/BananaBillPro.git
cd BananaBillPro/backend
```

2. **Configure environment variables**
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. **Install dependencies**
```bash
mvn clean install
```

4. **Run the application**
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Quick Start with Docker

```bash
# Start MongoDB and Redis
docker-compose up -d

# Run the application
mvn spring-boot:run
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication

All endpoints (except `/auth/**`) require JWT authentication:

```bash
Authorization: Bearer <access_token>
```

### Key Endpoints

#### Authentication
```http
POST   /auth/register          # Register new user
POST   /auth/login             # User login
POST   /auth/refresh           # Refresh access token
POST   /auth/logout            # Logout
GET    /auth/me                # Get current user
```

#### Bills
```http
POST   /api/bills              # Create new bill
GET    /api/bills/{id}         # Get bill by ID
GET    /api/bills/number/{num} # Get bill by number
PUT    /api/bills/{id}         # Update bill
DELETE /api/bills/{id}         # Delete bill
GET    /api/bills/recent       # Get recent bills
GET    /api/bills/unpaid       # Get unpaid bills
POST   /api/bills/{id}/mark-paid  # Mark as paid
POST   /api/bills/{id}/record-payment  # Record payment
```

#### Farmers
```http
POST   /api/farmers            # Create/update farmer
GET    /api/farmers            # List all farmers
GET    /api/farmers/{id}       # Get farmer by ID
GET    /api/farmers/mobile/{mobile}  # Search by mobile
```

#### Reports
```http
GET    /api/reports/daily      # Daily report
GET    /api/reports/monthly    # Monthly report
GET    /api/reports/farmer/{id}  # Farmer-specific report
```

### Example: Create a Bill

**Request:**
```http
POST /api/bills
Content-Type: application/json
Authorization: Bearer <token>

{
  "farmerMobile": "9876543210",
  "grossWeight": 100.00,
  "tareWeight": 5.00,
  "rate": 50.00,
  "danda": 2.00,
  "tut": 1.00
}
```

**Response:**
```json
{
  "success": true,
  "message": "Bill created successfully",
  "data": {
    "id": "65abc123",
    "billNumber": "B001",
    "farmerName": "John Doe",
    "netWeight": 95.00,
    "chargeableWeight": 98.00,
    "netAmount": 4900.00,
    "paymentStatus": "UNPAID"
  }
}
```

For complete API documentation, visit `/swagger-ui.html` when running the application.

---

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the backend root:

```bash
# Database
MONGODB_URI=mongodb://localhost:27017/bananabill
MONGODB_DATABASE=bananabill

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-256-bit-secret-key-here-change-in-production
JWT_ACCESS_EXPIRATION=900000        # 15 minutes
JWT_REFRESH_EXPIRATION=604800000    # 7 days

# Twilio (WhatsApp)
TWILIO_ACCOUNT_SID=your-account-sid
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_WHATSAPP_NUMBER=whatsapp:+14155238886

# Sentry
SENTRY_DSN=your-sentry-dsn

# Application
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

### Application Properties

Key configurations in `application.properties`:

```properties
# Server
server.port=${SERVER_PORT:8080}

# MongoDB
spring.data.mongodb.uri=${MONGODB_URI}
spring.data.mongodb.database=${MONGODB_DATABASE}

# Redis
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}

# JWT
jwt.secret=${JWT_SECRET}
jwt.access-expiration=${JWT_ACCESS_EXPIRATION}
jwt.refresh-expiration=${JWT_REFRESH_EXPIRATION}

# Caching
spring.cache.type=redis
spring.cache.redis.time-to-live=600000  # 10 minutes

# Actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
```

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run with Coverage
```bash
mvn test jacoco:report
# View report at target/site/jacoco/index.html
```

### Test Categories

**Unit Tests** (37 tests)
- Service layer tests
- Utility tests
- Business logic validation

**Integration Tests**
- API endpoint tests
- Database integration tests
- External service mocking

**Current Coverage:** 40% (enterprise standard: 30-40%)

---

## 🚀 Deployment

### Production Build

```bash
# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/banana-bill-backend-1.0.0.jar
```

### Docker Deployment

```bash
# Build image
docker build -t bananabill-backend .

# Run container
docker run -p 8080:8080 \
  -e MONGODB_URI=mongodb://mongo:27017/bananabill \
  -e REDIS_HOST=redis \
  bananabill-backend
```

### Docker Compose

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Environment-Specific Profiles

```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production
java -jar app.jar --spring.profiles.active=prod
```

---

## 📊 Monitoring

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health
curl http://localhost:8080/health
```

### Metrics

```bash
# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Application metrics
curl http://localhost:8080/actuator/metrics
```

### Error Tracking

Errors are automatically sent to Sentry. View dashboard at: https://sentry.io

---

## 📁 Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/bananabill/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── exception/        # Exception handling
│   │   │   ├── model/            # Domain models
│   │   │   ├── repository/       # Data repositories
│   │   │   ├── security/         # Security components
│   │   │   ├── service/          # Business logic
│   │   │   └── util/             # Utilities
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/com/bananabill/  # Test classes
├── pom.xml                        # Maven configuration
├── .env.example                   # Environment template
└── README.md                      # This file
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Standards
- Follow Java coding conventions
- Write unit tests for new features
- Update documentation
- Run tests before committing

---

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👥 Authors

- **Your Name** - *Initial work*

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- MongoDB for flexible document storage
- Twilio for WhatsApp integration
- Open source community

---

## 📞 Support

For support, email support@bananabill.com or create an issue in the repository.

---

## 🔗 Links

- [Frontend Repository](../frontend)
- [API Documentation](http://localhost:8080/swagger-ui.html)
- [Architecture Documentation](docs/architecture.md)
- [Deployment Guide](docs/deployment.md)

---

**Built with ❤️ using Spring Boot**
