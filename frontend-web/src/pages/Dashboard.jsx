import React, { useState } from 'react';
import { Routes, Route, NavLink, useLocation } from 'react-router-dom';
import { Bars3Icon, XMarkIcon, ChevronDownIcon } from '@heroicons/react/24/solid';
import Bounties from './Bounties';


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
      {/* Top Navigation */}
      <nav className="bg-white shadow-md sticky top-0 z-50 w-full">
        <div className="flex justify-between items-center h-16 px-4 sm:px-6 lg:px-8 relative">
          {/* Left: Logo and Nav Menus */}
          <div className="flex items-center space-x-6">
            <span className="text-2xl font-bold text-blue-600">TaskBounty</span>

            {/* Bounties */}
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

            {/* Solutions */}
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

            {/* Account */}
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

          {/* Mobile Button */}
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

        {/* Mobile Dropdowns */}
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

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-10 pb-6">
        <div className="bg-white rounded-2xl shadow-md p-8">
          <Routes>
            {/* Bounties */}
            <Route path="/" element={<Bounties />} />
            <Route path="/bounties" element={<Bounties />} />

            <Route path="/my-bounties" element={<div className="text-2xl font-bold">My Bounties Page</div>} />
            <Route path="/create-bounty" element={<div className="text-2xl font-bold">Create Bounty Post Page</div>} />
            <Route path="/draft-bounties" element={<div className="text-2xl font-bold">Draft Bounty Post Page</div>} />

            {/* Solutions */}
            <Route path="/my-solutions" element={<div className="text-2xl font-bold">My Solutions Page</div>} />
            <Route path="/user-solutions" element={<div className="text-2xl font-bold">User Solutions Page</div>} />

            {/* Account */}
            <Route path="/update-profile" element={<div className="text-2xl font-bold">Update Profile Page</div>} />
            <Route path="/add-stripe" element={<div className="text-2xl font-bold">Add Stripe Account Page</div>} />

            {/* Fallback */}
            <Route path="*" element={<div className="text-2xl font-bold">Page Not Found</div>} />
          </Routes>
        </div>
      </div>
    </div>
  );
}

