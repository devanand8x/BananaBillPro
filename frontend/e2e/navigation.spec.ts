import { test, expect } from '@playwright/test';

test.describe('Navigation E2E Tests', () => {

    test('should redirect to login when not authenticated', async ({ page }) => {
        await page.goto('/dashboard');
        await expect(page).toHaveURL(/login/);
    });

    test('should redirect to login from create bill when not authenticated', async ({ page }) => {
        await page.goto('/create-bill');
        await expect(page).toHaveURL(/login/);
    });

    test('should redirect to login from bill history when not authenticated', async ({ page }) => {
        await page.goto('/bill-history');
        await expect(page).toHaveURL(/login/);
    });

    test('should have proper page title', async ({ page }) => {
        await page.goto('/login');
        await expect(page).toHaveTitle(/banana|bill/i);
    });

    test('should display 404 for unknown routes', async ({ page }) => {
        await page.goto('/unknown-page-xyz');
        await expect(page.getByText(/404|not found/i)).toBeVisible();
    });
});
