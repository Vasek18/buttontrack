import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';

const Header: React.FC = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const location = useLocation();

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  // Close menu when route changes
  useEffect(() => {
    setIsMenuOpen(false);
  }, [location.pathname]);

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-light bg-white shadow-sm sticky-top">
        <div className="container">
          <Link className="navbar-brand" to="/">
            Button Track
          </Link>
          
          <div className="dropdown">
            <button
              className="btn btn-outline-secondary dropdown-toggle"
              type="button"
              onClick={toggleMenu}
              aria-expanded={isMenuOpen}
              style={{ minHeight: '44px', minWidth: '44px' }}
            >
              <svg
                width="24"
                height="24"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                {isMenuOpen ? (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                ) : (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                )}
              </svg>
            </button>

            {isMenuOpen && (
              <>
                <div 
                  className="position-fixed top-0 start-0 w-100 h-100"
                  style={{ 
                    backgroundColor: 'rgba(0, 0, 0, 0.5)', 
                    zIndex: 1040 
                  }}
                  onClick={() => setIsMenuOpen(false)}
                />
                <ul className="dropdown-menu dropdown-menu-end show position-absolute" style={{ zIndex: 1050 }}>
                  <li>
                    <Link
                      className="dropdown-item"
                      to="/"
                    >
                      Main
                    </Link>
                  </li>
                  <li>
                    <Link
                      className="dropdown-item"
                      to="/edit-buttons"
                    >
                      Edit Buttons
                    </Link>
                  </li>
                </ul>
              </>
            )}
          </div>
        </div>
      </nav>
    </>
  );
};

export default Header;