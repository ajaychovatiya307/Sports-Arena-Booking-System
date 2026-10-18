import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { ReactNode } from 'react';

interface Props { children: ReactNode; requiredRole?: 'ADMIN' | 'CUSTOMER'; }

const ProtectedRoute = ({ children, requiredRole }: Props) => {
  const { session } = useAuth();
  const location = useLocation();
  if (!session) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (requiredRole && session.role !== requiredRole) return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
};

export default ProtectedRoute;
