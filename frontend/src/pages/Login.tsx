import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { authApi } from '../api/authApi';
import { userApi } from '../api/userApi';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

export const Login = () => {
    const [formData, setFormData] = useState({ loginId: '', password: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const setAccessToken = useAuthStore((state) => state.setAccessToken);
    const setUser = useAuthStore((state) => state.setUser);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const { loginId, password } = formData; // Destructure formData
            const response = await authApi.login({ loginId, password });

            // Set token first to enable authenticated requests
            setAccessToken(response.accessToken);

            // Fetch full user info to get the correct username/nickname used in posts
            try {
                const userInfo = await userApi.getMyInfo();
                setUser({
                    loginId: userInfo.loginId,
                    username: userInfo.username // This should be the real name like "강준현"
                });
            } catch (userErr) {
                console.error("Failed to fetch user info after login:", userErr);
                // Fallback to loginId if fetch fails, though likely won't match posts
                setUser({
                    loginId: loginId,
                    username: loginId
                });
            }

            navigate('/');
        } catch (err: any) {
            console.error(err);
            setError(err.response?.data?.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-md mx-auto mt-16 p-8 bg-white rounded-2xl shadow-lg border border-slate-100">
            <div className="text-center mb-8">
                <h1 className="text-2xl font-bold text-slate-800">Welcome Back</h1>
                <p className="text-slate-500 mt-2">Sign in to continue to FOODCOM</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
                <Input
                    label="ID"
                    value={formData.loginId}
                    onChange={(e) => setFormData({ ...formData, loginId: e.target.value })}
                    placeholder="Enter your ID"
                    required
                />

                <Input
                    label="Password"
                    type="password"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    placeholder="Enter your password"
                    required
                />

                {error && <div className="text-red-500 text-sm text-center">{error}</div>}

                <Button type="submit" isLoading={loading} className="w-full">
                    Sign In
                </Button>
            </form>

            <div className="mt-6 text-center text-sm text-slate-500">
                Don't have an account?{' '}
                <Link to="/signup" className="text-blue-600 font-medium hover:text-blue-700">
                    Sign up
                </Link>
            </div>
        </div>
    );
};
