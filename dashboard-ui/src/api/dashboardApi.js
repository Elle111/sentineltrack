import axios from 'axios';

const API_BASE_URL = 'http://localhost:8084/api';

export const dashboardApi = {
  getAlerts: async () => {
    const response = await axios.get(`${API_BASE_URL}/alerts`);
    return response.data;
  },
  getRisks: async () => {
    const response = await axios.get(`${API_BASE_URL}/risks`);
    return response.data;
  },
  getSessions: async () => {
    const response = await axios.get(`${API_BASE_URL}/sessions`);
    return response.data;
  },
  getHealth: async () => {
    const response = await axios.get(`${API_BASE_URL}/health`);
    return response.data;
  }
};
