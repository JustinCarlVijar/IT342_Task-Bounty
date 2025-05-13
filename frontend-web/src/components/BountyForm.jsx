import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createBountyPost } from '../api/Api';

export default function BountyForm() {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [bountyPrice, setBountyPrice] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title || !description || !bountyPrice) {
      setErrorMessage('All fields are required.');
      return;
    }
    setIsSubmitting(true);
    setErrorMessage('');
    try {
      const response = await createBountyPost({
        title,
        description,
        bountyPrice: Number(bountyPrice),
      });
      navigate(`/bounties/${response.id}`);
    } catch (error) {
      setErrorMessage(error.message || 'Failed to create bounty post.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6 bg-white rounded-lg shadow-md">
      <h2 className="text-2xl font-bold mb-6">Create a New Bounty Post</h2>
      <div>
        <div className="mb-4">
          <label htmlFor="title" className="block text-sm font-medium text-gray-700">Title</label>
          <input
            type="text"
            id="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            placeholder="Enter the title of your bounty"
            required
          />
        </div>
        <div className="mb-4">
          <label htmlFor="description" className="block text-sm font-medium text-gray-700">Description</label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            placeholder="Describe the task or problem"
            rows="4"
            required
          ></textarea>
        </div>
        <div className="mb-4">
          <label htmlFor="bountyPrice" className="block text-sm font-medium text-gray-700">Bounty Price (PHP)</label>
          <input
            type="number"
            id="bountyPrice"
            value={bountyPrice}
            onChange={(e) => setBountyPrice(e.target.value)}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            placeholder="Set the bounty price"
            min="0"
            step="0.01"
            required
          />
        </div>
        {errorMessage && (
          <div className="mb-4 text-red-600">{errorMessage}</div>
        )}
        <button
          type="button"
          onClick={handleSubmit}
          disabled={isSubmitting}
          className="w-full px-4 py-2 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50"
        >
          {isSubmitting ? 'Creating Draft...' : 'Create Bounty Post'}
        </button>
      </div>
    </div>
  );
}
