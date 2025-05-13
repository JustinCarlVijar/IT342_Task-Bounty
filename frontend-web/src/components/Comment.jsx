import React, { useState } from 'react';
import { createComment } from '../api/Api';

export default function Comment({ comment, postId, refreshComments }) {
  const [isReplying, setIsReplying] = useState(false);
  const [replyContent, setReplyContent] = useState('');

  const handleReply = async () => {
    if (!replyContent.trim()) return;
    try {
      await createComment(postId, {
        parentCommentId: comment.id,
        content: replyContent,
      });
      setReplyContent('');
      setIsReplying(false);
      refreshComments();
    } catch (err) {
      console.error('Reply failed:', err);
    }
  };

  return (
    <div className="mb-4 pl-4 border-l-2 border-gray-200">
      <p className="text-sm text-gray-800 whitespace-pre-line">{comment.content}</p>
      <p className="text-xs text-gray-500">by {comment.authorUsername || 'Unknown'}</p>
      <button
        onClick={() => setIsReplying(!isReplying)}
        className="text-xs text-blue-500 hover:underline mt-1"
      >
        Reply
      </button>

      {isReplying && (
        <div className="mt-2">
          <textarea
            rows={2}
            className="w-full border rounded p-2 text-sm"
            value={replyContent}
            onChange={(e) => setReplyContent(e.target.value)}
            placeholder="Write a reply..."
          />
          <div className="mt-1 flex gap-2">
            <button
              onClick={handleReply}
              className="text-xs px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              Reply
            </button>
            <button
              onClick={() => setIsReplying(false)}
              className="text-xs px-2 py-1 text-gray-500 hover:underline"
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
