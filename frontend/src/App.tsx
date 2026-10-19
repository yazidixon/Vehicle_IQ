import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import { ToastProvider } from './contexts/ToastContext'
import { MainLayout } from './layouts/MainLayout'
import { AuthLayout } from './layouts/AuthLayout'
import { ProtectedRoute } from './components/layout/ProtectedRoute'

import { HomePage } from './pages/HomePage'
import { SearchPage } from './pages/SearchPage'
import { VehicleDetailPage } from './pages/VehicleDetailPage'
import { VinDecoderPage } from './pages/VinDecoderPage'
import { ComparePage } from './pages/ComparePage'
import { DashboardPage } from './pages/DashboardPage'
import { HistoryPage } from './pages/HistoryPage'
import { SavedComparisonsPage } from './pages/SavedComparisonsPage'
import { ProfilePage } from './pages/ProfilePage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { ServerErrorPage } from './pages/ServerErrorPage'

function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <Routes>
            <Route element={<MainLayout />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/search" element={<SearchPage />} />
              <Route path="/vehicles/:id" element={<VehicleDetailPage />} />
              <Route path="/vin-decoder" element={<VinDecoderPage />} />
              <Route path="/compare" element={<ComparePage />} />
              <Route path="/dashboard" element={<ProtectedRoute />}>
                <Route index element={<DashboardPage />} />
              </Route>
              <Route path="/history" element={<ProtectedRoute />}>
                <Route index element={<HistoryPage />} />
              </Route>
              <Route path="/saved-comparisons" element={<ProtectedRoute />}>
                <Route index element={<SavedComparisonsPage />} />
              </Route>
              <Route path="/profile" element={<ProtectedRoute />}>
                <Route index element={<ProfilePage />} />
              </Route>
              <Route path="/500" element={<ServerErrorPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
            <Route element={<AuthLayout />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
            </Route>
          </Routes>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  )
}

export default App
