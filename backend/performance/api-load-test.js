/**
 * k6 Performance Test - API Load Testing
 * 
 * Install k6: https://k6.io/docs/getting-started/installation/
 * 
 * Run: k6 run performance/api-load-test.js
 * Run with options: k6 run --vus 50 --duration 30s performance/api-load-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const loginDuration = new Trend('login_duration');
const billsDuration = new Trend('bills_duration');

// Test configuration
export const options = {
    stages: [
        { duration: '30s', target: 10 },  // Ramp up to 10 users
        { duration: '1m', target: 50 },   // Ramp up to 50 users
        { duration: '2m', target: 50 },   // Stay at 50 users
        { duration: '30s', target: 0 },   // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95% of requests under 500ms
        errors: ['rate<0.1'],               // Error rate under 10%
    },
};

const BASE_URL = 'http://localhost:8080';

// Test data
const testUser = {
    mobile: '9876543210',
    password: 'Test@1234',
};

export function setup() {
    // Login to get token
    const loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify(testUser), {
        headers: { 'Content-Type': 'application/json' },
    });

    const body = JSON.parse(loginRes.body);
    return { token: body.data?.accessToken || '' };
}

export default function (data) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${data.token}`,
    };

    // Test 1: Get Recent Bills
    const billsStart = new Date();
    const billsRes = http.get(`${BASE_URL}/bills/recent?limit=10`, { headers });
    billsDuration.add(new Date() - billsStart);

    check(billsRes, {
        'bills status is 200': (r) => r.status === 200,
        'bills response time < 200ms': (r) => r.timings.duration < 200,
    });
    errorRate.add(billsRes.status !== 200);

    sleep(1);

    // Test 2: Get Today Stats
    const statsRes = http.get(`${BASE_URL}/bills/stats/today`, { headers });
    check(statsRes, {
        'stats status is 200': (r) => r.status === 200,
    });
    errorRate.add(statsRes.status !== 200);

    sleep(1);

    // Test 3: Get Farmers
    const farmersRes = http.get(`${BASE_URL}/farmers`, { headers });
    check(farmersRes, {
        'farmers status is 200': (r) => r.status === 200,
    });
    errorRate.add(farmersRes.status !== 200);

    sleep(1);

    // Test 4: Health Check
    const healthRes = http.get(`${BASE_URL}/actuator/health`);
    check(healthRes, {
        'health status is 200': (r) => r.status === 200,
    });

    sleep(1);
}

export function teardown(data) {
    console.log('Performance test completed');
}
