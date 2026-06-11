import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { logout } from '../api/auth';
import FloatingChat from './FloatingChat';
import styles from './Layout.module.css';

const NAV_ITEMS = [
  { path: '/stores',       label: '가게 관리' },
  { path: '/upload',       label: '엑셀 업로드' },
  { path: '/analysis',     label: '매출 분석' },
  { path: '/insight',      label: 'AI 인사이트' },
  { path: '/menu-sale',    label: '메뉴 판매' },
  { path: '/menu-insight', label: '메뉴 인사이트' },
  { path: '/fixed-cost',   label: '고정비 관리' },
];

export default function Layout() {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    try { await logout(); } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('storeId');
      navigate('/login');
    }
  };

  const showChat = location.pathname !== '/stores';

  return (
    <div className={styles.container}>
      <aside className={styles.sidebar}>
        <div className={styles.logo}>Owners Eye</div>
        <nav className={styles.nav}>
          {NAV_ITEMS.map((item) => (
            <button
              key={item.path}
              className={`${styles.navItem} ${location.pathname === item.path ? styles.active : ''}`}
              onClick={() => navigate(item.path)}
            >
              {item.label}
            </button>
          ))}
        </nav>
        <button className={styles.logoutBtn} onClick={handleLogout}>로그아웃</button>
      </aside>

      <main className={styles.main}>
        <Outlet />
      </main>

      {showChat && <FloatingChat />}
    </div>
  );
}
