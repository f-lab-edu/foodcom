import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export let options = {
    stages: [
        { duration: '30s', target: 20 }, // 30초 동안 20명까지 램프업
        { duration: '1m', target: 50 },  // 1분 동안 50명 유지 (부하 구간)
        { duration: '30s', target: 0 },  // 30초 동안 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 95% 요청이 2초 이내
        http_req_failed: ['rate<0.01'],    // 에러율 1% 미만
    },
};

const BASE_URL = 'https://foodcom-backend-75378124076.asia-northeast3.run.app';

export default function () {
    // 8:2 비율로 Read(Replica)와 Write(Master) 분산
    let isWrite = Math.random() < 0.2; // 20% 확률

    if (isWrite) {
        // === Write Scenario: 회원가입 (Master DB) ===
        // 매번 새로운 유저 ID 생성 (충돌 방지, Base36 사용으로 길이 단축)
        const uniqueId = `L${__VU}${__ITER}${Date.now().toString(36)}`; // L50999l4y6abcd (약 15-18자)

        const payload = JSON.stringify({
            loginId: uniqueId.substring(0, 20), // 20자 제한 맞춤
            password: 'password1234!',
            username: 'LoadTester',
            gender: 'MALE',
            age: 25
        });

        const params = {
            headers: {
                'Content-Type': 'application/json',
            },
            tags: { name: 'Signup_Write' } // 결과 태그
        };

        let res = http.post(`${BASE_URL}/members`, payload, params);

        // 추가: 에러 트래킹
        if (res.status !== 201) {
            console.error(`Signup Failed! Status: ${res.status} Body: ${res.body}`);
        }

        check(res, {
            'Signup status is 201': (r) => r.status === 201,
            'Signup duration < 1s': (r) => r.timings.duration < 1000,
        });

    } else {
        // === Read Scenario: 게시글 목록 조회 (Replica DB) ===
        // Transactional(readOnly=true) 적용된 API
        const params = {
            tags: { name: 'GetPosts_Read' }
        };

        let res = http.get(`${BASE_URL}/posts`, params);

        check(res, {
            'GetPosts status is 200': (r) => r.status === 200,
            'GetPosts duration < 500ms': (r) => r.timings.duration < 500,
        });
    }

    sleep(1);
}
