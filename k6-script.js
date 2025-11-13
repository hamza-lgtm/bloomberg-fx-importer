import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 }, 
    { duration: '1m', target: 20 },  
    { duration: '10s', target: 0 },  
  ],
};


function generatePayload() {
  const uniqueId = `k6-${__VU}-${__ITER}`;
  return [
    {
      dealUniqueId: uniqueId,
      fromCurrency: "USD",
      toCurrency: "JPY",
      dealTimestamp: new Date().toISOString(),
      dealAmount: 1000 + (Math.random() * 100),
    },
  ];
}

export default function () {
  const url = 'http://localhost:8080/api/v1/deals/import';
  const payload = JSON.stringify(generatePayload());
  
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 201': (r) => r.status === 201,
    'successCount is 1': (r) => r.json('successCount') === 1,
  });

  sleep(1);
}