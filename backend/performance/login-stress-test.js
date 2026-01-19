/**
 * k6 Performance Test - Login Stress Test
 * 
 * Tests authentication endpoint under heavy load
 * Run: k6 run performance/login-stress-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
    scenarios: {
        login_stress: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '20s', target: 20 },
                { duration: '40s', target: 100 },
                { duration: '20s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(99)<1000'], // 99% under 1s
        errors: ['rate<0.05'],              // 5% error rate
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    const payload = JSON.stringify({
        mobile: `98765${Math.floor(10000 + Math.random() * 90000)}`,
        password: 'Test@1234',
    });

    const res = http.post(`${BASE_URL}/auth/login`, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    // We expect 401 for invalid users - that's OK for stress test
    check(res, {
        'response received': (r) => r.status === 200 || r.status === 401,
        'response time OK': (r) => r.timings.duration < 1000,
    });

    errorRate.add(res.status >= 500);
    sleep(0.5);
}
