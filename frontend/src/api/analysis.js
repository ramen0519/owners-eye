import api from './axios';

export const getAnalysis = (storeId, yearMonth) =>
  api.get(`/api/analysis?storeId=${storeId}&yearMonth=${yearMonth}`);
