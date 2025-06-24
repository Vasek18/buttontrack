import axios from 'axios';
import { Button, CreateButtonRequest, UpdateButtonRequest } from '../types/Button';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const buttonApi = {
  getButtons: async (userId: number): Promise<Button[]> => {
    const response = await api.get(`/api/buttons?userId=${userId}`);
    return response.data;
  },

  getButton: async (id: number): Promise<Button> => {
    const response = await api.get(`/api/buttons/${id}`);
    return response.data;
  },

  createButton: async (button: CreateButtonRequest): Promise<Button> => {
    const response = await api.post('/api/buttons', button);
    return response.data;
  },

  updateButton: async (id: number, button: UpdateButtonRequest): Promise<Button> => {
    const response = await api.put(`/api/buttons/${id}`, button);
    return response.data;
  },

  deleteButton: async (id: number): Promise<void> => {
    await api.delete(`/api/buttons/${id}`);
  },
};