import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'https://taskbounty-434679566601.us-west2.run.app',
  withCredentials: true,
});

// Authentication Endpoints
export const loginUser = async (loginData) => {
  try {
    const response = await api.post('/auth/login', {
      identifier: loginData.identifier,
      password: loginData.password,
    });
    return response.data;
  } catch (error) {
    console.error('Error logging in:', error);
    throw error.response?.data?.message || 'Login failed due to a network or CORS error.';
  }
};

// Comment Endpoints
export const createComment = async (postId, commentData) => {
  try {
    const response = await api.post(`/comment/${postId}/bounty_post`, commentData);
    return response.data;
  } catch (error) {
    console.error('Error creating comment:', error);
    throw error;
  }
};

export const getComments = async (postId, page = 0, size = 10) => {
  try {
    const response = await api.get(`/comment/${postId}/bounty_post`, {
      params: { page, size },
    });
    return response.data.content;
  } catch (error) {
    console.error('Error getting comments:', error);
    throw error;
  }
};

export const getComment = async (commentId) => {
  try {
    const response = await api.get(`/comment/${commentId}`);
    return response.data;
  } catch (error) {
    console.error('Error getting comment:', error);
    throw error;
  }
};

export const updateComment = async (commentId, commentData) => {
  try {
    const response = await api.post(`/comment/${commentId}`, commentData);
    return response.data;
  } catch (error) {
    console.error('Error updating comment:', error);
    throw error;
  }
};

export const deleteComment = async (commentId) => {
  try {
    await api.delete(`/comment/${commentId}`);
  } catch (error) {
    console.error('Error deleting comment:', error);
    throw error;
  }
};

// Solution Endpoints
export const submitSolution = async (solutionData) => {
  try {
    const response = await api.post('/solutions/submit', solutionData);
    return response.data;
  } catch (error) {
    console.error('Error submitting solution:', error);
    throw error;
  }
};


export const getSolutions = async (bountyPostId, page = 0, size = 10) => {
  try {
    const response = await api.get(`/solutions/${bountyPostId}`, {
      params: { page, size },
    });
    return response.data.content || response.data;
  } catch (error) {
    console.error('Error getting solutions:', error);
    throw error;
  }
};

export const getMySolutions = async (page = 0, size = 10) => {
  try {
    const response = await api.get('/solutions/my-solutions', {
      params: { page, size },
    });
    return response.data.content || response.data;
  } catch (error) {
    console.error('Error getting my solutions:', error);
    throw error;
  }
};

export const deleteSolution = async (solutionId) => {
  try {
    await api.delete(`/solutions/${solutionId}`);
  } catch (error) {
    console.error('Error deleting solution:', error);
    throw error;
  }
};

export const updateSolution = async (solutionId, solutionData) => {
  try {
    const response = await api.patch(`/solutions/${solutionId}`, solutionData);
    return response.data;
  } catch (error) {
    console.error('Error updating solution:', error);
    throw error;
  }
};

export const approveSolutionPayout = async (solutionId) => {
  try {
    const response = await api.post('/stripe/approve_solution/payout', null, {
      params: { solutionId },
    });
    return response.data;
  } catch (error) {
    console.error('Error approving solution payout:', error);
    throw error;
  }
};

export const approveSolutionTransfer = async (solutionId) => {
  try {
    const response = await api.post('/stripe/approve_solution/transfer', null, {
      params: { solutionId },
    });
    return response.data;
  } catch (error) {
    console.error('Error approving solution transfer:', error);
    throw error;
  }
};

// Bounty Post Endpoints
export const createBountyPost = async (bountyPostData) => {
  try {
    const response = await api.post('/bounty_post', bountyPostData);
    return response.data;
  } catch (error) {
    console.error('Error creating bounty post:', error);
    throw error;
  }
};

export const getPublicBountyPosts = async (page = 0, size = 25, sortBy = 'most_upvoted', search = '') => {
  try {
    const response = await api.get('/bounty_post', {
      params: {
        page,
        size,
        sortBy,
        ...(search?.trim() ? { search } : {}),
      },
    });
    return response.data.content;
  } catch (error) {
    console.error('Error getting public bounty posts:', error);
    throw error;
  }
};

export const getDraftBountyPosts = async (page = 0, size = 25) => {
  try {
    const response = await api.get('/bounty_post/draft', {
      params: { page, size },
    });
    return response.data.content;
  } catch (error) {
    console.error('Error getting draft bounty posts:', error);
    throw error;
  }
};

export const getDraftBountyPost = async (id) => {
  try {
    const response = await api.get(`/bounty_post/${id}/draft`);
    return response.data;
  } catch (error) {
    console.error('Error getting draft bounty post:', error);
    throw error;
  }
};

export const getBountyPost = async (id) => {
  try {
    const response = await api.get(`/bounty_post/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error getting bounty post:', error);
    throw error;
  }
};

export const getMyBountyPosts = async (page = 0, size = 25, sortBy = 'most_upvoted', search = '') => {
  try {
    const response = await api.get('/bounty_post/my_posts', {
      params: {
        page,
        size,
        sortBy,
        ...(search?.trim() ? { search } : {}),
      },
    });
    return response.data.content;
  } catch (error) {
    console.error('Error getting my bounty posts:', error);
    throw error;
  }
};


export const deleteBountyPost = async (id) => {
  try {
    await api.delete(`/bounty_post/${id}`);
  } catch (error) {
    console.error('Error deleting bounty post:', error);
    throw error;
  }
};

export const voteBountyPost = async (id, type) => {
  try {
    const response = await api.post(`/bounty_post/${id}/vote`, null, {
      params: { type },
    });
    return response.data;
  } catch (error) {
    console.error('Error voting on bounty post:', error);
    throw error;
  }
};

// Authentication Endpoints
export const registerUser = async (userData) => {
  try {
    const response = await api.post('/auth/register', userData);
    return response.data;
  } catch (error) {
    console.error('Error registering user:', error);
    throw error;
  }
};

export const verifyEmail = async (code) => {
  try {
    const numericCode = parseInt(code, 10);
    if (isNaN(numericCode)) {
      throw new Error('Verification code must be numeric.');
    }
    const response = await api.post('/auth/verify', null, {
      params: { code: numericCode },
    });
    return response.data;
  } catch (error) {
    console.error('Error verifying email:', error);
    throw error.response?.data?.message || error.message || 'Invalid or expired verification code.';
  }
};

export const resendVerificationCode = async () => {
  try {
    const response = await api.post('/auth/resend_code');
    return response.data;
  } catch (error) {
    console.error('Error resending verification code:', error);
    throw error;
  }
};

export const changeEmail = async (newEmail) => {
  try {
    const response = await api.post('/auth/change_email', { newEmail });
    return response.data;
  } catch (error) {
    console.error('Error changing email:', error);
    throw error;
  }
};

export const updateProfile = async (updateData) => {
  try {
    const response = await api.patch('/auth/update', updateData);
    return response.data;
  } catch (error) {
    console.error('Error updating profile:', error);
    throw error;
  }
};

export const getProfile = async () => {
  try {
    const response = await api.get('/auth/profile');
    return response.data;
  } catch (error) {
    console.error('Error getting profile:', error);
    throw error;
  }
};

export const getUserProfile = async (userId) => {
  try {
    const response = await api.get(`/auth/profile/${userId}`);
    return response.data;
  } catch (error) {
    console.error('Error getting user profile:', error);
    throw error;
  }
};

// Stripe Endpoints
export const createCheckoutSession = async (bountyPostId) => {
  try {
    const response = await api.get(`/stripe/checkout/${bountyPostId}`);
    if (!response.data) {
      throw new Error('Invalid response from checkout API: ' + JSON.stringify(response.data));
    }
    return response.data;
  } catch (error) {
    console.error('Error creating checkout session:', error);
    throw error;
  }
};

export const confirmPayment = async (bountyPostId, sessionId) => {
  try {
    const response = await api.get('/stripe/payment_success/bounty_post', {
      params: { bountyPostId, session_id: sessionId },
    });
    return response.data;
  } catch (error) {
    console.error('Error confirming payment:', error);
    throw error;
  }
};

export const onboardStripe = async () => {
  try {
    const response = await api.get('/stripe/onboarding');
    return response.data;
  } catch (error) {
    console.error('Error onboarding to Stripe:', error);
    throw error;
  }
};

export const createStripeAccount = async (email) => {
  try {
    const response = await api.post('/stripe/create_account', { email });
    return response.data;
  } catch (error) {
    console.error('Error creating Stripe account:', error);
    throw error;
  }
};
