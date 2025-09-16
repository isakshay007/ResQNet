import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { Toaster } from "react-hot-toast"; // global toaster

import Welcome from "./pages/Welcome.jsx";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard.jsx";

// Reporter
import MyDisasters from "./pages/MyDisasters.jsx";
import MyRequests from "./pages/Reporter/MyRequests.jsx";
import Contributions from "./pages/Reporter/Contributions.jsx";

// Responder
import AllRequests from "./pages/Responder/AllRequests.jsx";
import MyContributions from "./pages/Responder/MyContributions.jsx";

// Shared
import Notifications from "./pages/Notifications.jsx";
import Requests from "./pages/Requests.jsx"; // ✅ global requests

// Admin
import AdminDashboard from "./pages/Admin/AdminDashboard.jsx";
import AdminMapDashboard from "./pages/Admin/AdminMapDashboard.jsx";
import ManageUsers from "./pages/Admin/ManageUsers.jsx";
import ManageDisasters from "./pages/Admin/ManageDisasters.jsx";
import ManageRequests from "./pages/Admin/ManageRequests.jsx";
import ManageContributions from "./pages/Admin/ManageContributions.jsx";
import ManageNotifications from "./pages/Admin/ManageNotifications.jsx";
import AdminSummary from "./pages/Admin/AdminSummary.jsx";

import ProtectedRoute from "./components/ProtectedRoute";
import { AuthProvider } from "./context/AuthContext";

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Pages */}
          <Route path="/" element={<Welcome />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Shared */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <Notifications />
              </ProtectedRoute>
            }
          />

          {/* 🌍 Global Requests (all roles: Reporter, Responder, Admin) */}
          <Route
            path="/requests"
            element={
              <ProtectedRoute roles={["REPORTER", "RESPONDER", "ADMIN"]}>
                <Requests />
              </ProtectedRoute>
            }
          />

          {/* Reporter Routes */}
          <Route
            path="/my-disasters"
            element={
              <ProtectedRoute roles={["REPORTER", "RESPONDER"]}>
                <MyDisasters />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-requests"
            element={
              <ProtectedRoute roles={["REPORTER"]}>
                <MyRequests />
              </ProtectedRoute>
            }
          />
          <Route
            path="/contributions"
            element={
              <ProtectedRoute roles={["REPORTER"]}>
                <Contributions />
              </ProtectedRoute>
            }
          />

          {/* Responder Routes */}
          <Route
            path="/all-requests"
            element={
              <ProtectedRoute roles={["RESPONDER"]}>
                <AllRequests />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-contributions"
            element={
              <ProtectedRoute roles={["RESPONDER"]}>
                <MyContributions />
              </ProtectedRoute>
            }
          />

          {/* Admin Routes */}
          <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/map"
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <AdminMapDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/users"
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <ManageUsers />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/disasters"
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <ManageDisasters />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/requests"
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <ManageRequests />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/contributions"
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <ManageContributions />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/notifications"
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <ManageNotifications />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/summary"
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <AdminSummary />
              </ProtectedRoute>
            }
          />
        </Routes>
      </Router>

      {/*  Global Toast Provider */}
      <Toaster
        position="top-center"
        toastOptions={{
          duration: 5000,
          style: {
            background: "#fff",
            color: "#111",
            padding: "16px 22px",
            borderRadius: "14px",
            fontSize: "16px",
            fontWeight: "600",
            boxShadow:
              "0 8px 20px rgba(0,0,0,0.15), 0 4px 10px rgba(0,0,0,0.1)",
            animation: "fadeInScale 0.3s ease-out",
          },
          success: {
            style: {
              background: "#ecfdf5",
              color: "#065f46",
              border: "1px solid #10b981",
            },
            iconTheme: {
              primary: "#10b981",
              secondary: "#fff",
            },
          },
          error: {
            style: {
              background: "#fef2f2",
              color: "#991b1b",
              border: "1px solid #ef4444",
            },
            iconTheme: {
              primary: "#ef4444",
              secondary: "#fff",
            },
          },
        }}
      />

      {/* Custom Toast Animation */}
      <style>{`
        @keyframes fadeInScale {
          0% { opacity: 0; transform: scale(0.9); }
          100% { opacity: 1; transform: scale(1); }
        }
      `}</style>
    </AuthProvider>
  );
}

export default App;
