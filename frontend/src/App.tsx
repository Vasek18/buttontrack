import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import EditButtonsPage from './pages/EditButtonsPage';
import EditButtonPage from './pages/EditButtonPage';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<MainPage />} />
          <Route path="/edit-buttons" element={<EditButtonsPage />} />
          <Route path="/edit-button/:buttonId" element={<EditButtonPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
