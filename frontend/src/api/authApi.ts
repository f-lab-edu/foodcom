import api from './axios';

// Types based on Backend DTOs
export interface LoginRequest {
    loginId: string;
    password: string;
}

export interface SignupRequest {
    loginId: string;
    password: string;
    username: string;
    gender: 'MALE' | 'FEMALE';
    age: number;
}

export interface AccessTokenResponse {
    grantType: string;
    accessToken: string;
}

export const authApi = {
    login: async (data: LoginRequest) => {
        const response = await api.post<AccessTokenResponse>('/login', data);
        return response.data;
    },

    signup: async (data: SignupRequest) => {
        const response = await api.post('/members', data);
        return response.data;
    },

    logout: async () => {
        // Backend doesn't have explicit logout in controller shown, but typically we clear client side.
        // If backend needs cookie clearing, we might need an endpoint, but for now just clear local state.
        // Or maybe /auth/logout if it existed.
        localStorage.removeItem('accessToken');
        // We might want to call an endpoint to kill refresh token if backend supports it.
    }
};
