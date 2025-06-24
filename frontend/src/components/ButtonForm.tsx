import React, { useState } from 'react';
import { Button, CreateButtonRequest, UpdateButtonRequest } from '../types/Button';

interface ButtonFormProps {
  button?: Button;
  onSubmit: (data: CreateButtonRequest | UpdateButtonRequest) => void;
  onCancel: () => void;
  isLoading?: boolean;
}

const ButtonForm: React.FC<ButtonFormProps> = ({
  button,
  onSubmit,
  onCancel,
  isLoading = false,
}) => {
  const [title, setTitle] = useState(button?.title || '');
  const [color, setColor] = useState(button?.color || '#3B82F6');
  const [errors, setErrors] = useState<string[]>([]);

  const userId = 1; // Hardcoded for development

  const validateForm = (): boolean => {
    const newErrors: string[] = [];

    if (!title.trim()) {
      newErrors.push('Title is required');
    } else if (title.length > 100) {
      newErrors.push('Title cannot exceed 100 characters');
    }

    if (!color.match(/^#[0-9A-Fa-f]{6}$/)) {
      newErrors.push('Please select a valid color');
    }

    setErrors(newErrors);
    return newErrors.length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    if (button) {
      // Update existing button
      onSubmit({ title: title.trim(), color });
    } else {
      // Create new button
      onSubmit({ userId, title: title.trim(), color });
    }
  };

  const predefinedColors = [
    '#3B82F6', '#EF4444', '#10B981', '#F59E0B',
    '#8B5CF6', '#EC4899', '#06B6D4', '#84CC16',
    '#F97316', '#6366F1', '#14B8A6', '#F43F5E',
  ];

  return (
    <form onSubmit={handleSubmit}>
      {errors.length > 0 && (
        <div className="alert alert-danger mb-3" role="alert">
          <ul className="list-unstyled mb-0">
            {errors.map((error, index) => (
              <li key={index}>• {error}</li>
            ))}
          </ul>
        </div>
      )}

      <div className="mb-4">
        <label htmlFor="title" className="form-label">
          Button Title
        </label>
        <input
          type="text"
          id="title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="form-control"
          placeholder="Enter button title"
          maxLength={100}
          disabled={isLoading}
        />
        <div className="form-text text-end">
          {title.length}/100
        </div>
      </div>

      <div className="mb-4">
        <label className="form-label">
          Button Color
        </label>
        <div className="row g-2 mb-3">
          {predefinedColors.map((predefinedColor) => (
            <div key={predefinedColor} className="col-2">
              <button
                type="button"
                onClick={() => setColor(predefinedColor)}
                className={`btn p-0 border-2 ${
                  color === predefinedColor ? 'border-dark' : 'border-secondary'
                }`}
                style={{ 
                  backgroundColor: predefinedColor,
                  width: '48px',
                  height: '48px'
                }}
                disabled={isLoading}
              />
            </div>
          ))}
        </div>
        <div className="d-flex align-items-center gap-2">
          <label htmlFor="customColor" className="form-label mb-0">Custom:</label>
          <input
            id="customColor"
            type="color"
            value={color}
            onChange={(e) => setColor(e.target.value)}
            className="form-control form-control-color"
            style={{ width: '64px', height: '40px' }}
            disabled={isLoading}
          />
        </div>
      </div>

      <div className="border-top pt-3">
        <div className="d-flex justify-content-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            className="btn btn-outline-secondary"
            disabled={isLoading}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Saving...
              </>
            ) : (
              button ? 'Update Button' : 'Create Button'
            )}
          </button>
        </div>
      </div>
    </form>
  );
};

export default ButtonForm;