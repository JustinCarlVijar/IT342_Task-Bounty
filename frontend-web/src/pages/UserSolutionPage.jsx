import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getMyBountyPosts, getSolutions, approveSolutionPayout } from '../api/Api';
import UserSolution from '../components/UserSolution';

const PAGE_SIZE = 10;

const UserSolutionPage = () => {
  const { bountyPostId } = useParams();
  const navigate = useNavigate();
  const [solutions, setSolutions] = useState([]);
  const [bountyPosts, setBountyPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isCacheLoaded, setIsCacheLoaded] = useState(false);
  const observerRef = useRef();
  const cache = useRef({});
  const CACHE_KEY = `user_solutions_${bountyPostId || 'list'}`;

  // Load cache from localStorage
  useEffect(() => {
    const savedCache = localStorage.getItem(CACHE_KEY);
    if (savedCache) {
      try {
        cache.current = JSON.parse(savedCache);
      } catch (e) {
        console.error('Failed to parse cache from localStorage:', e);
        localStorage.removeItem(CACHE_KEY);
      }
    }
    setIsCacheLoaded(true);
  }, [bountyPostId]);

  // Memoized cache update function
  const updateCache = useCallback((key, data) => {
    cache.current[key] = data;
    try {
      localStorage.setItem(CACHE_KEY, JSON.stringify(cache.current));
    } catch (e) {
      console.error('Failed to save cache to localStorage:', e);
    }
  }, []);

  // Fetch user's bounty posts with solutions
  const fetchBountyPosts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const posts = await getMyBountyPosts(0, 100, 'newest', '');
      const postsWithSolutions = [];
      for (const post of posts) {
        const solutions = await getSolutions(post.id, 0, 1);
        if (solutions.length > 0) {
          postsWithSolutions.push(post);
        }
      }
      setBountyPosts(postsWithSolutions);
    } catch (err) {
      console.error('Failed to fetch bounty posts:', err);
      setError(err.message || 'An error occurred while fetching bounty posts.');
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch solutions for a specific bounty post
  const fetchSolutions = useCallback(
    async (pageNum, isNewFetch = false) => {
      if (!bountyPostId) return;
      setLoading(true);
      setError(null);
      const cacheKey = `${pageNum}`;

      if (!isNewFetch && cache.current[cacheKey]) {
        setSolutions(prev => [...prev, ...cache.current[cacheKey]]);
        setHasMore(cache.current[cacheKey].length === PAGE_SIZE);
        setPage(pageNum);
        setLoading(false);
        return;
      }

      try {
        const data = await getSolutions(bountyPostId, pageNum, PAGE_SIZE);
        updateCache(cacheKey, data);
        setSolutions(prev => (isNewFetch ? data : [...prev, ...data]));
        setHasMore(data.length === PAGE_SIZE);
        setPage(pageNum);
      } catch (err) {
        console.error('Failed to fetch solutions:', err);
        setError(err.message || 'An error occurred while fetching solutions.');
      } finally {
        setLoading(false);
      }
    },
    [bountyPostId, updateCache]
  );

  // Initial fetch and reset
  useEffect(() => {
    if (!isCacheLoaded) return;

    if (!bountyPostId) {
      fetchBountyPosts();
      setSolutions([]);
      setPage(0);
      setHasMore(true);
      return;
    }

    setLoading(true);
    setSolutions([]);
    setPage(0);
    setHasMore(true);

    const cacheKey = '0';
    if (cache.current[cacheKey]) {
      setSolutions(cache.current[cacheKey]);
      setHasMore(cache.current[cacheKey].length === PAGE_SIZE);
      setPage(0);
      setLoading(false);
    } else {
      fetchSolutions(0, true);
    }
  }, [bountyPostId, isCacheLoaded, fetchSolutions, fetchBountyPosts]);

  // Infinite scrolling
  useEffect(() => {
    if (!bountyPostId || !hasMore || loading || !isCacheLoaded) return;

    const observer = new IntersectionObserver(
      entries => {
        if (entries[0].isIntersecting && hasMore && !loading) {
          fetchSolutions(page + 1, false);
        }
      },
      { rootMargin: '200px' }
    );

    if (observerRef.current) observer.observe(observerRef.current);

    return () => {
      if (observerRef.current) observer.unobserve(observerRef.current);
      observer.disconnect();
    };
  }, [bountyPostId, hasMore, loading, page, fetchSolutions, isCacheLoaded]);

  // Approve solution
  const handleApprove = async (solutionId) => {
    try {
      await approveSolutionPayout(solutionId);
      setSolutions(prev =>
        prev.map(s => (s.id === solutionId ? { ...s, approved: true } : s))
      );
      Object.keys(cache.current).forEach(key => {
        cache.current[key] = cache.current[key].map(s =>
          s.id === solutionId ? { ...s, approved: true } : s
        );
      });
      localStorage.setItem(CACHE_KEY, JSON.stringify(cache.current));
    } catch (err) {
      console.error('Error approving solution:', err);
      setError(err.message || 'Failed to approve solution.');
    }
  };

  const handleSelectBounty = (postId) => {
    navigate(`/dashboard/user-solutions/${postId}`);
  };

  return (
    <div className="p-4 max-w-4xl mx-auto">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">User Solutions</h2>
      {!bountyPostId && (
        <div>
          <p className="text-gray-600 mb-4">Select a bounty post to view submitted solutions:</p>
          {loading && <p>Loading bounty posts...</p>}
          {error && <p className="text-red-500">Error: {error}</p>}
          {bountyPosts.length === 0 && !loading && !error && (
            <p className="text-gray-600">No solutions submitted to your bounty posts yet.</p>
          )}
          {bountyPosts.length > 0 && (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
              {bountyPosts.map(post => (
                <div
                  key={post.id}
                  onClick={() => handleSelectBounty(post.id)}
                  className="bg-white shadow-md rounded-xl p-6 hover:shadow-lg transition cursor-pointer"
                >
                  <h3 className="text-lg font-semibold text-blue-700 mb-2">{post.title}</h3>
                  <p className="text-gray-700 line-clamp-2">{post.description}</p>
                  <p className="text-sm text-gray-600 mt-2">
                    Bounty: <span className="text-green-600 font-semibold">${post.bountyPrice}</span>
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
      {bountyPostId && (
        <>
          {solutions.length === 0 && !loading && !error && (
            <p className="text-gray-600">No solutions submitted to this bounty yet.</p>
          )}
          {error && <p className="text-red-500">Error: {error}</p>}
          {solutions.length > 0 && (
            <div className="space-y-4">
              {solutions.map(solution => (
                <UserSolution
                  key={solution.id}
                  solution={solution}
                  onApprove={handleApprove}
                />
              ))}
            </div>
          )}
          <div ref={observerRef} className="h-20 flex justify-center items-center">
            {loading && <p>Loading solutions...</p>}
            {!hasMore && solutions.length > 0 && <p>No more solutions.</p>}
          </div>
        </>
      )}
    </div>
  );
};

export default UserSolutionPage;

