import api from './axios';

export const getFixedCostList = (storeId) =>
  api.get(`/api/fixed-costs/stores/${storeId}`);

export const getFixedCost = (storeId, yearMonth) =>
  api.get(`/api/fixed-costs?storeId=${storeId}&yearMonth=${yearMonth}`);

export const createFixedCost = (data) =>
  api.post('/api/fixed-costs', data);

export const updateFixedCost = (fixedCostId, data) =>
  api.put(`/api/fixed-costs/${fixedCostId}`, data);

export const deleteFixedCost = (fixedCostId) =>
  api.delete(`/api/fixed-costs/${fixedCostId}`);
