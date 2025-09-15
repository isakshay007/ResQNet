// src/components/Navbar/AdminNavbar.jsx
import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import ConfirmModal from "../common/ConfirmModal"; // reuse confirm modal

function AdminNavbar() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showConfirm, setShowConfirm] = useState(false);

  const handleLogout = () => {
    logout(); // clear token/session
    setShowConfirm(false);
    navigate("/login"); // redirect to login
  };

  // active link style
  const linkClass = (path) => {
    const isActive = location.pathname === path;
    return `hover:text-teal-600 transition ${
      isActive ? "font-bold underline text-teal-700" : ""
    }`;
  };

  return (
    <>
      <header className="fixed top-0 left-0 right-0 z-[1000] flex justify-between items-center bg-white/95 backdrop-blur-lg text-gray-900 px-6 py-3 shadow-md">
        {/* Left side */}
        <h1
          className="text-lg font-bold cursor-pointer bg-gradient-to-r from-teal-500 via-blue-600 to-teal-500 bg-[length:200%_100%] bg-clip-text text-transparent animate-subtle-shimmer"
          onClick={() => navigate("/admin/dashboard")}
        >
          ADMIN – ResQNet Dashboard
        </h1>

        {/* Navigation links */}
        <nav className="flex space-x-6 items-center">
          <button
            onClick={() => navigate("/admin/dashboard")}
            className={linkClass("/admin/dashboard")}
          >
            Admin Dashboard
          </button>
          <button
            onClick={() => navigate("/admin/notifications")}
            className={linkClass("/admin/notifications")}
          >
            Notifications
          </button>

          {/* Logout */}
          <button
            onClick={() => setShowConfirm(true)}
            className="relative z-[1001] bg-red-500 px-3 py-1 rounded hover:bg-red-600 text-white"
          >
            Logout
          </button>
        </nav>

        {/* Subtle shimmer animation */}
        <style>{`
          @keyframes subtle-shimmer {
            0% { background-position: -200% 0; }
            100% { background-position: 200% 0; }
          }
          .animate-subtle-shimmer {
            animation: subtle-shimmer 8s linear infinite;
          }
        `}</style>
      </header>

      {/* Push content below navbar */}
      <div className="h-[64px]" />

      {/* Logout Confirmation */}
      {showConfirm && (
        <ConfirmModal
          title="Are you sure you want to logout?"
          message="You will need to log in again to access your account."
          onConfirm={handleLogout}
          onCancel={() => setShowConfirm(false)}
        />
      )}
    </>
  );
}

export default AdminNavbar;
