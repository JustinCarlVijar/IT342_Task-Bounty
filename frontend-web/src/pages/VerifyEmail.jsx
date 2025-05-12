import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { verifyEmail, resendVerificationCode } from '../api/Api';

export default function VerifyEmail() {
  console.log('VerifyEmail component rendered'); // Debug log
  const navigate = useNavigate();
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    console.log('Verification code submitted:', code); // Debug log

    try {
      const response = await verifyEmail(code);
      console.log('Verify email response:', response); // Debug log
      setSuccess('Email confirmed successfully!');
      setTimeout(() => navigate('/'), 2000); // Redirect to login after 2 seconds
    } catch (err) {
      console.error('Verify email error:', err); // Debug log
      setError(err.response?.data?.message || 'Invalid verification code. Please try again.');
    }
  };

  const handleResend = async () => {
    setError('');
    setSuccess('');
    console.log('Resending verification code'); // Debug log

    try {
      const response = await resendVerificationCode();
      console.log('Resend code response:', response); // Debug log
      setSuccess('Verification code resent successfully!');
    } catch (err) {
      console.error('Resend code error:', err); // Debug log
      setError(err.response?.data?.message || 'Failed to resend code. Please try again.');
    }
  };

  return (
    <div className="bg-gray-100 flex items-center justify-center min-h-screen">
      <div className="bg-white p-8 rounded-2xl shadow-md w-full max-w-md">
        <h1 className="text-3xl font-bold text-center text-gray-800 mb-6">Verify Email</h1>

        {error && <p className="text-red-500 text-center mb-4">{error}</p>}
        {success && <p className="text-green-500 text-center mb-4">{success}</p>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Verification Code</label>
            <input
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              required
              placeholder="Enter code"
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <button
            type="submit"
            className="w-full bg-blue-500 text-white py-2 rounded-lg hover:bg-blue-600 transition"
          >
            Verify
          </button>
        </form>

        <p className="text-sm text-center text-gray-600 mt-4">
          Didn't receive a code?{' '}
          <span
            onClick={handleResend}
            className="text-blue-600 underline hover:text-blue-800 cursor-pointer"
          >
            Resend Code
          </span>
        </p>
      </div>
    </div>
  );
}
