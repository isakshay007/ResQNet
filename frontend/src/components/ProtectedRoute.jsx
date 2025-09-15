// src/components/ProtectedRoute.jsx
import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    // Not logged in → redirect to login
    return <Navigate to="/login" replace />;
  }

  if (roles && !roles.includes(user?.role)) {
    // Authenticated but doesn't have required role
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

export default ProtectedRoute;
