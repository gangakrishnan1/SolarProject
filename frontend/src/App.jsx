import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import { ToastProvider } from './context/ToastContext.jsx';
import PageTransition from './components/ui/PageTransition.jsx';
import CalculatorPage from './pages/CalculatorPage.jsx';
import ResultsPage from './pages/ResultsPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import './styles/global.css';

function AnimatedRoutes() {
  const location = useLocation();
  return (
    <PageTransition key={location.pathname}>
      <Routes location={location}>
        <Route path="/" element={<CalculatorPage />} />
        <Route path="/results" element={<ResultsPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
      </Routes>
    </PageTransition>
  );
}

export default function App() {
  return (
    <ToastProvider>
      <BrowserRouter>
        <AnimatedRoutes />
      </BrowserRouter>
    </ToastProvider>
  );
}
