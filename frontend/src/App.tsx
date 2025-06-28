import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import MainPage from './pages/MainPage';
import EditButtonsPage from './pages/EditButtonsPage';
import EditButtonPage from './pages/EditButtonPage';
import StatsPage from './pages/StatsPage';
import LoginPage from './pages/LoginPage';

function App() {
  const clientId = process.env.REACT_APP_GOOGLE_CLIENT_ID;
  
  if (!clientId) {
    return <div>Missing Google Client ID configuration</div>;
  }

  return (
    <GoogleOAuthProvider clientId={clientId}>
      <AuthProvider>
        <Router>
          <div className="App">
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/" element={<ProtectedRoute><MainPage /></ProtectedRoute>} />
              <Route path="/edit-buttons" element={<ProtectedRoute><EditButtonsPage /></ProtectedRoute>} />
              <Route path="/edit-button/:buttonId" element={<ProtectedRoute><EditButtonPage /></ProtectedRoute>} />
              <Route path="/stats" element={<ProtectedRoute><StatsPage /></ProtectedRoute>} />
            </Routes>
          </div>
        </Router>
      </AuthProvider>
    </GoogleOAuthProvider>
  );
}

export default App;
