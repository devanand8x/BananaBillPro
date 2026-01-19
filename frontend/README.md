# 🍌 BananaBillPro Frontend

**Modern billing system for banana traders** - Built with React 18 + TypeScript + Vite

[![TypeScript](https://img.shields.io/badge/TypeScript-5.8-blue)](https://www.typescriptlang.org/)
[![React](https://img.shields.io/badge/React-18.3-blue)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-7.3-purple)](https://vitejs.dev/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

---

## 🎯 Features

- ✅ **Authentication** - Secure login with OTP support
- ✅ **Bill Management** - Create bills with auto-calculations
- ✅ **Farmer Management** - Track farmer details and history
- ✅ **Payment Tracking** - Record and monitor payments
- ✅ **WhatsApp Integration** - Share bills instantly
- ✅ **Reports & Analytics** - Daily/monthly reports
- ✅ **PWA Support** - Install as mobile app
- ✅ **Dark Mode Ready** - Theme support

---

## 🛠️ Tech Stack

### Core
- **React** 18.3.1 - UI library
- **TypeScript** 5.8.3 - Type safety
- **Vite** 7.3.1 - Build tool & dev server
- **React Router** 6.30.1 - Client-side routing

### UI & Styling
- **Radix UI** - Accessible component primitives
- **Tailwind CSS** 3.4.17 - Utility-first CSS
- **shadcn/ui** - Pre-built components
- **Lucide React** - Icon library

### State & Data
- **TanStack Query** 5.83.0 - Server state management
- **React Hook Form** 7.61.1 - Form handling
- **Zod** 3.25.76 - Schema validation
- **Axios** 1.13.2 - HTTP client

### Development
- **Vitest** 4.0.17 - Unit testing
- **Playwright** 1.41.0 - E2E testing
- **ESLint** 9.32.0 - Linting
- **TypeScript ESLint** 8.38.0 - TS linting

---

## 🚀 Getting Started

### Prerequisites

- Node.js 18+ ([Download](https://nodejs.org/))
- npm 9+ or pnpm 8+

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/BananaBillPro.git
cd BananaBillPro/frontend

# Install dependencies
npm install

# Setup environment variables
cp .env.example .env
# Edit .env with your backend API URL

# Start development server
npm run dev
```

The app will open at `http://localhost:5173`

---

## 📁 Project Structure

```
frontend/
├── public/              # Static assets
├── src/
│   ├── assets/          # Images, fonts
│   ├── components/      # Reusable components
│   │   ├── ui/          # UI primitives (45+ components)
│   │   ├── ErrorBoundary.tsx
│   │   ├── FilterPanel.tsx
│   │   └── ...
│   ├── contexts/        # React contexts (Auth, Theme, Language)
│   ├── hooks/           # Custom React hooks
│   ├── lib/             # Utilities & helpers
│   ├── pages/           # Page components (15 pages)
│   │   ├── LoginPage.tsx
│   │   ├── Dashboard.tsx
│   │   ├── CreateBill.tsx
│   │   └── ...
│   ├── services/        # API integration layer
│   │   ├── api.ts       # Axios instance
│   │   ├── auth.ts      # Auth API calls
│   │   └── bills.ts     # Bill operations
│   ├── types/           # TypeScript type definitions
│   ├── App.tsx          # Root component
│   ├── main.tsx         # Entry point
│   └── index.css        # Global styles
├── tests/               # E2E tests
├── .env.example         # Environment template
├── package.json         # Dependencies
├── tsconfig.json        # TypeScript config
├── vite.config.ts       # Vite configuration
└── tailwind.config.ts   # Tailwind configuration
```

---

## 🔧 Available Scripts

```bash
# Development
npm run dev              # Start dev server (http://localhost:5173)
npm run build            # Build for production
npm run preview          # Preview production build

# Code Quality
npm run lint             # Run ESLint
npm run type-check       # TypeScript type checking

# Testing
npm test                 # Run unit tests
npm run test:ui          # Test UI (Vitest UI)
npm run test:coverage    # Test with coverage report

# E2E Testing
npx playwright test      # Run E2E tests
npx playwright test --ui # E2E test UI
```

---

## ⚙️ Environment Variables

Create `.env` file:

```bash
# Backend API URL
VITE_API_URL=http://localhost:8080/api

# Sentry (Error Tracking) - Optional
VITE_SENTRY_DSN=your-sentry-dsn

# Environment
VITE_ENV=development
```

---

## 🧪 Testing

### Unit & Component Tests

```bash
# Run all tests
npm test

# Watch mode
npm test -- --watch

# Coverage report
npm run test:coverage
```

### E2E Tests

```bash
# Run E2E tests
npx playwright test

# Interactive mode
npx playwright test --ui

# Debug mode
npx playwright test --debug
```

**Test Coverage Goals:**
- Components: 60%+
- Services: 70%+
- Utilities: 80%+

---

## 🎨 UI Components

Uses **shadcn/ui** component system built on **Radix UI** primitives.

### Adding New Components

```bash
# Add a component
npx shadcn-ui@latest add button

# Add multiple
npx shadcn-ui@latest add card dialog
```

### Component List

Over 45 components available:
- Forms: `input`, `select`, `checkbox`, `radio-group`
- Layout: `card`, `dialog`, `sheet`, `tabs`
- Feedback: `toast`, `alert`, `progress`
- Navigation: `dropdown-menu`, `navigation-menu`
- [Full list](./components.json)

---

## 📱 PWA Support

App is installable as Progressive Web App:

1. Visit site on mobile
2. Browser will prompt "Add to Home Screen"
3. App installs with offline support

**Features:**
- Offline caching
- App icon & splash screen
- Full-screen experience
- Push notifications (future)

---

## 🌐 Browser Support

- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+
- ❌ IE 11 (not supported)

**Mobile:**
- ✅ iOS 14+ Safari
- ✅ Android 10+ Chrome

---

## 🚀 Deployment

### Build for Production

```bash
npm run build
# Output: dist/
```

### Deploy to Vercel (Recommended)

```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
vercel --prod
```

### Deploy to Netlify

```bash
# Install Netlify CLI
npm i -g netlify-cli

# Deploy
netlify deploy --prod --dir=dist
```

### Environment Variables (Production)

Set in your hosting platform:
- `VITE_API_URL` → Your production API URL
- `VITE_SENTRY_DSN` → Your Sentry DSN

---

## 🎯 Code Quality Standards

### TypeScript
- **Strict mode** enabled
- All components typed
- No `any` types (use `unknown` if needed)

### ESLint
- React hooks rules enforced
- No unused variables
- Consistent formatting

### Best Practices
- ✅ Functional components only
- ✅ Custom hooks for reusable logic
- ✅ Error boundaries for error handling
- ✅ Lazy loading for routes
- ✅ Memoization where needed

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

### Code Style
- Follow existing patterns
- Write tests for new features
- Update documentation
- Run linter before committing

---

## 📊 Performance

### Lighthouse Scores (Target)
- Performance: 95+
- Accessibility: 100
- Best Practices: 100
- SEO: 90+

### Optimizations
- Code splitting (React.lazy)
- Image optimization
- Tree shaking
- Gzip compression
- CDN for static assets

---

## 🐛 Troubleshooting

### Dev server not starting
```bash
# Clear cache
rm -rf node_modules .vite
npm install
npm run dev
```

### Build errors
```bash
# Type check
npm run type-check

# Fix lint errors
npm run lint -- --fix
```

### API connection issues
- Check `VITE_API_URL` in `.env`
- Verify backend is running
- Check CORS settings

---

## 📝 License

MIT License - see [LICENSE](LICENSE)

---

## 👥 Team

- **Developer** - [Your Name]
- **Designer** - [Designer Name]

---

## 🙏 Acknowledgments

- [React](https://react.dev/)
- [Vite](https://vitejs.dev/)
- [shadcn/ui](https://ui.shadcn.com/)
- [Radix UI](https://www.radix-ui.com/)
- [Tailwind CSS](https://tailwindcss.com/)

---

**Built with ❤️ using React + TypeScript + Vite**
