import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';
import type { SignupRequest } from '../api/authApi';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

export const Signup = () => {
    const [formData, setFormData] = useState<SignupRequest>({
        loginId: '',
        password: '',
        username: '',
        age: 20,
        gender: 'MALE'
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await authApi.signup(formData);
            navigate('/login');
        } catch (err: any) {
            console.error(err);
            const errorData = err.response?.data;
            // 에러 메시지가 객체일 수 있음 (유효성 검사 실패 시)
            if (errorData?.message) {
                if (typeof errorData.message === 'string') {
                    setError(errorData.message);
                } else if (typeof errorData.message === 'object') {
                    // {password: "비밀번호는...", loginId: "아이디는..."} 형태
                    const messages = Object.values(errorData.message).join(', ');
                    setError(messages);
                } else {
                    setError('Registration failed');
                }
            } else {
                setError(errorData?.code || 'Registration failed');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-md mx-auto mt-10 p-8 bg-white rounded-2xl shadow-lg border border-slate-100">
            <div className="text-center mb-8">
                <h1 className="text-2xl font-bold text-slate-800">Create Account</h1>
                <p className="text-slate-500 mt-2">Join FOODCOM today</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
                <Input
                    label="ID"
                    value={formData.loginId}
                    onChange={(e) => setFormData({ ...formData, loginId: e.target.value })}
                    required
                />

                <Input
                    label="Password"
                    type="password"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    required
                />

                <Input
                    label="Username"
                    value={formData.username}
                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                    required
                />

                <div className="grid grid-cols-2 gap-4">
                    <div className="flex flex-col gap-1.5">
                        <label className="text-sm font-medium text-slate-700">Age</label>
                        <input
                            type="number"
                            className="px-4 py-2 rounded-lg border border-slate-200 focus:border-blue-500 outline-none"
                            value={formData.age}
                            onChange={(e) => setFormData({ ...formData, age: parseInt(e.target.value) })}
                        />
                    </div>
                    <div className="flex flex-col gap-1.5">
                        <label className="text-sm font-medium text-slate-700">Gender</label>
                        <select
                            className="px-4 py-2 rounded-lg border border-slate-200 focus:border-blue-500 outline-none bg-white"
                            value={formData.gender}
                            onChange={(e) => setFormData({ ...formData, gender: e.target.value as 'MALE' | 'FEMALE' })}
                        >
                            <option value="MALE">Male</option>
                            <option value="FEMALE">Female</option>
                        </select>
                    </div>
                </div>

                {error && <div className="text-red-500 text-sm text-center">{error}</div>}

                <Button type="submit" isLoading={loading} className="w-full mt-2">
                    Sign Up
                </Button>
            </form>

            <div className="mt-6 text-center text-sm text-slate-500">
                Already have an account?{' '}
                <Link to="/login" className="text-blue-600 font-medium hover:text-blue-700">
                    Sign in
                </Link>
            </div>
        </div>
    );
};
