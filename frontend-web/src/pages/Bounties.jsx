import React, { useEffect, useRef, useState, useCallback } from 'react';
import { getPublicBountyPosts } from '../api/Api'; // Assuming this API function is defined elsewhere
import BountyPost from '../components/BountyPost';   // Assuming this component is defined elsewhere

const PAGE_SIZE = 10; // Define a constant for the number of items per page

export default function Bounties() {
  const [posts, setPosts] = useState([]);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('most_upvoted');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true); // Start with loading true
  const [error, setError] = useState(null);
  const [isCacheLoaded, setIsCacheLoaded] = useState(false); // To track if localStorage cache is loaded

  const observerRef = useRef();

  const CACHE_KEY = 'bounty_post_cache';
  const cache = useRef({});

  // Effect 1: Load initial cache from localStorage on component mount
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
    setIsCacheLoaded(true); // Signal that cache loading attempt is complete
  }, []); // Runs once on mount

  // Memoized function to update both in-memory cache and localStorage
  const updateCache = useCallback((key, data) => {
    cache.current[key] = data;
    try {
      localStorage.setItem(CACHE_KEY, JSON.stringify(cache.current));
    } catch (e) {
      console.error('Failed to save cache to localStorage:', e);
    }
  }, []);

  // Memoized function to fetch posts
  const fetchPosts = useCallback(async (pageNumToFetch, isNewSearchOrSortOperation = false) => {
    setLoading(true); // Ensure loading is true at the start of any fetch operation
    setError(null);
    const cacheKey = `${search}_${sortBy}_${pageNumToFetch}`;

    // If it's for loading more pages (not a new search/sort operation) AND data is in cache, use it
    if (!isNewSearchOrSortOperation && cache.current[cacheKey]) {
      const cachedData = cache.current[cacheKey];
      // console.log(`Using cached data for subsequent page: ${cacheKey}`);
      setPosts(prevPosts => [...prevPosts, ...cachedData]); // Always append for subsequent pages
      setHasMore(cachedData.length === PAGE_SIZE);
      setPage(pageNumToFetch);
      setLoading(false);
      return;
    }

    // Fetch from API (either for a new search/sort, or if subsequent page not in cache)
    // console.log(`Workspaceing from API: ${cacheKey}, isNewSearch: ${isNewSearchOrSortOperation}`);
    try {
      const data = await getPublicBountyPosts(pageNumToFetch, PAGE_SIZE, sortBy, search);
      updateCache(cacheKey, data);
      // If it's a new search/sort operation, replace posts. Otherwise, append.
      setPosts(prevPosts => (isNewSearchOrSortOperation ? data : [...prevPosts, ...data]));
      setHasMore(data.length === PAGE_SIZE);
      setPage(pageNumToFetch);
    } catch (err) {
      console.error('Failed to fetch bounty posts:', err);
      setError(err.message || 'An error occurred while fetching posts.');
    } finally {
      setLoading(false);
    }
  }, [search, sortBy, updateCache]);

  // Effect 2: Handles initial data load and changes to search/sortBy
  useEffect(() => {
    if (!isCacheLoaded) {
      // Still waiting for cache to be loaded from localStorage.
      // setLoading(true) is already set initially or by previous operations.
      return;
    }

    setLoading(true); // Indicate start of data processing for this effect
    setPosts([]);     // Clear current posts for a new search/sort or initial load
    setPage(0);       // Reset page to 0
    setHasMore(true); // Assume there's more data

    const initialPageNum = 0;
    const cacheKeyForPage0 = `${search}_${sortBy}_${initialPageNum}`;

    // For initial load or new search/sort, check cache for page 0 first
    if (cache.current[cacheKeyForPage0]) {
      // console.log(`Initial/New Search: Using cached data for page 0: ${cacheKeyForPage0}`);
      const cachedData = cache.current[cacheKeyForPage0];
      setPosts(cachedData); // Set posts from cache
      setHasMore(cachedData.length === PAGE_SIZE);
      setPage(initialPageNum);
      setLoading(false); // Done loading from cache
    } else {
      // console.log(`Initial/New Search: Page 0 not in cache, fetching from API: ${cacheKeyForPage0}`);
      // If page 0 not in cache, fetch it. `isNewSearchOrSortOperation = true` ensures posts are replaced.
      fetchPosts(initialPageNum, true);
    }
  }, [search, sortBy, isCacheLoaded, fetchPosts]); // `WorkspacePosts` is memoized

  // Effect 3: IntersectionObserver for infinite scrolling subsequent pages
  useEffect(() => {
    if (!hasMore || loading || !isCacheLoaded) return; // Don't observe if no more, loading, or cache not ready

    const intersectionObserver = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loading) {
          // console.log(`Observer triggered, fetching page: ${page + 1}`);
          // For subsequent pages, isNewSearchOrSortOperation is false
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
      {/* Search and Sort UI */}
      <div className="flex flex-col sm:flex-row justify-between mb-4 gap-2">
        <input
          type="text"
          className="w-full sm:w-2/3 p-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
          placeholder="Search bounty posts..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          aria-label="Search bounty posts"
        />
        <select
          className="w-full sm:w-1/3 p-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          aria-label="Sort bounty posts by"
        >
          <option value="most_upvoted">Most Upvoted</option>
          <option value="newest">Newest</option>
        </select>
      </div>

      {/* Display Posts Grid */}
      {posts.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {posts.map((post, index) => (
            <BountyPost key={post.id || `post-${index}`} post={post} />
          ))}
        </div>
      )}

      {/* Observer element and status messages */}
      <div ref={observerRef} className="h-20 flex flex-col justify-center items-center text-center">
        {loading && <p>Loading posts...</p>}
        {error && <p className="text-red-500">Error: {error}</p>}
        {!loading && !hasMore && posts.length > 0 && <p>You've reached the end!</p>}
        {!loading && !hasMore && posts.length === 0 && !error && search && <p>No posts found matching your search criteria.</p>}
        {!loading && !hasMore && posts.length === 0 && !error && !search && <p>No bounty posts available at the moment.</p>}
      </div>
    </div>
  );
}
