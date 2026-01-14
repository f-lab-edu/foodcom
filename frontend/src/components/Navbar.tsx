import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { Button } from './ui/Button';
import { LogOut, User, PenSquare } from 'lucide-react';

export const Navbar = () => {
    const { isAuthenticated, logout, user } = useAuthStore();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="bg-white/80 backdrop-blur-md border-b border-slate-200 sticky top-0 z-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    <Link to="/" className="flex items-center gap-2">
                        <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                            FOODCOM
                        </span>
                    </Link>

                    <div className="flex items-center gap-4">
                        {isAuthenticated ? (
                            <>
                                <Link to="/write">
                                    <Button variant="ghost" className="gap-2">
                                        <PenSquare size={18} />
                                        Write
                                    </Button>
                                </Link>
                                <Link to="/mypage">
                                    <Button variant="ghost" className="gap-2">
                                        <User size={18} />
                                        {user?.username || 'My Page'}
                                    </Button>
                                </Link>
                                <Button variant="secondary" onClick={handleLogout} className="gap-2">
                                    <LogOut size={16} /> Logout
                                </Button>
                            </>
                        ) : (
                            <>
                                <Link to="/login">
                                    <Button variant="ghost">Login</Button>
                                </Link>
                                <Link to="/signup">
                                    <Button>Sign Up</Button>
                                </Link>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
};
