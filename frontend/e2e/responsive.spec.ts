import { test, expect } from '@playwright/test';

test.describe('Responsive Design E2E Tests', () => {

    test('login page should be responsive on mobile', async ({ page }) => {
        await page.setViewportSize({ width: 375, height: 667 }); // iPhone SE
        await page.goto('/login');
        await expect(page.getByRole('button', { name: /login|sign in/i })).toBeVisible();
    });

    test('login page should be responsive on tablet', async ({ page }) => {
        await page.setViewportSize({ width: 768, height: 1024 }); // iPad
        await page.goto('/login');
        await expect(page.getByRole('button', { name: /login|sign in/i })).toBeVisible();
    });

    test('login page should be responsive on desktop', async ({ page }) => {
        await page.setViewportSize({ width: 1920, height: 1080 }); // Desktop
        await page.goto('/login');
        await expect(page.getByRole('button', { name: /login|sign in/i })).toBeVisible();
    });

    test('register page should be responsive on mobile', async ({ page }) => {
        await page.setViewportSize({ width: 375, height: 667 });
        await page.goto('/register');
        await expect(page.getByRole('button', { name: /register|sign up/i })).toBeVisible();
    });

    test('forms should be scrollable on small screens', async ({ page }) => {
        await page.setViewportSize({ width: 320, height: 480 }); // Small phone
        await page.goto('/login');
        // Page should load without horizontal scroll
        const bodyWidth = await page.evaluate(() => document.body.scrollWidth);
        const viewportWidth = await page.evaluate(() => window.innerWidth);
        expect(bodyWidth).toBeLessThanOrEqual(viewportWidth + 10); // Allow small margin
    });
});
