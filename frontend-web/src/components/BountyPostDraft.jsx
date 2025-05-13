import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createCheckoutSession } from '../api/Api'; // Adjust path based on your project structure

export default function BountyPostDraft({ post }) {
  const navigate = useNavigate();
  const [isPublishing, setIsPublishing] = useState(false);
  const [error, setError] = useState(null);

  const handlePublish = async () => {
    setIsPublishing(true);
    setError(null);
    try {
      console.log('Initiating checkout for bountyPostId:', post.id); // Debug
      const response = await createCheckoutSession(post.id);
      console.log('Checkout response:', response); // Debug full response
      // Handle both string URL and object with url field
      const checkoutUrl = typeof response === 'string' ? response : response?.url;
      if (!checkoutUrl) {
        throw new Error('No checkout URL returned from API. Response: ' + JSON.stringify(response));
      }
      window.location.href = checkoutUrl; // Redirect to Stripe checkout
    } catch (err) {
      console.error('Checkout error:', err);
      setError(err.message || 'Failed to initiate checkout. Please try again.');
      setIsPublishing(false);
    }
  };

  return (
    <div className="bg-white shadow-md rounded-xl p-6 hover:shadow-lg transition">
      <h2
        onClick={() => navigate(`/bounties/${post.id}`)}
        className="text-xl font-semibold text-blue-700 mb-2 cursor-pointer hover:underline"
      >
        {post.title}
      </h2>
      <p className="text-gray-700 line-clamp-3 mb-4">{post.description}</p>
      <div className="flex justify-between items-center text-sm text-gray-600">
        <span>Bounty: <strong className="text-green-600">${post.bountyPrice}</strong></span>
        <button
          onClick={handlePublish}
          disabled={isPublishing}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
        >
          {isPublishing ? 'Processing...' : 'Publish'}
        </button>
      </div>
      {error && <p className="text-red-500 mt-2">{error}</p>}
    </div>
  );
}
