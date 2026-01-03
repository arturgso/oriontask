import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { useEffect } from 'react';
import { useStore } from './state/store';
import { LoginPage } from './pages/LoginPage';
import { DharmasPage } from './pages/DharmasPage';
import { TasksPage } from './pages/TasksPage';
import { AgoraPage } from './pages/AgoraPage';

function App() {
  const loadUserFromStorage = useStore((state) => state.loadUserFromStorage);
  const loadTheme = useStore((state) => state.loadTheme);
  const theme = useStore((state) => state.theme);
  const hydrated = useStore((state) => state.hydrated);

  useEffect(() => {
    loadTheme();
    loadUserFromStorage();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  if (!hydrated) {
    return null;
  }

  return (
    <Router>
      <Toaster
        position="top-center"
        toastOptions={{
          duration: 3000,
          style: {
            background: '#363636',
            color: '#fff',
          },
          success: {
            duration: 3000,
            iconTheme: {
              primary: '#4CAF50',
              secondary: '#fff',
            },
          },
          error: {
            duration: 4000,
            iconTheme: {
              primary: '#EF4444',
              secondary: '#fff',
            },
          },
        }}
      />
      <Routes>
        <Route path="/" element={<Navigate to="/agora" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/dharmas" element={<DharmasPage />} />
        <Route path="/tasks/:dharmaId" element={<TasksPage />} />
        <Route path="/agora" element={<AgoraPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
