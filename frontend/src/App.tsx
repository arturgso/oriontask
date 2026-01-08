import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { useEffect } from 'react';
import { useStore } from './state/store';
import { authService } from './services/authService';
import { LoginPage } from './pages/LoginPage';
import { DharmasPage } from './pages/DharmasPage';
import { TasksPage } from './pages/TasksPage';
import { AgoraPage } from './pages/AgoraPage';
import ProfilePage from './pages/ProfilePage';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = authService.isAuthenticated();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

function App() {
  const loadUserFromStorage = useStore((state) => state.loadUserFromStorage);
  const loadTheme = useStore((state) => state.loadTheme);
  const theme = useStore((state) => state.theme);
  const hydrated = useStore((state) => state.hydrated);
  const setUser = useStore((state) => state.setUser);

  useEffect(() => {
    loadTheme();
    
    // Carregar usuário do authService
    const user = authService.getUser();
    if (user) {
      setUser({
        id: user.id,
        username: user.username,
        name: user.name,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      });
    }
    
    loadUserFromStorage();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const html = document.documentElement;
    html.setAttribute('data-theme', theme);
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
        <Route path="/dharmas" element={<ProtectedRoute><DharmasPage /></ProtectedRoute>} />
        <Route path="/tasks/:dharmaId" element={<ProtectedRoute><TasksPage /></ProtectedRoute>} />
        <Route path="/agora" element={<ProtectedRoute><AgoraPage /></ProtectedRoute>} />
        <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
