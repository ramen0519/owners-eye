import api from './axios';

export const uploadMenuSale = (storeId, yearMonth, file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post(`/api/menu-sales/upload?storeId=${storeId}&yearMonth=${yearMonth}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const getMenuInsight = (storeId, yearMonth) =>
  api.get(`/api/menu-insight?storeId=${storeId}&yearMonth=${yearMonth}`);
