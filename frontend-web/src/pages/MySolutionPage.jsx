import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getMySolutions, getBountyPost, updateSolution, deleteSolution } from '../api/Api';
import MySolution from '../components/MySolution';

const PAGE_SIZE = 10;

const MySolutionPage = () => {
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
  const CACHE_KEY = `my_solutions_${bountyPostId || 'list'}`;

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

  // Fetch bounty posts where the user has submitted solutions
  const fetchBountyPosts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const allSolutions = await getMySolutions(0, 100);
      const uniquePostIds = [...new Set(allSolutions.map(s => s.bountyPostId))];
      const postPromises = uniquePostIds.map(id => getBountyPost(id));
      const posts = await Promise.all(postPromises);
      setBountyPosts(posts.filter(post => post && post.id));
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
        const data = await getMySolutions(pageNum, PAGE_SIZE);
        const filteredData = data.filter(s => s.bountyPostId === bountyPostId);
        updateCache(cacheKey, filteredData);
        setSolutions(prev => (isNewFetch ? filteredData : [...prev, ...filteredData]));
        setHasMore(filteredData.length === PAGE_SIZE);
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

  // Handle edit solution
  const handleEdit = async (solutionId) => {
    navigate(`/dashboard/solve/${solutionId}`); // Assuming SolutionForm can handle editing
  };

  // Handle delete solution
  const handleDelete = async (solutionId) => {
    try {
      await deleteSolution(solutionId);
      setSolutions(prev => prev.filter(s => s.id !== solutionId));
      Object.keys(cache.current).forEach(key => {
        cache.current[key] = cache.current[key].filter(s => s.id !== solutionId);
      });
      localStorage.setItem(CACHE_KEY, JSON.stringify(cache.current));
    } catch (err) {
      console.error('Error deleting solution:', err);
      setError(err.message || 'Failed to delete solution.');
    }
  };

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
      const filteredData = cache.current[cacheKey].filter(s => s.bountyPostId === bountyPostId);
      setSolutions(filteredData);
      setHasMore(filteredData.length === PAGE_SIZE);
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

  const handleSelectBounty = (postId) => {
    navigate(`/dashboard/my-solutions/${postId}`);
  };

  return (
    <div className="p-4 max-w-4xl mx-auto">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">My Solutions</h2>
      {!bountyPostId && (
        <div>
          <p className="text-gray-600 mb-4">Select a bounty post to view your submitted solutions:</p>
          {loading && <p>Loading bounty posts...</p>}
          {error && <p className="text-red-500">Error: {error}</p>}
          {bountyPosts.length === 0 && !loading && !error && (
            <p className="text-gray-600">You haven't submitted any solutions yet.</p>
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
            <p className="text-gray-600">No solutions submitted for this bounty.</p>
          )}
          {error && <p className="text-red-500">Error: {error}</p>}
          {solutions.length > 0 && (
            <div className="space-y-4">
              {solutions.map(solution => (
                <MySolution
                  key={solution.id}
                  solution={solution}
                  onEdit={handleEdit}
                  onDelete={handleDelete}
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

export default MySolutionPage;

