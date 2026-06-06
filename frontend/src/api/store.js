import api from './axios';

export const getStores = () => api.get('/api/stores');
export const createStore = (storeName) => api.post('/api/stores', { storeName });
export const deleteStore = (storeId) => api.delete(`/api/stores/${storeId}`);
