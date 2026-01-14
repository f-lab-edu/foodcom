import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
    accessToken: string | null;
    isAuthenticated: boolean;
    user: {
        loginId: string;
        username: string;
    } | null;

    setAccessToken: (token: string) => void;
    setUser: (user: { loginId: string; username: string }) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            accessToken: null,
            isAuthenticated: false,
            user: null,

            setAccessToken: (token) => {
                localStorage.setItem('accessToken', token); // Sync manually for Axios interceptor
                set({ accessToken: token, isAuthenticated: true });
            },
            setUser: (user) => set({ user }),
            logout: () => {
                localStorage.removeItem('accessToken');
                set({ accessToken: null, isAuthenticated: false, user: null });
            },
        }),
        {
            name: 'auth-storage', // unique name
            partialize: (state) => ({ accessToken: state.accessToken, user: state.user, isAuthenticated: state.isAuthenticated }),
        }
    )
);
