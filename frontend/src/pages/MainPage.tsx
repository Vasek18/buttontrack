import React from 'react';
import Header from '../components/Header';
import ButtonList from '../components/ButtonList';

const MainPage: React.FC = () => {
  return (
    <div className="min-vh-100 d-flex flex-column">
      <Header />
      <main className="flex-grow-1 py-4">
        <div className="container">
          <ButtonList />
        </div>
      </main>
    </div>
  );
};

export default MainPage;