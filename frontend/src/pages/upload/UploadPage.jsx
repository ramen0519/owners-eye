import { useState } from 'react';
import { uploadPos, uploadBaemin, uploadCoupang } from '../../api/upload';
import styles from './UploadPage.module.css';

const UPLOAD_TYPES = [
  { key: 'pos',     label: 'POS',  fn: uploadPos },
  { key: 'baemin',  label: '배민', fn: uploadBaemin },
  { key: 'coupang', label: '쿠팡', fn: uploadCoupang },
];

export default function UploadPage() {
  const storeId = localStorage.getItem('storeId');
  const [yearMonth, setYearMonth] = useState('');
  const [files, setFiles] = useState({ pos: null, baemin: null, coupang: null });
  const [status, setStatus] = useState({ pos: '', baemin: '', coupang: '' });
  const [loading, setLoading] = useState({ pos: false, baemin: false, coupang: false });

  const handleFileChange = (key, file) => {
    setFiles((prev) => ({ ...prev, [key]: file }));
    setStatus((prev) => ({ ...prev, [key]: '' }));
  };

  const handleUpload = async (key, fn) => {
    if (!yearMonth) { setStatus((prev) => ({ ...prev, [key]: '연월을 먼저 선택해주세요.' })); return; }
    if (!files[key]) { setStatus((prev) => ({ ...prev, [key]: '파일을 선택해주세요.' })); return; }
    setLoading((prev) => ({ ...prev, [key]: true }));
    setStatus((prev) => ({ ...prev, [key]: '' }));
    try {
      await fn(storeId, yearMonth, files[key]);
      setStatus((prev) => ({ ...prev, [key]: '✓ 업로드 완료' }));
      setFiles((prev) => ({ ...prev, [key]: null }));
    } catch (err) {
      setStatus((prev) => ({ ...prev, [key]: err.response?.data?.message || '업로드 실패' }));
    } finally {
      setLoading((prev) => ({ ...prev, [key]: false }));
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.pageTitle}>엑셀 업로드</h2>
      </div>

      <div className={styles.monthWrap}>
        <label className={styles.label}>업로드 연월</label>
        <input
          className={styles.monthInput}
          type="month"
          value={yearMonth}
          onChange={(e) => setYearMonth(e.target.value)}
        />
      </div>

      <div className={styles.cardList}>
        {UPLOAD_TYPES.map(({ key, label, fn }) => (
          <div key={key} className={styles.card}>
            <h3 className={styles.cardTitle}>{label} 매출 데이터</h3>
            <input
              className={styles.fileInput}
              type="file"
              accept=".xlsx,.xls"
              onChange={(e) => handleFileChange(key, e.target.files[0])}
            />
            {files[key] && <p className={styles.fileName}>{files[key].name}</p>}
            <button
              className={styles.uploadBtn}
              onClick={() => handleUpload(key, fn)}
              disabled={loading[key]}
            >
              {loading[key] ? '업로드 중...' : '업로드'}
            </button>
            {status[key] && (
              <p className={status[key].startsWith('✓') ? styles.success : styles.error}>
                {status[key]}
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
