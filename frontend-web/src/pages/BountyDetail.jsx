import React, { useEffect, useState, useRef, useCallback, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { getBountyPost, getComments, createComment, voteBountyPost } from '../api/Api';
import Comment from '../components/Comment';

const PAGE_SIZE = 10;

export default function BountyDetail() {
  const { id } = useParams();
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [commentInput, setCommentInput] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const observerRef = useRef();
  const CACHE_KEY = `comments_${id}`;
  const cache = useRef({});

  const fetchPost = async () => {
    try {
      const data = await getBountyPost(id);
      setPost(data);
    } catch (err) {
      console.error('Error loading post:', err);
    }
  };

  const fetchComments = useCallback(async (pageNum) => {
    setLoading(true);
    const cacheKey = `${pageNum}`;
    if (cache.current[cacheKey]) {
      setComments(prev => [...prev, ...cache.current[cacheKey]]);
      setHasMore(cache.current[cacheKey].length === PAGE_SIZE);
      setLoading(false);
      return;
    }

    try {
      const data = await getComments(id, pageNum, PAGE_SIZE);
      const commentsData = Array.isArray(data) ? data : data.content || [];
      cache.current[cacheKey] = commentsData;
      localStorage.setItem(CACHE_KEY, JSON.stringify(cache.current));
      setComments(prev => [...prev, ...commentsData]);
      setHasMore(commentsData.length === PAGE_SIZE);
    } catch (err) {
      console.error('Error loading comments:', err.response?.data || err.message);
      setComments(prev => prev);
    } finally {
      setLoading(false);
    }
  }, [id]);

  const handlePostComment = async () => {
    if (!commentInput.trim()) return;
    try {
      await createComment(id, { content: commentInput });
      setCommentInput('');
      setComments([]);
      setPage(0);
      setHasMore(true);
      cache.current = {};
      await fetchComments(0);
    } catch (err) {
      console.error('Error posting comment:', err);
    }
  };

  const handleVote = async (type) => {
    try {
      await voteBountyPost(id, type);
      fetchPost();
    } catch (err) {
      console.error(`Error ${type}ing post:`, err);
    }
  };

  useEffect(() => {
    fetchPost();
    const savedCache = localStorage.getItem(CACHE_KEY);
    if (savedCache) {
      cache.current = JSON.parse(savedCache);
      if (cache.current['0']) {
        setComments(cache.current['0']);
        setHasMore(cache.current['0'].length === PAGE_SIZE);
        setPage(0);
      } else {
        fetchComments(0);
      }
    } else {
      fetchComments(0);
    }
  }, [id, fetchComments]);

  useEffect(() => {
    if (!hasMore || loading) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loading) {
          const nextPage = page + 1;
          fetchComments(nextPage);
          setPage(nextPage);
        }
      },
      { rootMargin: '200px' }
    );
    if (observerRef.current) observer.observe(observerRef.current);
    return () => {
      if (observerRef.current) observer.unobserve(observerRef.current);
      observer.disconnect();
    };
  }, [hasMore, loading, page, fetchComments]);

  const buildCommentTree = (comments, parentId = null) => {
    if (!Array.isArray(comments)) {
      console.warn('buildCommentTree received non-array comments:', comments);
      return [];
    }
    return comments
      .filter(c => c.parentCommentId === parentId || (parentId === null && !c.parentCommentId))
      .map(c => ({
        ...c,
        replies: buildCommentTree(comments, c.id)
      }));
  };

  const commentTree = useMemo(() => buildCommentTree(comments), [comments]);

  return (
    <div className="max-w-4xl mx-auto px-4 pt-10 pb-20">
      {post ? (
        <div className="bg-white shadow-md rounded-2xl p-8">
          <h1 className="text-3xl font-bold text-blue-700 mb-4">{post.title}</h1>
          <p className="text-gray-800 whitespace-pre-line mb-6">{post.description}</p>
          <div className="flex items-center space-x-4 text-sm text-gray-600 mb-4">
            <span>Bounty: <span className="text-green-600 font-semibold">${post.bountyPrice}</span></span>
            <button onClick={() => handleVote('upvote')} className="flex items-center space-x-1">
              <span>▲</span>
              <span>{post.upvotes}</span>
            </button>
            <button onClick={() => handleVote('downvote')} className="flex items-center space-x-1">
              <span>▼</span>
              <span>{post.downvotes}</span>
            </button>
          </div>

          <hr className="my-6" />

          <h2 className="text-lg font-semibold mb-2">Comments</h2>

          <div className="mb-6">
            <textarea
              rows={3}
              className="w-full border rounded-lg p-2 text-sm"
              placeholder="Add a comment..."
              value={commentInput}
              onChange={(e) => setCommentInput(e.target.value)}
            />
            <button
              onClick={handlePostComment}
              className="mt-2 px-4 py-2 text-sm bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              Comment
            </button>
          </div>

          <div className="space-y-4">
            {commentTree.length === 0 ? (
              <p className="text-gray-500">No comments yet.</p>
            ) : (
              commentTree.map(comment => (
                <Comment
                  key={comment.id}
                  comment={comment}
                  postId={id}
                  refreshComments={() => {
                    setComments([]);
                    setPage(0);
                    setHasMore(true);
                    cache.current = {};
                    fetchComments(0);
                  }}
                />
              ))
            )}
          </div>
          <div ref={observerRef} className="h-20 flex justify-center items-center">
            {loading && <p>Loading comments...</p>}
            {!hasMore && comments.length > 0 && <p>No more comments.</p>}
          </div>
        </div>
      ) : (
        <p className="text-center mt-10 text-gray-500">Loading post...</p>
      )}
    </div>
  );
}
