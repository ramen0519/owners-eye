import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { getInsight } from '../../api/insight';
import styles from './InsightPage.module.css';

export default function InsightPage() {
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
      const res = await getInsight(storeId, yearMonth);
      setInsight(res.data.data);
    } catch {
      setError('인사이트 생성에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.pageTitle}>AI 인사이트</h2>
        <p className={styles.pageDesc}>기준 월 포함 최근 3개월 매출 추이를 분석합니다.</p>
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
          {loading ? '분석 중...' : 'AI 인사이트 생성'}
        </button>
      </form>

      {error && <p className={styles.error}>{error}</p>}

      {loading && (
        <div className={styles.loadingBox}>
          <p className={styles.loadingText}>AI가 매출 데이터를 분석하고 있습니다...</p>
        </div>
      )}

      {insight && (
        <div className={styles.insightBox}>
          <p className={styles.insightLabel}>AI 분석 결과</p>
          <div className={styles.insightText}><ReactMarkdown remarkPlugins={[remarkGfm]}>{insight}</ReactMarkdown></div>
        </div>
      )}
    </div>
  );
}
