import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import ProtectedRoute from '../ProtectedRoute';
import { useAuth } from '../../context/AuthContext';

jest.mock('../../context/AuthContext', () => ({
  useAuth: jest.fn()
}));

const mockedUseAuth = useAuth as jest.Mock;

const renderWithRoutes = () =>
  render(
    <MemoryRouter initialEntries={['/protected']}>
      <Routes>
        <Route path="/login" element={<div>Login Page</div>} />
        <Route path="/dashboard" element={<div>Dashboard Page</div>} />
        <Route
          path="/protected"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <div>Secret Content</div>
            </ProtectedRoute>
          }
        />
      </Routes>
    </MemoryRouter>
  );

describe('ProtectedRoute', () => {
  it('redirects to /login when there is no session', () => {
    mockedUseAuth.mockReturnValue({ session: null });

    renderWithRoutes();

    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('redirects to /dashboard when the role does not match', () => {
    mockedUseAuth.mockReturnValue({ session: { userId: 'U1', role: 'CUSTOMER', fullName: 'Jane' } });

    renderWithRoutes();

    expect(screen.getByText('Dashboard Page')).toBeInTheDocument();
  });

  it('renders the protected content when session and role match', () => {
    mockedUseAuth.mockReturnValue({ session: { userId: 'A1', role: 'ADMIN', fullName: 'Admin' } });

    renderWithRoutes();

    expect(screen.getByText('Secret Content')).toBeInTheDocument();
  });
});
