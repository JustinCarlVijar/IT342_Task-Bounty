import React, { useEffect, useRef, useState, useCallback } from 'react';
import { getDraftBountyPosts } from '../api/Api'; // Adjust path based on your project structure
import BountyPostDraft from '../components/BountyPostDraft'; // Adjust path based on your project structure

const PAGE_SIZE = 10;

export default function BountyDrafts() {
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isCacheLoaded, setIsCacheLoaded] = useState(false);
  const observerRef = useRef();

  const CACHE_KEY = 'bounty_draft_cache';
  const cache = useRef({});

  // Load cache from localStorage
  useEffect(() => {
    const savedCache = localStorage.getItem(CACHE_KEY);
    if (savedCache) {
      try {
        cache.current = JSON.parse(savedCache);
        console.log('Loaded cache:', cache.current); // Debug
      } catch (e) {
        console.error('Failed to parse cache from localStorage:', e);
        localStorage.removeItem(CACHE_KEY);
      }
    }
    setIsCacheLoaded(true);
  }, []);

  // Update cache
  const updateCache = useCallback((key, data) => {
    cache.current[key] = data;
    try {
      localStorage.setItem(CACHE_KEY, JSON.stringify(cache.current));
    } catch (e) {
      console.error('Failed to save cache to localStorage:', e);
    }
  }, []);

  // Fetch draft posts
  const fetchPosts = useCallback(
    async (pageNumToFetch, isInitialLoad = false) => {
      setLoading(true);
      setError(null);
      const cacheKey = `page_${pageNumToFetch}`;

      if (!isInitialLoad && cache.current[cacheKey]) {
        const cachedData = cache.current[cacheKey];
        console.log('Using cached data for page', pageNumToFetch, ':', cachedData); // Debug
        setPosts((prevPosts) => [...prevPosts, ...cachedData]);
        setHasMore(cachedData.length === PAGE_SIZE);
        setPage(pageNumToFetch);
        setLoading(false);
        return;
      }

      try {
        const data = await getDraftBountyPosts(pageNumToFetch, PAGE_SIZE);
        console.log('Fetched data for page', pageNumToFetch, ':', data); // Debug
        updateCache(cacheKey, data);
        setPosts((prevPosts) => (isInitialLoad ? data : [...prevPosts, ...data]));
        setHasMore(data.length === PAGE_SIZE);
        setPage(pageNumToFetch);
      } catch (err) {
        console.error('Failed to fetch draft bounty posts:', err);
        setError(err.message || 'An error occurred while fetching drafts.');
      } finally {
        setLoading(false);
      }
    },
    [updateCache]
  );

  // Handle initial load
  useEffect(() => {
    if (!isCacheLoaded) return;

    setLoading(true);
    setPosts([]);
    setPage(0);
    setHasMore(true);

    const cacheKeyForPage0 = `page_0`;
    if (cache.current[cacheKeyForPage0]) {
      const cachedData = cache.current[cacheKeyForPage0];
      console.log('Initial load from cache:', cachedData); // Debug
      setPosts(cachedData);
      setHasMore(cachedData.length === PAGE_SIZE);
      setPage(0);
      setLoading(false);
    } else {
      fetchPosts(0, true);
    }
  }, [isCacheLoaded, fetchPosts]);

  // Infinite scrolling
  useEffect(() => {
    if (!hasMore || loading || !isCacheLoaded) return;

    const intersectionObserver = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loading) {
          fetchPosts(page + 1, false);
        }
      },
      { rootMargin: '200px' }
    );

    const currentObserverElement = observerRef.current;
    if (currentObserverElement) {
      intersectionObserver.observe(currentObserverElement);
    }

    return () => {
      if (currentObserverElement) {
        intersectionObserver.unobserve(currentObserverElement);
      }
      intersectionObserver.disconnect();
    };
  }, [hasMore, loading, page, fetchPosts, isCacheLoaded]);

  return (
    <div className="p-4 max-w-4xl mx-auto">
      <h2 className="text-2xl font-bold mb-4">Draft Bounty Posts</h2>
      {loading && <p>Loading drafts...</p>}
      {error && <p className="text-red-500">Error: {error}</p>}
      {posts.length === 0 && !loading && !error && (
        <p>No draft bounty posts available at the moment.</p>
      )}
      {posts.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {posts.map((post, index) => (
            <BountyPostDraft
              key={post.id || `draft-${index}`}
              post={post}
            />
          ))}
        </div>
      )}
      <div ref={observerRef} className="h-20 flex flex-col justify-center items-center text-center">
        {!loading && hasMore && <p>Loading more drafts...</p>}
        {!loading && !hasMore && posts.length > 0 && <p>You've reached the end!</p>}
      </div>
    </div>
  );
}
