import { test, expect } from '@playwright/test';

test.describe('Register Page E2E Tests', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/register');
    });

    test('should display registration form', async ({ page }) => {
        await expect(page.getByRole('heading', { name: /register|sign up/i })).toBeVisible();
        await expect(page.getByLabel(/name/i)).toBeVisible();
        await expect(page.getByLabel(/mobile/i)).toBeVisible();
        await expect(page.getByLabel(/password/i)).toBeVisible();
    });

    test('should validate name is required', async ({ page }) => {
        await page.getByLabel(/mobile/i).fill('9876543210');
        await page.getByLabel(/password/i).fill('Test@1234');
        await page.getByRole('button', { name: /register|sign up/i }).click();
        await expect(page.getByText(/name.*required/i)).toBeVisible();
    });

    test('should validate mobile number format', async ({ page }) => {
        await page.getByLabel(/name/i).fill('Test User');
        await page.getByLabel(/mobile/i).fill('123'); // Invalid short number
        await page.getByLabel(/password/i).fill('Test@1234');
        await page.getByRole('button', { name: /register|sign up/i }).click();
        await expect(page.getByText(/10 digits|invalid/i)).toBeVisible();
    });

    test('should validate password strength', async ({ page }) => {
        await page.getByLabel(/name/i).fill('Test User');
        await page.getByLabel(/mobile/i).fill('9876543210');
        await page.getByLabel(/password/i).fill('123'); // Weak password
        await page.getByRole('button', { name: /register|sign up/i }).click();
        await expect(page.getByText(/password.*characters|weak/i)).toBeVisible();
    });

    test('should have link to login page', async ({ page }) => {
        await expect(page.getByRole('link', { name: /login|sign in/i })).toBeVisible();
    });
});
