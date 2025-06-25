import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import ButtonForm from '../components/ButtonForm';
import { Button, UpdateButtonRequest } from '../types/Button';
import { buttonApi } from '../services/api';

const EditButtonPage: React.FC = () => {
  const { buttonId } = useParams<{ buttonId: string }>();
  const navigate = useNavigate();
  const [button, setButton] = useState<Button | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [formLoading, setFormLoading] = useState(false);

  const userId = 1; // Hardcoded for development

  useEffect(() => {
    const fetchButton = async () => {
      if (!buttonId) {
        setError('Button ID is required');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const userButtons = await buttonApi.getButtons(userId);
        const foundButton = userButtons?.find(b => b.id === parseInt(buttonId));
        
        if (foundButton) {
          setButton(foundButton);
          setError(null);
        } else {
          setError(`Button with ID ${buttonId} not found`);
        }
      } catch (err) {
        setError('Failed to load button');
        console.error('Error fetching button:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchButton();
  }, [buttonId, userId]);

  const handleUpdateButton = async (data: UpdateButtonRequest) => {
    if (!button) return;

    try {
      setFormLoading(true);
      const updatedButton = await buttonApi.updateButton(button.id, data);
      setButton(updatedButton);
      navigate('/edit-buttons');
    } catch (err) {
      setError('Failed to update button');
      console.error('Error updating button:', err);
    } finally {
      setFormLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/edit-buttons');
  };

  if (loading) {
    return (
      <div className="min-vh-100 d-flex flex-column">
        <Header />
        <main className="flex-grow-1 py-4">
          <div className="container">
            <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '300px' }}>
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading button...</span>
              </div>
            </div>
          </div>
        </main>
      </div>
    );
  }

  if (error || !button) {
    return (
      <div className="min-vh-100 d-flex flex-column">
        <Header />
        <main className="flex-grow-1 py-4">
          <div className="container">
            <div className="row justify-content-center">
              <div className="col-md-6">
                <div className="alert alert-danger" role="alert">
                  {error || 'Button not found'}
                </div>
                <button 
                  className="btn btn-secondary"
                  onClick={() => navigate('/edit-buttons')}
                >
                  ← Back to Edit Buttons
                </button>
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
          <div className="row justify-content-center">
            <div className="col-md-8 col-lg-6">
              <div className="d-flex align-items-center mb-4">
                <button 
                  className="btn btn-outline-secondary me-3"
                  onClick={() => navigate('/edit-buttons')}
                >
                  ← Back
                </button>
                <div>
                  <h2 className="h2 fw-bold mb-0">Edit Button</h2>
                  <p className="text-muted mb-0">Modify your tracking button</p>
                </div>
              </div>

              <div className="card">
                <div className="card-body">
                  <ButtonForm
                    button={button}
                    onSubmit={handleUpdateButton}
                    onCancel={handleCancel}
                    isLoading={formLoading}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default EditButtonPage;