import axios from 'axios';

// 🔓 인증이 필요 없는 공개 엔드포인트 목록
const PUBLIC_ENDPOINTS = ['/members', '/login', '/auth/reissue'];

// Create Axios instance
const api = axios.create({
    baseURL: '/api',
    withCredentials: true, // Send cookies (RefreshToken)
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request Interceptor: Attach AccessToken (공개 엔드포인트 제외)
api.interceptors.request.use(
    (config) => {
        const requestUrl = config.url || '';

        // 공개 엔드포인트는 토큰을 붙이지 않음
        const isPublicEndpoint = PUBLIC_ENDPOINTS.some(endpoint => requestUrl.includes(endpoint));

        console.log(`[AXIOS REQ] ${requestUrl} | Public: ${isPublicEndpoint}`);

        if (!isPublicEndpoint) {
            const token = localStorage.getItem('accessToken');
            if (token) {
                console.log(`[AXIOS REQ] Adding token to ${requestUrl}`);
                config.headers['Authorization'] = `Bearer ${token}`;
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response Interceptor: Handle 401 and Reissue
api.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const originalRequest = error.config;
        const requestUrl = originalRequest?.url || '';

        console.log(`[AXIOS ERR] ${requestUrl} | Status: ${error.response?.status}`);

        // 공개 엔드포인트에서 발생한 에러는 reissue 시도하지 않음
        const isPublicEndpoint = PUBLIC_ENDPOINTS.some(endpoint => requestUrl.includes(endpoint));

        console.log(`[AXIOS ERR] Public: ${isPublicEndpoint} | Retry: ${originalRequest._retry}`);

        // If 401, not a public endpoint, and not already retrying
        if (error.response?.status === 401 && !isPublicEndpoint && !originalRequest._retry) {
            console.log(`[AXIOS] >>>>>> CALLING REISSUE for ${requestUrl}`);
            originalRequest._retry = true;

            try {
                // Attempt reissue (쿠키의 refresh_token 사용)
                const verifyResponse = await axios.post('/api/auth/reissue', {}, { withCredentials: true });

                const newAccessToken = verifyResponse.data.accessToken;

                // Update local storage
                localStorage.setItem('accessToken', newAccessToken);

                // Update header for original request
                originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;

                // Retry original request
                return api(originalRequest);

            } catch (refreshError) {
                // Refresh 실패 시 토큰 삭제 후 로그인 페이지로
                console.error('Refresh token expired or invalid', refreshError);
                localStorage.removeItem('accessToken');
                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

export default api;
