import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Footer from './components/Footer';

import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Profile from './pages/Profile';
import Teams from './pages/Teams';
import ArenaBrowse from './pages/ArenaBrowse';
import ArenaPackages from './pages/ArenaPackages';
import Categories from './pages/Categories';
import Customers from './pages/Customers';
import Slots from './pages/Slots';
import Bookings from './pages/Bookings';
import BookingForm from './pages/BookingForm';
import Reports from './pages/Reports';

const Shell = ({ children }: { children: React.ReactNode }) => (
  <div className="app-shell">
    <Sidebar />
    <div className="app-main">
      <Header />
      <main className="app-content">{children}</main>
      <Footer />
    </div>
  </div>
);

const App = () => {
  const { session, isAdmin } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={session ? <Navigate to="/dashboard" replace /> : <Login />} />
      <Route path="/register" element={session ? <Navigate to="/dashboard" replace /> : <Register />} />

      <Route path="/dashboard" element={<ProtectedRoute><Shell><Dashboard /></Shell></ProtectedRoute>} />
      <Route path="/profile" element={<ProtectedRoute><Shell><Profile /></Shell></ProtectedRoute>} />
      <Route path="/teams" element={<ProtectedRoute><Shell><Teams /></Shell></ProtectedRoute>} />
      <Route path="/arenas" element={<ProtectedRoute><Shell>{isAdmin ? <ArenaPackages /> : <ArenaBrowse />}
      </Shell></ProtectedRoute>} />
      <Route path="/bookings" element={<ProtectedRoute><Shell><Bookings /></Shell></ProtectedRoute>} />
      <Route path="/bookings/new" element={<ProtectedRoute><Shell><BookingForm /></Shell></ProtectedRoute>} />

      <Route path="/categories" element={<ProtectedRoute requiredRole="ADMIN"><Shell><Categories /></Shell></ProtectedRoute>} />
      <Route path="/customers"  element={<ProtectedRoute requiredRole="ADMIN"><Shell><Customers /></Shell></ProtectedRoute>} />
      <Route path="/slots"      element={<ProtectedRoute requiredRole="ADMIN"><Shell><Slots /></Shell></ProtectedRoute>} />
      <Route path="/reports"    element={<ProtectedRoute requiredRole="ADMIN"><Shell><Reports /></Shell></ProtectedRoute>} />

      <Route path="/" element={<Navigate to={session ? '/dashboard' : '/login'} replace />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;
