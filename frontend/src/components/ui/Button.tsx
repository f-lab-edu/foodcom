import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    isLoading?: boolean;
    variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
}

export const Button = ({
    children,
    isLoading,
    variant = 'primary',
    className = '',
    disabled,
    ...props
}: ButtonProps) => {
    const baseStyles = "px-4 py-2 rounded-xl font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed active:scale-95";

    const variants = {
        primary: "bg-blue-600 text-white hover:bg-blue-700 shadow-lg shadow-blue-200",
        secondary: "bg-white text-slate-700 border border-slate-200 hover:bg-slate-50 hover:border-slate-300",
        danger: "bg-red-500 text-white hover:bg-red-600 shadow-lg shadow-red-200",
        ghost: "bg-transparent text-slate-600 hover:bg-slate-100",
        outline: "bg-transparent border border-slate-300 text-slate-600 hover:bg-slate-50"
    };

    return (
        <button
            className={`${baseStyles} ${variants[variant]} ${className}`}
            disabled={disabled || isLoading}
            {...props}
        >
            {isLoading ? (
                <div className="flex items-center justify-center gap-2">
                    <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
                    <span>Loading...</span>
                </div>
            ) : children}
        </button>
    );
};
