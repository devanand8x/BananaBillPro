/**
 * k6 Performance Test - Spike Test
 * 
 * Tests how the system handles sudden traffic spikes
 * Run: k6 run performance/spike-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
    scenarios: {
        spike: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '10s', target: 10 },   // Normal load
                { duration: '5s', target: 200 },   // Spike!
                { duration: '30s', target: 200 },  // Stay at spike
                { duration: '5s', target: 10 },    // Scale down
                { duration: '10s', target: 0 },    // Recovery
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        errors: ['rate<0.2'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    // Health check - simple endpoint
    const res = http.get(`${BASE_URL}/actuator/health`);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 2s': (r) => r.timings.duration < 2000,
    });

    errorRate.add(res.status !== 200);
    sleep(0.1);
}
