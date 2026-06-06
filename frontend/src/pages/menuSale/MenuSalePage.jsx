import { useState } from 'react';
import { uploadMenuSale } from '../../api/menuSale';
import styles from './MenuSalePage.module.css';

export default function MenuSalePage() {
  const storeId = localStorage.getItem('storeId');
  const [yearMonth, setYearMonth] = useState('');
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(false);

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!yearMonth) { setStatus('error:연월을 선택해주세요.'); return; }
    if (!file) { setStatus('error:파일을 선택해주세요.'); return; }
    setLoading(true);
    setStatus('');
    try {
      await uploadMenuSale(storeId, yearMonth, file);
      setStatus('success:업로드 완료! 메뉴 인사이트에서 분석 결과를 확인하세요.');
      setFile(null);
    } catch (err) {
      setStatus('error:' + (err.response?.data?.message || '업로드에 실패했습니다.'));
    } finally {
      setLoading(false);
    }
  };

  const isSuccess = status.startsWith('success:');
  const isError = status.startsWith('error:');
  const statusMsg = status.replace(/^(success|error):/, '');

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.pageTitle}>메뉴 판매 업로드</h2>
        <p className={styles.pageDesc}>배민 주문 내역 엑셀을 업로드하면 AI가 메뉴별 판매량을 자동 집계합니다.</p>
      </div>

      <div className={styles.card}>
        <form onSubmit={handleUpload} className={styles.form}>
          <div className={styles.field}>
            <label className={styles.label}>업로드 연월</label>
            <input
              className={styles.monthInput}
              type="month"
              value={yearMonth}
              onChange={(e) => setYearMonth(e.target.value)}
            />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>배민 주문 내역 파일</label>
            <input
              className={styles.fileInput}
              type="file"
              accept=".xlsx,.xls"
              onChange={(e) => setFile(e.target.files[0])}
            />
            {file && <p className={styles.fileName}>{file.name}</p>}
          </div>

          <button className={styles.uploadBtn} type="submit" disabled={loading}>
            {loading ? 'AI 분석 중... (시간이 걸릴 수 있습니다)' : '업로드 및 분석'}
          </button>
        </form>

        {status && (
          <p className={isSuccess ? styles.success : styles.error}>{statusMsg}</p>
        )}
      </div>

      <div className={styles.infoCard}>
        <p className={styles.infoTitle}>안내</p>
        <ul className={styles.infoList}>
          <li>배민 주문 내역 엑셀 파일을 업로드하면 AI가 메뉴별 판매 횟수를 자동으로 집계합니다.</li>
          <li>같은 연월 데이터는 새로운 데이터로 교체됩니다.</li>
          <li>업로드 후 메뉴 인사이트 페이지에서 분석 결과를 확인할 수 있습니다.</li>
        </ul>
      </div>
    </div>
  );
}
