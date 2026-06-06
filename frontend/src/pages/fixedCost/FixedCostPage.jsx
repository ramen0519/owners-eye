import { useState, useEffect } from 'react';
import { getFixedCostList, createFixedCost, updateFixedCost, deleteFixedCost } from '../../api/fixedCost';
import styles from './FixedCostPage.module.css';

const FIELDS = [
  { key: 'materialCost',     label: '재료비' },
  { key: 'laborCost',        label: '인건비' },
  { key: 'rent',             label: '임대료' },
  { key: 'utilities',        label: '수도·가스·전기' },
  { key: 'storeDeliveryFee', label: '가게배달료' },
  { key: 'consumables',      label: '소모품' },
  { key: 'other',            label: '기타' },
];

const emptyForm = () => Object.fromEntries(FIELDS.map((f) => [f.key, '']));

export default function FixedCostPage() {
  const storeId = localStorage.getItem('storeId');
  const [list, setList] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [yearMonth, setYearMonth] = useState('');
  const [form, setForm] = useState(emptyForm());
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchList = async () => {
    try {
      const res = await getFixedCostList(storeId);
      setList(res.data.data);
    } catch {
      setError('목록을 불러오지 못했습니다.');
    }
  };

  useEffect(() => { fetchList(); }, []);

  const handleFormChange = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    const payload = Object.fromEntries(
      FIELDS.map((f) => [f.key, form[f.key] === '' ? 0 : Number(form[f.key])])
    );
    try {
      if (editingId) {
        await updateFixedCost(editingId, payload);
      } else {
        await createFixedCost({ storeId: Number(storeId), yearMonth, ...payload });
      }
      setShowForm(false);
      setEditingId(null);
      setYearMonth('');
      setForm(emptyForm());
      await fetchList();
    } catch (err) {
      setError(err.response?.data?.message || '저장에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (item) => {
    setEditingId(item.fixedCostId);
    setYearMonth(item.yearMonth);
    setForm(Object.fromEntries(FIELDS.map((f) => [f.key, item[f.key] ?? ''])));
    setShowForm(true);
    setError('');
  };

  const handleDelete = async (fixedCostId) => {
    if (!confirm('삭제하시겠습니까?')) return;
    try {
      await deleteFixedCost(fixedCostId);
      await fetchList();
    } catch {
      setError('삭제에 실패했습니다.');
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingId(null);
    setYearMonth('');
    setForm(emptyForm());
    setError('');
  };

  const total = (item) =>
    FIELDS.reduce((sum, f) => sum + (item[f.key] ?? 0), 0);

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.pageTitle}>고정비 관리</h2>
        {!showForm && (
          <button className={styles.addBtn} onClick={() => setShowForm(true)}>
            + 고정비 추가
          </button>
        )}
      </div>

      {error && <p className={styles.error}>{error}</p>}

      {showForm && (
        <div className={styles.formCard}>
          <h3 className={styles.formTitle}>{editingId ? '고정비 수정' : '고정비 등록'}</h3>
          <form onSubmit={handleSubmit} className={styles.form}>
            {!editingId && (
              <div className={styles.field}>
                <label className={styles.label}>연월</label>
                <input
                  className={styles.input}
                  type="month"
                  value={yearMonth}
                  onChange={(e) => setYearMonth(e.target.value)}
                  required
                />
              </div>
            )}
            <div className={styles.fieldGrid}>
              {FIELDS.map((f) => (
                <div key={f.key} className={styles.field}>
                  <label className={styles.label}>{f.label}</label>
                  <input
                    className={styles.input}
                    type="number"
                    min="0"
                    placeholder="0"
                    value={form[f.key]}
                    onChange={(e) => handleFormChange(f.key, e.target.value)}
                  />
                </div>
              ))}
            </div>
            <div className={styles.formActions}>
              <button className={styles.cancelBtn} type="button" onClick={handleCancel}>취소</button>
              <button className={styles.submitBtn} type="submit" disabled={loading}>
                {loading ? '저장 중...' : editingId ? '수정 완료' : '등록'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className={styles.list}>
        {list.length === 0 ? (
          <p className={styles.empty}>등록된 고정비가 없습니다.</p>
        ) : (
          list.map((item) => (
            <div key={item.fixedCostId} className={styles.card}>
              <div className={styles.cardTop}>
                <span className={styles.cardMonth}>{item.yearMonth}</span>
                <span className={styles.cardTotal}>{total(item).toLocaleString()}원</span>
              </div>
              <div className={styles.costGrid}>
                {FIELDS.map((f) => (
                  <div key={f.key} className={styles.costItem}>
                    <span className={styles.costLabel}>{f.label}</span>
                    <span className={styles.costValue}>{(item[f.key] ?? 0).toLocaleString()}원</span>
                  </div>
                ))}
              </div>
              <div className={styles.cardActions}>
                <button className={styles.editBtn} onClick={() => handleEdit(item)}>수정</button>
                <button className={styles.deleteBtn} onClick={() => handleDelete(item.fixedCostId)}>삭제</button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
