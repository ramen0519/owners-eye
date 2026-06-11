import api from './axios';

export const login = (email, password) =>
  api.post('/api/auth/login', { email, password });

export const signup = (email, password, name) =>
  api.post('/api/auth/signup', { email, password, name });

export const logout = () =>
  api.post('/api/auth/logout');
