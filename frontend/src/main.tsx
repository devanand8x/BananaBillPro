import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "./index.css";

// Initialize Sentry for error tracking (completely optional)
// Only loads in production with DSN configured
// Skip in development to avoid module resolution errors
if (import.meta.env.PROD && import.meta.env.VITE_SENTRY_DSN) {
  // Use dynamic import to avoid loading in development
  // The @vite-ignore comment tells Vite to skip static analysis
  import(/* @vite-ignore */ './sentry')
    .then((module) => {
      if (module?.initSentry && typeof module.initSentry === 'function') {
        module.initSentry().catch(() => {
          // Silently fail if Sentry initialization fails
        });
      }
    })
    .catch(() => {
      // Sentry module or package not available - expected if not installed
      // Silently continue without error tracking
    });
}

createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
