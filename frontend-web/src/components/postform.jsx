import { useState } from 'react';

const PostForm = ({ onPostSubmit }) => {
  const [postContent, setPostContent] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (postContent) {
      onPostSubmit(postContent);
      setPostContent('');
    }
  };

  return (
    <div className="max-w-lg mx-auto p-4">
        <h2 className="text-2xl font-bold text-center mb-4">Create a Post</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
            <textarea
                className="w-full p-2 border rounded-md"
                placeholder="Write something..."
                value={postContent}
                onChange={(e) => setPostContent(e.target.value)}
            />
            <button
                type="submit"
                className="w-full bg-blue-500 text-white py-2 rounded-md"
            >
            Post
            </button>
        </form>
    </div>
  );
};

export default PostForm;
