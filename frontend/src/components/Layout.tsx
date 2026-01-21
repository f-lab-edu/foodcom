import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';

export const Layout = () => {
    return (
        <div className="min-h-screen bg-slate-50 flex flex-col">
            <Navbar />
            <main className="flex-1 max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-8">
                <Outlet />
            </main>
            <footer className="bg-white border-t border-slate-200 py-6 mt-auto">
                <div className="max-w-7xl mx-auto px-4 text-center text-slate-500 text-sm">
                    &copy; {new Date().getFullYear()} FOODCOM. All rights reserved.
                </div>
            </footer>
        </div>
    );
};
