import { createBrowserRouter } from 'react-router-dom';
import Layout from '../components/Layout';
import LoginPage from '../pages/auth/LoginPage';
import SignupPage from '../pages/auth/SignupPage';
import StorePage from '../pages/store/StorePage';
import UploadPage from '../pages/upload/UploadPage';
import AnalysisPage from '../pages/analysis/AnalysisPage';
import InsightPage from '../pages/insight/InsightPage';
import MenuSalePage from '../pages/menuSale/MenuSalePage';
import MenuInsightPage from '../pages/menuInsight/MenuInsightPage';
import FixedCostPage from '../pages/fixedCost/FixedCostPage';

const router = createBrowserRouter([
  { path: '/',       element: <LoginPage /> },
  { path: '/login',  element: <LoginPage /> },
  { path: '/signup', element: <SignupPage /> },
  {
    element: <Layout />,
    children: [
      { path: '/stores',       element: <StorePage /> },
      { path: '/upload',       element: <UploadPage /> },
      { path: '/analysis',     element: <AnalysisPage /> },
      { path: '/insight',      element: <InsightPage /> },
      { path: '/menu-sale',    element: <MenuSalePage /> },
      { path: '/menu-insight', element: <MenuInsightPage /> },
      { path: '/fixed-cost',   element: <FixedCostPage /> },
    ],
  },
]);

export default router;
