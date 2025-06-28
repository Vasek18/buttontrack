import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';

interface UserInfo {
  id: number;
  email: string;
  name: string;
}

interface AuthContextType {
  user: UserInfo | null;
  login: (userInfo: UserInfo) => void;
  logout: () => void;
  isAuthenticated: boolean;
  checkAuth: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);
  
  const checkAuth = async () => {
    try {
      // Try to get user info using the session cookie
      const response = await fetch(`${process.env.REACT_APP_API_URL}/api/buttons`, {
        method: 'GET',
        credentials: 'include', // Include cookies
      });
      
      if (response.status === 401) {
        // Not authenticated
        setUser(null);
      } else if (response.ok) {
        // We're authenticated, but we need user info
        // For now, we'll set a minimal user object
        // In a real app, you might have a /api/me endpoint
        setUser({ id: 0, email: '', name: '' });
      }
    } catch (error) {
      console.error('Auth check failed:', error);
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  const login = (userInfo: UserInfo) => {
    setUser(userInfo);
  };

  const logout = async () => {
    try {
      await fetch(`${process.env.REACT_APP_API_URL}/api/logout`, {
        method: 'POST',
        credentials: 'include',
      });
    } catch (error) {
      console.error('Logout failed:', error);
    }
    setUser(null);
  };

  const value = {
    user,
    login,
    logout,
    checkAuth,
    isAuthenticated: !!user,
  };
  
  if (isLoading) {
    return <div>Loading...</div>;
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};