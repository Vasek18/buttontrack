import React, { useEffect } from 'react';
import { GoogleLogin } from '@react-oauth/google';
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

  const handleGoogleSuccess = async (credentialResponse: any) => {
    try {
      if (credentialResponse.credential) {
        // Verify the token with your backend
        const userInfo = await authApi.verifyToken(credentialResponse.credential);
        login(credentialResponse.credential, userInfo);
        navigate('/');
      }
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  const handleGoogleError = () => {
    console.error('Google Login Failed');
  };

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
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={handleGoogleError}
            width="384"
            theme="outline"
            size="large"
            text="signin_with"
          />
        </div>
      </div>
    </div>
  );
};

export default LoginPage;