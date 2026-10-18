import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Login from '../Login';
import { AuthAPI } from '../../api/services';
import { useAuth } from '../../context/AuthContext';

jest.mock('../../api/services', () => ({
  AuthAPI: { login: jest.fn() }
}));

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useLocation: () => ({ state: undefined })
}));

jest.mock('../../context/AuthContext', () => ({
  useAuth: jest.fn()
}));

const mockedUseAuth = useAuth as jest.Mock;
const mockedLogin = AuthAPI.login as jest.Mock;

describe('Login page', () => {
  const loginFn = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockedUseAuth.mockReturnValue({ login: loginFn });
  });

  it('logs in successfully and navigates to the dashboard', async () => {
    mockedLogin.mockResolvedValue({ userId: 'U1', role: 'CUSTOMER', fullName: 'Jane' });

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    await userEvent.type(screen.getByLabelText(/email/i), 'jane@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'secret123');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => expect(loginFn).toHaveBeenCalledWith({ userId: 'U1', role: 'CUSTOMER', fullName: 'Jane' }));
    expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
  });

  it('shows an error message when login fails', async () => {
    mockedLogin.mockRejectedValue({ response: { data: { message: 'Invalid email or password' } } });

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    await userEvent.type(screen.getByLabelText(/email/i), 'jane@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'wrongpass');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText(/invalid email or password/i)).toBeInTheDocument();
    expect(loginFn).not.toHaveBeenCalled();
  });
});
