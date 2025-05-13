import React, { useState } from 'react';
import { Routes, Route, NavLink, useLocation, useParams } from 'react-router-dom';
import { Bars3Icon, XMarkIcon, ChevronDownIcon } from '@heroicons/react/24/solid';
import Bounties from './Bounties';
import BountyForm from '../components/BountyForm';
import BountyDrafts from './BountyDrafts';
import MySolutionPage from './MySolutionPage';
import UserSolutionPage from './UserSolutionPage';
import SolutionForm from '../components/SolutionForm';
import { getBountyPost, getUserProfile, getSolutionByIdAndBountyPostId } from '../api/Api';

const SolutionDetail = () => {
  const { solutionId } = useParams();
  const [solution, setSolution] = useState(null);
  const [bountyTitle, setBountyTitle] = useState('Loading...');
  const [submitter, setSubmitter] = useState('Loading...');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchSolutionDetails = async () => {
      setLoading(true);
      try {
        // Note: API doesn't have a direct getSolutionById endpoint, so we need bountyPostId
        // Assume solution object from parent page includes bountyPostId
        // For simplicity, fetch solutions for a known bountyPostId or modify backend
        const solutions = await getMySolutions(0, 1); // Temporary workaround
        const solution = solutions.find(s => s.id === solutionId);
        if (!solution) throw new Error('Solution not found');
        setSolution(solution);

        const bounty = await getBountyPost(solution.bountyPostId);
        setBountyTitle(bounty.title || 'Unknown Bounty');

        const user = await getUserProfile(solution.submitterId);
        setSubmitter(user.username || 'Unknown User');
      } catch (err) {
        console.error('Error fetching solution details:', err);
        setError(err.message || 'Failed to load solution details.');
      } finally {
        setLoading(false);
      }
    };
    fetchSolutionDetails();
  }, [solutionId]);

  if (loading) return <p>Loading solution details...</p>;
  if (error) return <p className="text-red-500">Error: {error}</p>;
  if (!solution) return <p>Solution not found.</p>;

  return (
    <div className="p-4 max-w-4xl mx-auto">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">Solution Details</h2>
      <div className="bg-white border rounded-lg shadow-sm p-6">
        <h3 className="text-lg font-semibold text-gray-800">Solution for {bountyTitle}</h3>
        <p className="text-sm text-gray-600">Submitted by: {submitter}</p>
        <p className="text-sm text-gray-600 mt-2">
          Submitted: {new Date(solution.createdAt).toLocaleString()}
        </p>
        <p className="text-sm text-gray-600">
          Status: {solution.approved ? 'Approved' : 'Not Approved'}
        </p>
        <p className="text-gray-700 mt-4">{solution.content}</p>
      </div>
    </div>
  );
};

export default function Dashboard() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [openMenu, setOpenMenu] = useState(null);
  const location = useLocation();

  const toggleMenu = (menu) => {
    setOpenMenu(openMenu === menu ? null : menu);
  };

  const getActiveSection = () => {
    if (location.pathname.includes('/dashboard/solutions')) return 'solutions';
    if (location.pathname.includes('/dashboard/account')) return 'account';
    return 'bounties';
  };

  const activeSection = getActiveSection();

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-md sticky top-0 z-50 w-full">
        <div className="flex justify-between items-center h-16 px-4 sm:px-6 lg:px-8 relative">
          <div className="flex items-center space-x-6">
            <span className="text-2xl font-bold text-blue-600">TaskBounty</span>
            <div className="relative">
              <button
                onClick={() => toggleMenu('bounties')}
                className="flex items-center space-x-1 text-sm font-medium text-gray-700 hover:text-blue-600"
              >
                <span>Bounties</span>
                <ChevronDownIcon className="w-4 h-4" />
              </button>
              {openMenu === 'bounties' && (
                <div className="absolute left-0 top-full mt-2 w-48 bg-white border rounded-md shadow-lg z-40">
                  <NavLink to="/dashboard/bounties" className="block px-4 py-2 text-sm hover:bg-gray-100">Bounties</NavLink>
                  <NavLink to="/dashboard/my-bounties" className="block px-4 py-2 text-sm hover:bg-gray-100">My Bounties</NavLink>
                  <NavLink to="/dashboard/create-bounty" className="block px-4 py-2 text-sm hover:bg-gray-100">Create Bounty Post</NavLink>
                  <NavLink to="/dashboard/draft-bounties" className="block px-4 py-2 text-sm hover:bg-gray-100">Draft Bounty Post</NavLink>
                </div>
              )}
            </div>
            <div className="relative">
              <button
                onClick={() => toggleMenu('solutions')}
                className="flex items-center space-x-1 text-sm font-medium text-gray-700 hover:text-blue-600"
              >
                <span>Solutions</span>
                <ChevronDownIcon className="w-4 h-4" />
              </button>
              {openMenu === 'solutions' && (
                <div className="absolute left-0 top-full mt-2 w-48 bg-white border rounded-md shadow-lg z-40">
                  <NavLink to="/dashboard/my-solutions" className="block px-4 py-2 text-sm hover:bg-gray-100">My Solutions</NavLink>
                  <NavLink to="/dashboard/user-solutions" className="block px-4 py-2 text-sm hover:bg-gray-100">User Solutions</NavLink>
                </div>
              )}
            </div>
            <div className="relative">
              <button
                onClick={() => toggleMenu('account')}
                className="flex items-center space-x-1 text-sm font-medium text-gray-700 hover:text-blue-600"
              >
                <span>Account</span>
                <ChevronDownIcon className="w-4 h-4" />
              </button>
              {openMenu === 'account' && (
                <div className="absolute left-0 top-full mt-2 w-48 bg-white border rounded-md shadow-lg z-40">
                  <NavLink to="/dashboard/update-profile" className="block px-4 py-2 text-sm hover:bg-gray-100">Update Profile</NavLink>
                  <NavLink to="/dashboard/add-stripe" className="block px-4 py-2 text-sm hover:bg-gray-100">Add Stripe Account</NavLink>
                </div>
              )}
            </div>
          </div>
          <div className="md:hidden">
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100"
            >
              {isMobileMenuOpen ? (
                <XMarkIcon className="h-6 w-6" aria-hidden="true" />
              ) : (
                <Bars3Icon className="h-6 w-6" aria-hidden="true" />
              )}
            </button>
          </div>
        </div>
        {isMobileMenuOpen && (
          <div className="md:hidden px-4 pb-3 space-y-4">
            <div>
              <p className="font-semibold text-gray-700">Bounties</p>
              <NavLink to="/dashboard/bounties" className="block pl-4 py-1 text-sm hover:text-blue-600">Bounties</NavLink>
              <NavLink to="/dashboard/my-bounties" className="block pl-4 py-1 text-sm hover:text-blue-600">My Bounties</NavLink>
              <NavLink to="/dashboard/create-bounty" className="block pl-4 py-1 text-sm hover:text-blue-600">Create Bounty Post</NavLink>
              <NavLink to="/dashboard/draft-bounties" className="block pl-4 py-1 text-sm hover:text-blue-600">Draft Bounty Post</NavLink>
            </div>
            <div>
              <p className="font-semibold text-gray-700">Solutions</p>
              <NavLink to="/dashboard/my-solutions" className="block pl-4 py-1 text-sm hover:text-blue-600">My Solutions</NavLink>
              <NavLink to="/dashboard/user-solutions" className="block pl-4 py-1 text-sm hover:text-blue-600">User Solutions</NavLink>
            </div>
            <div>
              <p className="font-semibold text-gray-700">Account</p>
              <NavLink to="/dashboard/update-profile" className="block pl-4 py-1 text-sm hover:text-blue-600">Update Profile</NavLink>
              <NavLink to="/dashboard/add-stripe" className="block pl-4 py-1 text-sm hover:text-blue-600">Add Stripe Account</NavLink>
            </div>
          </div>
        )}
      </nav>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-10 pb-6">
        <div className="bg-white rounded-2xl shadow-md p-8">
          <Routes>
            <Route path="/" element={<Bounties />} />
            <Route path="/bounties" element={<Bounties />} />
            <Route path="/create-bounty" element={<BountyForm />} />
            <Route path="/my-bounties" element={<div className="text-2xl font-bold">My Bounties Page</div>} />
            <Route path="/draft-bounties" element={<BountyDrafts />} />
            <Route path="/my-solutions" element={<MySolutionPage />} />
            <Route path="/my-solutions/:bountyPostId" element={<MySolutionPage />} />
            <Route path="/user-solutions" element={<UserSolutionPage />} />
            <Route path="/user-solutions/:bountyPostId" element={<UserSolutionPage />} />
            <Route path="/solutions/:solutionId" element={<SolutionDetail />} />
            <Route path="/solve/:bountyPostId" element={<SolutionForm />} />
            <Route path="/update-profile" element={<div className="text-2xl font-bold">Update Profile Page</div>} />
            <Route path="/add-stripe" element={<div className="text-2xl font-bold">Add Stripe Account Page</div>} />
            <Route path="*" element={<div className="text-2xl font-bold">Page Not Found</div>} />
          </Routes>
        </div>
      </div>
    </div>
  );
}
