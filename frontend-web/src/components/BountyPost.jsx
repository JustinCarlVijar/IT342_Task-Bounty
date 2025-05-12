import React from 'react';
import { useNavigate } from 'react-router-dom';

export default function BountyPost({ post }) {
  const navigate = useNavigate();

  return (
    <div
      onClick={() => navigate(`/bounties/${post.id}`)}
      className="bg-white shadow-md rounded-xl p-6 hover:shadow-lg transition cursor-pointer"
    >
      <h2 className="text-xl font-semibold text-blue-700 mb-2">{post.title}</h2>
      <p className="text-gray-700 line-clamp-3 mb-4">{post.description}</p>
      <div className="flex justify-between text-sm text-gray-600">
        <span>Bounty: <strong className="text-green-600">${post.bountyPrice}</strong></span>
        <span>▲ {post.upvotes} ▼ {post.downvotes}</span>
      </div>
    </div>
  );
}

