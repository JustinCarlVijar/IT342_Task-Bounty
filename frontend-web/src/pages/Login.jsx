import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginUser } from '../api/Api';

export function Login() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    identifier: '',
    password: '',
  });
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const response = await loginUser({
        identifier: formData.identifier,
        password: formData.password,
      });
      console.log('Login success:', response);
      navigate('/dashboard');
    } catch (err) {
      console.error('Login error:', err);
      setError(err?.message || 'Login failed. Please check your credentials.');
    }
  };

  return (
    <div className="bg-gray-100 min-h-screen flex flex-col md:flex-row items-center justify-center px-4">
      {/* Left Side: Branding */}
      <div className="flex-1 mb-10 md:mb-0 md:mr-10 text-center md:text-left">
        <h1 className="text-5xl font-bold text-blue-600">TaskBounty</h1>
        <p className="mt-4 text-lg text-gray-700 max-w-md">
          Connect. Collaborate. Get rewarded for your expertise.
        </p>
      </div>

      {/* Right Side: Login Form */}
      <div className="w-full max-w-md bg-white p-8 rounded-2xl shadow-md">
        <h2 className="text-2xl font-semibold text-center text-gray-800 mb-6">Log In</h2>

        {error && <p className="text-red-500 text-center mb-4">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email or Username</label>
            <input
              type="text"
              name="identifier"
              value={formData.identifier}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition"
          >
            Sign In
          </button>
        </form>

        <p className="text-sm text-center text-gray-600 mt-4">
          Don&apos;t have an account?{' '}
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

