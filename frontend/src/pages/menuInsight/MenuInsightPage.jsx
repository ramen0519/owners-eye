import { useState } from 'react';
import { getMenuInsight } from '../../api/menuSale';
import styles from './MenuInsightPage.module.css';

export default function MenuInsightPage() {
  const storeId = localStorage.getItem('storeId');
  const [yearMonth, setYearMonth] = useState('');
  const [insight, setInsight] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!yearMonth) return;
    setLoading(true);
    setError('');
    setInsight('');
    try {
      const res = await getMenuInsight(storeId, yearMonth);
      setInsight(res.data.data);
    } catch {
      setError('메뉴 인사이트 생성에 실패했습니다. 먼저 메뉴 판매 데이터를 업로드해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.pageTitle}>메뉴 인사이트</h2>
        <p className={styles.pageDesc}>메뉴별 판매량을 AI가 분석해 인기 메뉴와 개선 포인트를 제안합니다.</p>
      </div>

      <form onSubmit={handleGenerate} className={styles.searchBar}>
        <input
          className={styles.monthInput}
          type="month"
          value={yearMonth}
          onChange={(e) => setYearMonth(e.target.value)}
          required
        />
        <button className={styles.generateBtn} type="submit" disabled={loading}>
          {loading ? '분석 중...' : '메뉴 인사이트 생성'}
        </button>
      </form>

      {error && <p className={styles.error}>{error}</p>}

      {loading && (
        <div className={styles.loadingBox}>
          <p className={styles.loadingText}>AI가 메뉴 판매 데이터를 분석하고 있습니다...</p>
        </div>
      )}

      {insight && (
        <div className={styles.insightBox}>
          <p className={styles.insightLabel}>AI 분석 결과</p>
          <p className={styles.insightText}>{insight}</p>
        </div>
      )}
    </div>
  );
}
