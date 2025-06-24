import React, { useEffect, useState } from 'react';
import { Button } from '../types/Button';
import { buttonApi } from '../services/api';

const ButtonList: React.FC = () => {
  const [buttons, setButtons] = useState<Button[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const userId = 1; // Hardcoded for development

  useEffect(() => {
    const fetchButtons = async () => {
      try {
        setLoading(true);
        const userButtons = await buttonApi.getButtons(userId);
        setButtons(userButtons);
      } catch (err) {
        setError('Failed to load buttons');
        console.error('Error fetching buttons:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchButtons();
  }, [userId]);

  const handleButtonPress = (button: Button) => {
    console.log('Button pressed:', button.title);
    // TODO: Implement button press recording
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '300px' }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading buttons...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '300px' }}>
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      </div>
    );
  }

  if (buttons.length === 0) {
    return (
      <div className="d-flex flex-column align-items-center justify-content-center text-muted" style={{ minHeight: '300px' }}>
        <p className="fs-5 mb-3">No buttons found</p>
        <p className="text-muted">Create your first button in the Edit Buttons section</p>
      </div>
    );
  }

  return (
    <div className="button-grid px-3">
      {buttons.map((button) => (
        <button
          key={button.id}
          onClick={() => handleButtonPress(button)}
          className="button-track-btn w-100 shadow"
          style={{ 
            backgroundColor: button.color,
            borderColor: button.color
          }}
        >
          {button.title}
        </button>
      ))}
    </div>
  );
};

export default ButtonList;