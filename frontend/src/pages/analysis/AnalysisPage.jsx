import { useState, useEffect } from 'react';
import {
  PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid
} from 'recharts';
import { getAnalysis } from '../../api/analysis';
import styles from './AnalysisPage.module.css';

const COLORS = ['#4f46e5', '#7c3aed', '#06b6d4', '#10b981', '#f59e0b', '#ef4444'];

const formatAmount = (v) => `${v.toLocaleString()}원`;

export default function AnalysisPage() {
  const storeId = localStorage.getItem('storeId');
  const [yearMonth, setYearMonth] = useState(() => sessionStorage.getItem('analysis_yearMonth') || '');
  const [data, setData] = useState(() => {
    const saved = sessionStorage.getItem('analysis_data');
    return saved ? JSON.parse(saved) : null;
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!yearMonth) return;
    setLoading(true);
    setError('');
    setData(null);
    try {
      const res = await getAnalysis(storeId, yearMonth);
      const result = res.data.data;
      setData(result);
      sessionStorage.setItem('analysis_yearMonth', yearMonth);
      sessionStorage.setItem('analysis_data', JSON.stringify(result));
    } catch {
      setError('분석 데이터를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const activeChannels = data?.channels.filter((ch) => ch.revenue > 0) ?? [];

  const pieData = activeChannels.map((ch) => ({
    name: ch.channel,
    value: ch.revenue,
  }));

  const barData = activeChannels.map((ch) => {
    const totalCost = ch.costs.reduce((sum, c) => sum + c.amount, 0);
    return {
      name: ch.channel,
      매출: ch.revenue,
      순이익: ch.revenue - totalCost,
    };
  });

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.pageTitle}>매출 분석</h2>
      </div>

      <form onSubmit={handleSearch} className={styles.searchBar}>
        <input
          className={styles.monthInput}
          type="month"
          value={yearMonth}
          onChange={(e) => setYearMonth(e.target.value)}
          required
        />
        <button className={styles.searchBtn} type="submit" disabled={loading}>
          {loading ? '조회 중...' : '조회'}
        </button>
      </form>

      {error && <p className={styles.error}>{error}</p>}

      {data && (
        <>
          <div className={styles.totalCard}>
            <p className={styles.totalLabel}>총 매출</p>
            <p className={styles.totalAmount}>{data.totalRevenue.toLocaleString()}원</p>
          </div>

          {activeChannels.length > 0 && (
            <div className={styles.chartRow}>
              <div className={styles.chartCard}>
                <p className={styles.chartTitle}>채널별 매출 비중</p>
                <ResponsiveContainer width="100%" height={240}>
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={90}
                      paddingAngle={3}
                      dataKey="value"
                    >
                      {pieData.map((_, i) => (
                        <Cell key={i} fill={COLORS[i % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(v) => formatAmount(v)} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              <div className={styles.chartCard}>
                <p className={styles.chartTitle}>채널별 매출 vs 순이익</p>
                <ResponsiveContainer width="100%" height={240}>
                  <BarChart data={barData} barCategoryGap="30%">
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="name" tick={{ fontSize: 13 }} />
                    <YAxis tickFormatter={(v) => `${(v / 10000).toFixed(0)}만`} tick={{ fontSize: 12 }} />
                    <Tooltip formatter={(v) => formatAmount(v)} />
                    <Legend />
                    <Bar dataKey="매출"  fill="#4f46e5" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="순이익" fill="#10b981" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}

          <div className={styles.channelList}>
            {activeChannels.map((ch) => (
              <div key={ch.channel} className={styles.channelCard}>
                <div className={styles.channelHeader}>
                  <span className={styles.channelName}>{ch.channel}</span>
                  <span className={styles.channelRatio}>{ch.revenueRatio}%</span>
                </div>
                <p className={styles.channelRevenue}>{ch.revenue.toLocaleString()}원</p>
                <div className={styles.progressBar}>
                  <div className={styles.progressFill} style={{ width: `${ch.revenueRatio}%` }} />
                </div>
                {ch.costs && ch.costs.length > 0 && (
                  <div className={styles.costList}>
                    <p className={styles.costTitle}>비용 내역</p>
                    {ch.costs.map((cost) => (
                      <div key={cost.name} className={styles.costItem}>
                        <span className={styles.costName}>{cost.name}</span>
                        <span className={styles.costAmount}>{cost.amount.toLocaleString()}원 ({cost.ratio}%)</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
