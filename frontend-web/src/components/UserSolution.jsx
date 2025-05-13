import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { PencilIcon, EyeIcon, TrashIcon } from '@heroicons/react/24/solid';
import { getBountyPost, getUserProfile } from '../api/Api';

const MySolution = ({ solution, onEdit, onDelete }) => {
  const navigate = useNavigate();
  const [isExpanded, setIsExpanded] = useState(false);
  const [bountyTitle, setBountyTitle] = useState('Loading...');
  const [submitter, setSubmitter] = useState('Loading...');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDetails = async () => {
      setLoading(true);
      try {
        // Fetch bounty post title
        const bounty = await getBountyPost(solution.bountyPostId);
        setBountyTitle(bounty.title || 'Unknown Bounty');

        // Fetch submitter username
        const user = await getUserProfile(solution.submitterId);
        setSubmitter(user.username || 'Unknown User');
      } catch (err) {
        console.error('Error fetching solution details:', err);
        setError('Failed to load solution details.');
        setBountyTitle('Unknown Bounty');
        setSubmitter('Unknown User');
      } finally {
        setLoading(false);
      }
    };
    fetchDetails();
  }, [solution.bountyPostId, solution.submitterId]);

  const handleCardClick = () => {
    navigate(`/dashboard/solutions/${solution.id}`);
  };

  return (
    <div className="bg-white border rounded-lg shadow-sm p-4 mb-4">
      {/* Card Header */}
      <div
        className="cursor-pointer flex justify-between items-center"
        onClick={handleCardClick}
      >
        <div>
          <h3 className="text-lg font-semibold text-gray-800">
            Solution for {bountyTitle}
          </h3>
          <p className="text-sm text-gray-600">Bounty: {bountyTitle}</p>
          <p className="text-sm text-gray-600">Submitted by: {submitter}</p>
        </div>
        <div className="flex items-center space-x-2">
          <span
            className={`px-2 py-1 text-xs font-medium rounded-full ${
              solution.approved ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
            }`}
          >
            {solution.approved ? 'Approved' : 'Not Approved'}
          </span>
        </div>
      </div>

      {/* Expanded Details */}
      {isExpanded && (
        <div className="mt-4 border-t pt-4">
          {error && <p className="text-red-500">{error}</p>}
          {loading ? (
            <p>Loading details...</p>
          ) : (
            <>
              <p className="text-gray-700">{solution.content}</p>
              <p className="text-sm text-gray-600 mt-2">
                Submitted: {new Date(solution.createdAt).toLocaleString()}
              </p>
              <div className="mt-4 flex space-x-2">
                {!solution.approved && (
                  <>
                    <button
                      onClick={() => onEdit(solution.id)}
                      className="flex items-center px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                    >
                      <PencilIcon className="w-4 h-4 mr-1" />
                      Edit
                    </button>
                    <button
                      onClick={() => onDelete(solution.id)}
                      className="flex items-center px-3 py-1 bg-red-600 text-white rounded-md hover:bg-red-700"
                    >
                      <TrashIcon className="w-4 h-4 mr-1" />
                      Delete
                    </button>
                  </>
                )}
                <button
                  onClick={() => setIsExpanded(false)}
                  className="flex items-center px-3 py-1 bg-gray-600 text-white rounded-md hover:bg-gray-700"
                >
                  <EyeIcon className="w-4 h-4 mr-1" />
                  View
                </button>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default MySolution;
