import { Link } from 'react-router-dom'

function LoginPage() {
  return (
    <div className="min-h-screen flex flex-col">
      {/* Green bar on top with no borders */}
      <div className="header">
        <h1 className="text-xl font-bold">Task Bounty</h1>
      </div>

      {/* Full-height flexbox container to center the form */}
      <div className="flex items-center justify-center flex-grow bg-gray-100">
        <div className="form-container">
          <h2 className="text-2xl font-bold mb-6 text-center">Login</h2>
          <form>
            <div className="mb-4">
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email</label>
              <input
                type="email"
                id="email"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div className="mb-6">
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">Password</label>
              <input
                type="password"
                id="password"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <button type="submit" className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600">
              Login
            </button>
          </form>
          <p className="mt-4 text-sm text-center">
            Don't have an account?{' '}
            <Link to="/register" className="text-blue-500 hover:underline">
              Register here
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default LoginPage
