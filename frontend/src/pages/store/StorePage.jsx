import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getStores, createStore, deleteStore } from '../../api/store';
import styles from './StorePage.module.css';

export default function StorePage() {
  const navigate = useNavigate();
  const [stores, setStores] = useState([]);
  const [storeName, setStoreName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchStores = async () => {
    try {
      const res = await getStores();
      setStores(res.data.data);
    } catch {
      setError('가게 목록을 불러오지 못했습니다.');
    }
  };

  useEffect(() => { fetchStores(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!storeName.trim()) return;
    setLoading(true);
    setError('');
    try {
      await createStore(storeName.trim());
      setStoreName('');
      await fetchStores();
    } catch {
      setError('가게 등록에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (storeId) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      await deleteStore(storeId);
      await fetchStores();
    } catch {
      setError('가게 삭제에 실패했습니다.');
    }
  };

  const handleSelectStore = (storeId) => {
    localStorage.setItem('storeId', storeId);
    navigate('/upload');
  };

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h2 className={styles.pageTitle}>내 가게</h2>
      </div>

      <form onSubmit={handleCreate} className={styles.form}>
        <input
          className={styles.input}
          type="text"
          placeholder="가게 이름 입력"
          value={storeName}
          onChange={(e) => setStoreName(e.target.value)}
        />
        <button className={styles.addBtn} type="submit" disabled={loading}>
          {loading ? '등록 중...' : '+ 가게 추가'}
        </button>
      </form>

      {error && <p className={styles.error}>{error}</p>}

      <div className={styles.storeList}>
        {stores.length === 0 ? (
          <p className={styles.empty}>등록된 가게가 없습니다.</p>
        ) : (
          stores.map((store) => (
            <div key={store.storeId} className={styles.storeCard}>
              <div className={styles.storeInfo} onClick={() => handleSelectStore(store.storeId)}>
                <span className={styles.storeName}>{store.storeName}</span>
                <span className={styles.storeDate}>
                  {new Date(store.createdAt).toLocaleDateString('ko-KR')}
                </span>
              </div>
              <button className={styles.deleteBtn} onClick={() => handleDelete(store.storeId)}>
                삭제
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
