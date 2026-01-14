import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    error?: string;
    inputClassName?: string;
}

export const Input: React.FC<InputProps> = ({ label, error, className = '', inputClassName = '', ...props }) => {
    return (
        <div className={`flex flex-col gap-1.5 ${className}`}>
            {label && <label className="text-sm font-medium text-slate-700">{label}</label>}
            <input
                className={`px-4 py-2 rounded-lg border border-slate-200 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 outline-none transition-all duration-200 bg-white placeholder:text-slate-400 ${error ? 'border-red-500 focus:border-red-500 focus:ring-red-500/10' : ''} ${inputClassName}`}
                {...props}
            />
            {error && <span className="text-xs text-red-500 font-medium">{error}</span>}
        </div>
    );
};
