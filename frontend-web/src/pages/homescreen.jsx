// src/pages/HomeScreen.jsx
import { useState } from 'react';
import PostForm from '../components/PostForm';
import PostList from '../components/PostList';

const HomeScreen = () => {
  const [posts, setPosts] = useState([]);

  const handlePostSubmit = (postContent) => {
    setPosts([...posts, postContent]);
  };

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <PostForm onPostSubmit={handlePostSubmit} />
      <PostList posts={posts} />
    </div>
  );
};

export default HomeScreen;
