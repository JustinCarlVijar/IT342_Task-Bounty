import React, { useState } from 'react';
import { PencilIcon, EyeIcon, TrashIcon } from '@heroicons/react/24/solid';

const MySolution = ({ solution, onEdit, onDelete }) => {
  const [isExpanded, setIsExpanded] = useState(false);

  return (
    <div className="bg-white border rounded-lg shadow-sm p-4 mb-4">
      {/* Card Header */}
      <div
        className="cursor-pointer flex justify-between items-center"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <div>
          <h3 className="text-lg font-semibold text-gray-800">{solution.title}</h3>
          <p className="text-sm text-gray-600">Bounty: {solution.bountyTitle}</p>
          <p className="text-sm text-gray-600">Submitted by: {solution.user}</p>
        </div>
        <div className="flex items-center space-x-2">
          <span
            className={`px-2 py-1 text-xs font-medium rounded-full ${
              solution.isApproved ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
            }`}
          >
            {solution.isApproved ? 'Approved' : 'Not Approved'}
          </span>
        </div>
      </div>

      {/* Expanded Details */}
      {isExpanded && (
        <div className="mt-4 border-t pt-4">
          <p className="text-gray-700">{solution.content}</p>
          <div className="mt-4 flex space-x-2">
            <button
              onClick={() => onEdit(solution.id)}
              className="flex items-center px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              <PencilIcon className="w-4 h-4 mr-1" />
              Edit
            </button>
            <button
              onClick={() => setIsExpanded(false)}
              className="flex items-center px-3 py-1 bg-gray-600 text-white rounded-md hover:bg-gray-700"
            >
              <EyeIcon className="w-4 h-4 mr-1" />
              View
            </button>
            <button
              onClick={() => onDelete(solution.id)}
              className="flex items-center px-3 py-1 bg-red-600 text-white rounded-md hover:bg-red-700"
            >
              <TrashIcon className="w-4 h-4 mr-1" />
              Delete
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default MySolution;
