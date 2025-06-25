import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import App from './App';

jest.mock('react-router-dom', () => ({
  BrowserRouter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Routes: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Route: ({ element }: { element: React.ReactNode }) => <div>{element}</div>,
  useLocation: () => ({ pathname: '/' }),
  useNavigate: () => jest.fn(),
  useParams: () => ({ buttonId: '1' }),
  Link: ({ children, to }: { children: React.ReactNode; to: string }) => <a href={to}>{children}</a>,
}));

jest.mock('./services/api', () => ({
  buttonApi: {
    getButtons: jest.fn().mockResolvedValue([]),
    getButton: jest.fn(),
    createButton: jest.fn(),
    updateButton: jest.fn(),
    deleteButton: jest.fn(),
    pressButton: jest.fn(),
  }
}));

describe('App', () => {
  test('renders app without crashing', () => {
    render(<App />);
    expect(document.querySelector('.App')).toBeInTheDocument();
  });
});
