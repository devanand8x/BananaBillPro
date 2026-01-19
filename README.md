# 🍌 BananaBillPro

A complete billing and management system for banana traders. Built with React, Spring Boot, and MongoDB.

![Tests](https://img.shields.io/badge/tests-315%2B%20passing-brightgreen)
![Build](https://img.shields.io/badge/build-passing-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

## ✨ Features

- **Bill Management** - Create, edit, print, and share bills via WhatsApp
- **Farmer Management** - Track farmers with mobile, address, and transaction history
- **Payment Tracking** - Record payments, mark as paid, send confirmations
- **Reports** - Monthly reports, farmer-wise statements, export to PDF
- **WhatsApp Integration** - Send bills and statements directly via Twilio
- **PWA Support** - Install as app on mobile devices
- **Multi-language** - Hindi & English support

## 🔧 Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18 + Vite + TypeScript |
| UI | ShadCN UI + TailwindCSS |
| Backend | Java 17 + Spring Boot 3 |
| Database | MongoDB |
| Cache | Redis |
| Auth | JWT |
| Messaging | Twilio WhatsApp API |

## 🚀 Quick Start

### Prerequisites

- Node.js 18+
- Java 17+
- MongoDB
- Redis (optional)

### Backend Setup

```bash
cd backend
cp .env.example .env
# Edit .env with your credentials
mvn spring-boot:run
```

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

### Access

- Frontend: http://localhost:5173
- Backend: http://localhost:8080/api

## 📁 Project Structure

```
BananaBillPro/
├── backend/           # Spring Boot API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bananabill/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   └── security/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/          # React App
│   ├── src/
│   │   ├── pages/
│   │   ├── components/
│   │   ├── services/
│   │   └── hooks/
│   └── package.json
└── README.md
```

## 🧪 Testing

```bash
# Backend tests (205 tests)
cd backend && mvn test

# Frontend tests (90 tests)
cd frontend && npm test

# E2E tests (20 tests)
cd frontend && npx playwright test
```

**Total: 315+ tests passing ✅**

## 🔐 Environment Variables

### Backend (.env)

```env
MONGODB_URI=mongodb://localhost:27017/banana_bill
JWT_SECRET=your-secret-key
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_WHATSAPP_FROM=whatsapp:+14155238886
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_UPLOAD_PRESET=your-preset
```

### Frontend (.env)

```env
VITE_API_URL=http://localhost:8080/api
```

## 🚀 Deployment

### Free Deployment Stack

| Service | Purpose | Cost |
|---------|---------|------|
| Vercel | Frontend | Free |
| Render | Backend | Free |
| MongoDB Atlas | Database | Free 512MB |

## 📊 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /auth/login | User login |
| POST | /auth/register | User registration |
| GET | /bills/recent | Get recent bills |
| POST | /bills | Create bill |
| GET | /farmers | Get all farmers |
| POST | /farmers | Create farmer |

## 📄 License

MIT License - feel free to use for personal or commercial projects.

## 👨‍💻 Author

Built with ❤️ for banana traders everywhere.
