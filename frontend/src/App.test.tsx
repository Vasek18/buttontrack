import React from 'react';
import { render, waitFor, act } from '@testing-library/react';
import App from './App';

jest.mock('react-router-dom', () => ({
  BrowserRouter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Routes: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  Route: ({ element }: { element: React.ReactNode }) => <div>{element}</div>,
  Link: ({ children, to }: { children: React.ReactNode; to: string }) => <a href={to}>{children}</a>,
  useLocation: () => ({ pathname: '/' }),
  useNavigate: () => jest.fn(),
  useParams: () => ({ buttonId: '1' }),
}));

jest.mock('./services/api', () => ({
  buttonApi: {
    getButtons: jest.fn().mockResolvedValue([]),
    getButton: jest.fn().mockResolvedValue({
      id: 1,
      userId: 1,
      title: 'Test Button',
      color: '#007bff',
      createdAt: '2023-01-01T00:00:00Z',
      updatedAt: '2023-01-01T00:00:00Z'
    }),
    createButton: jest.fn(),
    updateButton: jest.fn(),
    deleteButton: jest.fn(),
    pressButton: jest.fn().mockResolvedValue(undefined),
  }
}));

describe('App', () => {
  test('renders app without crashing', async () => {
    let component: any;
    
    await act(async () => {
      component = render(<App />);
    });
    
    // Wait for the loading spinner to disappear and async operations to complete
    await waitFor(() => {
      expect(component.container.querySelector('.spinner-border')).not.toBeInTheDocument();
    }, { timeout: 3000 });
    
    expect(component.container).toBeInTheDocument();
  });
});
