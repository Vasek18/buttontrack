import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import ButtonForm from '../components/ButtonForm';
import { Button, CreateButtonRequest, UpdateButtonRequest } from '../types/Button';
import { buttonApi } from '../services/api';

const EditButtonsPage: React.FC = () => {
  const navigate = useNavigate();
  const [buttons, setButtons] = useState<Button[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [formLoading, setFormLoading] = useState(false);

  const userId = 1; // Hardcoded for development

  const fetchButtons = async () => {
    try {
      setLoading(true);
      const userButtons = await buttonApi.getButtons(userId);
      setButtons(userButtons);
      setError(null);
    } catch (err) {
      setError('Failed to load buttons');
      console.error('Error fetching buttons:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchButtons();
  }, [userId]);

  const handleFormSubmit = async (data: CreateButtonRequest | UpdateButtonRequest) => {
    // Since this is EditButtonsPage (list view), we only handle creation
    const createData = data as CreateButtonRequest;
    
    try {
      setFormLoading(true);
      const newButton = await buttonApi.createButton(createData);
      setButtons([...buttons, newButton]);
      setShowForm(false);
    } catch (err) {
      setError('Failed to create button');
      console.error('Error creating button:', err);
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteButton = async (buttonId: number) => {
    if (!window.confirm('Are you sure you want to delete this button?')) {
      return;
    }

    try {
      await buttonApi.deleteButton(buttonId);
      setButtons(buttons.filter(b => b.id !== buttonId));
    } catch (err) {
      setError('Failed to delete button');
      console.error('Error deleting button:', err);
    }
  };

  const handleEditButton = (button: Button) => {
    navigate(`/edit-button/${button.id}`);
  };

  const handleCancelForm = () => {
    setShowForm(false);
  };

  if (loading) {
    return (
      <div className="min-vh-100 d-flex flex-column">
        <Header />
        <main className="flex-grow-1 py-4">
          <div className="container">
            <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '300px' }}>
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading buttons...</span>
              </div>
            </div>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="min-vh-100 d-flex flex-column">
      <Header />
      <main className="flex-grow-1 py-4">
        <div className="container">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h2 className="h2 fw-bold">Edit Buttons</h2>
              <p className="text-muted mb-0">Manage your tracking buttons</p>
            </div>
            {!showForm && (
              <button
                onClick={() => setShowForm(true)}
                className="btn btn-primary"
              >
                Add New Button
              </button>
            )}
          </div>

          {error && (
            <div className="alert alert-danger mb-4" role="alert">
              {error}
            </div>
          )}

          {showForm && (
            <div className="card mb-4">
              <div className="card-body">
                <h3 className="card-title h5 mb-4">
                  Create New Button
                </h3>
                <ButtonForm
                  onSubmit={handleFormSubmit}
                  onCancel={handleCancelForm}
                  isLoading={formLoading}
                />
              </div>
            </div>
          )}

          <div className="card">
            {!buttons || buttons.length === 0 ? (
              <div className="card-body text-center py-5">
                <p className="fs-5 mb-3 text-muted">No buttons created yet</p>
                <p className="text-muted mb-0">Click "Add New Button" to get started</p>
              </div>
            ) : (
              <div className="list-group list-group-flush">
                {buttons.map((button) => (
                  <div key={button.id} className="list-group-item d-flex align-items-center justify-content-between py-3">
                    <div className="d-flex align-items-center">
                      <div
                        className="rounded me-3"
                        style={{ 
                          backgroundColor: button.color,
                          width: '32px',
                          height: '32px'
                        }}
                      />
                      <div>
                        <h4 className="h6 mb-1">{button.title}</h4>
                        <p className="text-muted small mb-0">
                          Created: {new Date(button.createdAt).toLocaleDateString()}
                        </p>
                      </div>
                    </div>
                    <div className="btn-group" role="group">
                      <button
                        onClick={() => handleEditButton(button)}
                        className="btn btn-outline-primary btn-sm"
                        disabled={showForm}
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDeleteButton(button.id)}
                        className="btn btn-outline-danger btn-sm"
                        disabled={showForm}
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default EditButtonsPage;