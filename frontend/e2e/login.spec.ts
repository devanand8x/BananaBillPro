import { test, expect } from '@playwright/test';

test.describe('Login Page E2E Tests', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
    });

    test('should display login form', async ({ page }) => {
        await expect(page.getByRole('heading', { name: /login|sign in/i })).toBeVisible();
        await expect(page.getByLabel(/mobile/i)).toBeVisible();
        await expect(page.getByLabel(/password/i)).toBeVisible();
        await expect(page.getByRole('button', { name: /login|sign in/i })).toBeVisible();
    });

    test('should show validation errors for empty fields', async ({ page }) => {
        await page.getByRole('button', { name: /login|sign in/i }).click();
        // Expect validation message
        await expect(page.getByText(/required|enter/i)).toBeVisible();
    });

    test('should show error for invalid credentials', async ({ page }) => {
        await page.getByLabel(/mobile/i).fill('1234567890');
        await page.getByLabel(/password/i).fill('wrongpassword');
        await page.getByRole('button', { name: /login|sign in/i }).click();

        // Expect error message
        await expect(page.getByText(/invalid|incorrect|failed/i)).toBeVisible({ timeout: 5000 });
    });

    test('should have link to register page', async ({ page }) => {
        await expect(page.getByRole('link', { name: /register|sign up/i })).toBeVisible();
    });

    test('should navigate to register page', async ({ page }) => {
        await page.getByRole('link', { name: /register|sign up/i }).click();
        await expect(page).toHaveURL(/register/);
    });
});
