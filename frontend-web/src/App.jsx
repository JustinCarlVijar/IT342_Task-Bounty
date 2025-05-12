import './App.css';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { Login } from './pages/Login';
import Register from './pages/Register';
import VerifyEmail from './pages/VerifyEmail';
import Dashboard from './pages/Dashboard';
import BountyDetail from './pages/BountyDetail'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify-email" element={<VerifyEmail />} />
        <Route path="/dashboard/*" element={<Dashboard />} />
        <Route path="/bounties/:id" element={<BountyDetail />} />
        <Route path="*" element={<div className="text-center mt-8">404: Page Not Found</div>} />
      </Routes>
    </Router>
  );
}

export default App;
