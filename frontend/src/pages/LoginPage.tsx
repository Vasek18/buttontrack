import React, { useEffect } from 'react';
import { useGoogleLogin } from '@react-oauth/google';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { authApi } from '../services/api';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  const googleLogin = useGoogleLogin({
    onSuccess: async (response) => {
      try {
        // Get the ID token from the authorization code
        const tokenResponse = await fetch('https://oauth2.googleapis.com/token', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: new URLSearchParams({
            client_id: process.env.REACT_APP_GOOGLE_CLIENT_ID!,
            client_secret: '', // This should be handled on the backend
            code: response.code,
            grant_type: 'authorization_code',
            redirect_uri: window.location.origin,
          }),
        });

        const tokens = await tokenResponse.json();
        
        if (tokens.id_token) {
          // Verify the token with your backend
          const userInfo = await authApi.verifyToken(tokens.id_token);
          login(tokens.id_token, userInfo);
          navigate('/');
        }
      } catch (error) {
        console.error('Login failed:', error);
      }
    },
    flow: 'auth-code',
  });

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Sign in to Button Track
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Track your daily habits with custom buttons
          </p>
        </div>
        <div className="mt-8 space-y-6">
          <button
            onClick={() => googleLogin()}
            className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            Sign in with Google
          </button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;