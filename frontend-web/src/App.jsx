import './App.css'
import { HashRouter as Router , Route, Routes } from 'react-router-dom'
import { Login } from './pages/login'
import Register from './pages/register'
import Banner from './components/banner'


function App() {

  return(
    <Router>
      <Banner />
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Routes>
    </Router>
  )
}

export default App
