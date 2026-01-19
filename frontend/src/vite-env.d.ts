/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly VITE_API_URL?: string;
    readonly VITE_SENTRY_DSN?: string;
    readonly MODE: string;
    readonly PROD: boolean;
    readonly DEV: boolean;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}

// Declare Sentry module as optional to avoid build errors if not installed
declare module '@sentry/react' {
    export function init(options: any): void;
    export function browserTracingIntegration(): any;
    export function replayIntegration(): any;
}
