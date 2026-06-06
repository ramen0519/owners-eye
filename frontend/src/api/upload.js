import api from './axios';

const uploadFile = (endpoint, storeId, yearMonth, file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post(`/api/upload/${endpoint}?storeId=${storeId}&yearMonth=${yearMonth}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const uploadPos = (storeId, yearMonth, file) => uploadFile('pos', storeId, yearMonth, file);
export const uploadBaemin = (storeId, yearMonth, file) => uploadFile('baemin', storeId, yearMonth, file);
export const uploadCoupang = (storeId, yearMonth, file) => uploadFile('coupang', storeId, yearMonth, file);
