import axios from 'axios';
import { Button, CreateButtonRequest, UpdateButtonRequest, StatsResponse } from '../types/Button';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Include cookies in all requests
  headers: {
    'Content-Type': 'application/json',
  },
});

export const buttonApi = {
  getButtons: async (): Promise<Button[]> => {
    const response = await api.get('/api/buttons');
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

  pressButton: async (id: number): Promise<void> => {
    await api.post(`/api/press/${id}`);
  },

  getStats: async (startTimestamp?: string, endTimestamp?: string): Promise<StatsResponse> => {
    let url = '/api/stats';
    const params = new URLSearchParams();
    if (startTimestamp) {
      params.append('start', startTimestamp);
    }
    if (endTimestamp) {
      params.append('end', endTimestamp);
    }
    if (params.toString()) {
      url += '?' + params.toString();
    }
    const response = await api.get(url);
    return response.data;
  },
};

export default api;