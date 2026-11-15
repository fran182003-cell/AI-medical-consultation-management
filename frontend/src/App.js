import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import NuevaCita from './components/NuevaCita2';
import AdminPanel from './components/Adminpanel'; 
import { useAuth } from './api/Comprobar_rol';

function App() {
  const { isAdmin, isLogged } = useAuth();

  return (
    <Router>
      <Routes>
        {/* 1. RUTA PÚBLICA: Login y redirección inicial inteligente */}
        <Route 
          path="/" 
          element={
            isLogged 
              ? (isAdmin ? <Navigate to="/admin/dashboard" /> : <Navigate to="/dashboard" />) 
              : <Login />
          } 
        />

        {/* =========================================
            2. RUTAS ESTÁNDAR (MÉDICOS)
            (Si un admin entra aquí, lo mandamos a su versión /admin)
        ========================================= */}
        <Route 
          path="/dashboard" 
          element={
            isLogged 
              ? (isAdmin ? <Navigate to="/admin/dashboard" /> : <Dashboard />) 
              : <Navigate to="/" />
          } 
        />
        
        <Route 
          path="/nueva-cita" 
          element={
            isLogged 
              ? (isAdmin ? <Navigate to="/admin/nueva-cita" /> : <NuevaCita />) 
              : <Navigate to="/" />
          } 
        />

        {/* =========================================
            3. RUTAS EXCLUSIVAS ADMIN
        ========================================= */}
        {/* Panel principal de Admin */}
        <Route 
          path="/admin" 
          element={isLogged && isAdmin ? <AdminPanel /> : <Navigate to="/dashboard" />} 
        />
        
        {/* Vistas compartidas pero con URL de Admin */}
        <Route 
          path="/admin/dashboard" 
          element={isLogged && isAdmin ? <Dashboard /> : <Navigate to="/dashboard" />} 
        />
        
        <Route 
          path="/admin/nueva-cita" 
          element={isLogged && isAdmin ? <NuevaCita /> : <Navigate to="/dashboard" />} 
        />

        {/* =========================================
            4. REDIRECCIÓN POR DEFECTO
        ========================================= */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;
