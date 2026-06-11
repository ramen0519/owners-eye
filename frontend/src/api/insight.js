import api from './axios';

export const getInsight = (storeId, yearMonth) =>
  api.get(`/api/insight?storeId=${storeId}&yearMonth=${yearMonth}`);

export const sendChat = (storeId, question) =>
  api.post('/api/chat', { storeId: Number(storeId), question });
