import React from 'react';
import { useNavigate } from 'react-router-dom';

export function Login() {
    const navigate = useNavigate();

    return (
        <div className="bg-gray-100 flex items-center justify-center min-h-screen">
            {/* Container takes full width of the screen */}
            <div className="bg-white p-8 rounded-2xl shadow-md w-full">
                {/* Header */}
                <div className="mb-6">
                    <h1 className="text-3xl font-bold text-center text-gray-800">Login</h1>
                </div>

                {/* Login Form */}
                <form className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Username</label>
                        <input
                            type="text"
                            className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
                        <input
                            type="password"
                            className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-blue-500 text-white py-2 rounded-lg hover:bg-blue-600 transition"
                    >
                        Sign In
                    </button>
                </form>

                {/* Register Link */}
                <p className="text-sm text-center text-gray-600 mt-4">
                    Don't have an account?{' '}
                    <span
                        onClick={() => navigate('/register')}
                        className="text-blue-600 underline hover:text-blue-800 cursor-pointer"
                    >
                        Register
                    </span>
                </p>
            </div>
        </div>
    );
}
