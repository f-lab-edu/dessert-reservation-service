import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

/**
 * Scenario 5: Concurrency Accuracy Verification Test
 *
 * Purpose: Verify that pessimistic locking prevents overselling
 *
 * Test Setup:
 * - Dessert with 10 items in stock (pre-configured in DB)
 * - 500 VUs attempting to reserve 2 items each simultaneously
 * - Expected: Only 5 reservations succeed (5 × 2 = 10 items)
 * - Expected: 495 reservations fail (insufficient inventory)
 *
 * Success Criteria:
 * - Successful reservations: exactly 5
 * - Failed reservations: 495
 * - No overselling (success count must not exceed 5)
 *
 * Usage:
 * k6 run -e DESSERT_ID=149 -e USERNAME=user1@example.com -e PASSWORD=password1234 \
 *   -e BASE_URL=http://localhost:80 scenario5-concurrency-accuracy.js
 */

// Custom Metrics
const successCounter = new Counter('reservation_success');
const failureCounter = new Counter('reservation_failure');
const conflict409Counter = new Counter('http_409_conflict');
const badRequest400Counter = new Counter('http_400_bad_request');
const otherErrorCounter = new Counter('http_other_errors');

// Options
export const options = {
  scenarios: {
    concurrency_test: {
      executor: 'shared-iterations',
      vus: 50,
      iterations: 500,
      maxDuration: '5m',
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<3000', 'p(99)<5000'],
    'http_req_failed': ['rate<0.99'], // Most requests will fail (495/500), this is expected
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:80';

export default function () {
  const dessertId = __ENV.DESSERT_ID || '149';
  const username = __ENV.USERNAME || 'user1@example.com';
  const password = __ENV.PASSWORD || 'test1234';

  // Step 1: Login to obtain session cookie
  const loginPayload = `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;

  const loginRes = http.post(`${BASE_URL}/login`, loginPayload, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    redirects: 0, // Do not follow redirects
  });

  const loginSuccess = check(loginRes, {
    'login successful': (r) => r.status === 200 || r.status === 302,
  });

  if (!loginSuccess) {
    console.error(`Login failed with status ${loginRes.status}, body: ${loginRes.body}`);
    failureCounter.add(1);
    return;
  }

  // Extract JSESSIONID cookie
  const cookies = loginRes.cookies;
  let sessionCookie = '';
  for (let name in cookies) {
    if (name === 'JSESSIONID') {
      sessionCookie = cookies[name][0].value;
      break;
    }
  }

  if (!sessionCookie) {
    console.error('Failed to extract JSESSIONID from login response');
    failureCounter.add(1);
    return;
  }

  // Step 2: Attempt to make a reservation
  const reservationPayload = JSON.stringify({
    dessertId: parseInt(dessertId),
    count: 2,
  });

  const headers = {
    'Content-Type': 'application/json',
    'Cookie': `JSESSIONID=${sessionCookie}`,
  };

  const reservationRes = http.post(
    `${BASE_URL}/api/reservations`,
    reservationPayload,
    { headers: headers }
  );

  const status = reservationRes.status;

  // Classify response and update metrics
  if (status === 200 || status === 201) {
    successCounter.add(1);
    check(reservationRes, {
      'reservation successful': () => true,
    });
  } else {
    failureCounter.add(1);

    if (status === 409) {
      conflict409Counter.add(1);
      check(reservationRes, {
        'expected conflict (inventory exhausted)': () => true,
      });
    } else if (status === 400) {
      badRequest400Counter.add(1);
      check(reservationRes, {
        'bad request': () => true,
      });
    } else {
      otherErrorCounter.add(1);
      check(reservationRes, {
        'other error': () => true,
      });
    }
  }
}

/**
 * Custom summary handler to verify concurrency accuracy.
 * Prints detailed results and checks for overselling.
 */
export function handleSummary(data) {
  const successCount = data.metrics.reservation_success?.values?.count || 0;
  const failureCount = data.metrics.reservation_failure?.values?.count || 0;
  const conflict409Count = data.metrics.http_409_conflict?.values?.count || 0;
  const badRequest400Count = data.metrics.http_400_bad_request?.values?.count || 0;
  const otherErrorCount = data.metrics.http_other_errors?.values?.count || 0;

  const totalAttempts = successCount + failureCount;
  const oversellingDetected = successCount > 5;
  const accuracyPassed = successCount === 5 && failureCount === 495;

  console.log('\n╔═══════════════════════════════════════════════════════════════╗');
  console.log('║   Concurrency Accuracy Verification Results                  ║');
  console.log('╚═══════════════════════════════════════════════════════════════╝');
  console.log('');
  console.log(`  Total Attempts:           ${totalAttempts}`);
  console.log(`  Successful Reservations:  ${successCount} (Expected: 5)`);
  console.log(`  Failed Reservations:      ${failureCount} (Expected: 495)`);
  console.log('');
  console.log('  Failure Breakdown:');
  console.log(`    - HTTP 409 (Conflict):   ${conflict409Count}`);
  console.log(`    - HTTP 400 (Bad Req):    ${badRequest400Count}`);
  console.log(`    - Other Errors:          ${otherErrorCount}`);
  console.log('');
  console.log('  ─────────────────────────────────────────────────────────────');
  console.log(`  Overselling Detected:     ${oversellingDetected ? '⚠️  YES (FAILURE)' : '✓ NO (PASS)'}`);
  console.log(`  Accuracy Test Result:     ${accuracyPassed ? '✓ PASS' : '⚠️  FAIL'}`);
  console.log('  ─────────────────────────────────────────────────────────────');
  console.log('');

  if (oversellingDetected) {
    console.log('  ⚠️  CRITICAL: Overselling detected! Pessimistic locking may be broken.');
    console.log(`     More than 5 reservations succeeded (actual: ${successCount})`);
  } else if (!accuracyPassed) {
    console.log('  ⚠️  WARNING: Test did not produce expected results.');
    console.log(`     Expected: 5 successes, 495 failures`);
    console.log(`     Actual: ${successCount} successes, ${failureCount} failures`);
  } else {
    console.log('  ✓ SUCCESS: Pessimistic locking working correctly!');
    console.log('     Exactly 5 reservations succeeded, no overselling occurred.');
  }

  console.log('');
  console.log('═══════════════════════════════════════════════════════════════');
  console.log('');

  // Generate file paths with proper format
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');

  const dateStr = `${year}-${month}-${day}`;
  const timeStr = `${hours}-${minutes}-${seconds}`;
  const result = accuracyPassed ? 'PASS' : 'FAIL';

  const baseFilename = `scenario5_${result}_${timeStr}`;

  // Summary text
  const summaryText = `
╔═══════════════════════════════════════════════════════════════╗
║   Concurrency Accuracy Verification Results                  ║
╚═══════════════════════════════════════════════════════════════╝

Total Attempts:           ${totalAttempts}
Successful Reservations:  ${successCount} (Expected: 5)
Failed Reservations:      ${failureCount} (Expected: 495)

Failure Breakdown:
  - HTTP 409 (Conflict):   ${conflict409Count}
  - HTTP 400 (Bad Req):    ${badRequest400Count}
  - Other Errors:          ${otherErrorCount}

─────────────────────────────────────────────────────────────
Overselling Detected:     ${oversellingDetected ? 'YES (FAILURE)' : 'NO (PASS)'}
Accuracy Test Result:     ${accuracyPassed ? 'PASS' : 'FAIL'}
─────────────────────────────────────────────────────────────

${oversellingDetected ?
  `⚠️  CRITICAL: Overselling detected! Pessimistic locking may be broken.
     More than 5 reservations succeeded (actual: ${successCount})` :
  !accuracyPassed ?
  `⚠️  WARNING: Test did not produce expected results.
     Expected: 5 successes, 495 failures
     Actual: ${successCount} successes, ${failureCount} failures` :
  `✓ SUCCESS: Pessimistic locking working correctly!
     Exactly 5 reservations succeeded, no overselling occurred.`
}

═══════════════════════════════════════════════════════════════
`;

  return {
    'stdout': summaryText,
    [`../results/${baseFilename}.json`]: JSON.stringify(data, null, 2),
    [`../results/${baseFilename}_summary.txt`]: summaryText,
  };
}
