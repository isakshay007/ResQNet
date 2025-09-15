import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import ConfirmModal from "../common/ConfirmModal"; // reusable modal

function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showConfirm, setShowConfirm] = useState(false);

  const handleLogout = () => {
    logout(); // clears token + redirects (handled in AuthContext)
    setShowConfirm(false);
  };

  // helper for active link styling (works for exact & prefix match)
  const linkClass = (path, prefixMatch = false) => {
    const isActive = prefixMatch
      ? location.pathname.startsWith(path)
      : location.pathname === path;
    return `hover:text-teal-600 transition ${
      isActive ? "font-bold underline text-teal-700" : ""
    }`;
  };

  return (
    <>
      {/* Fixed Navbar pinned at the top */}
      <header className="fixed top-0 left-0 right-0 z-[1000] flex justify-between items-center bg-white/90 backdrop-blur-lg text-gray-900 px-6 py-3 shadow-md">
        {/* Left side: Dynamic Title */}
        <h1
          className="text-lg font-bold cursor-pointer bg-gradient-to-r from-teal-500 via-blue-600 to-teal-500 bg-[length:200%_100%] bg-clip-text text-transparent animate-subtle-shimmer"
          onClick={() =>
            user?.role === "ADMIN"
              ? navigate("/admin/dashboard")
              : navigate("/dashboard")
          }
        >
          {user?.role || "User"} – ResQNet Dashboard
        </h1>

        {/* Right side: Role-based links */}
        <nav className="flex space-x-6 items-center">
          {/* Reporter & Responder: My Disasters */}
          {(user?.role === "REPORTER" || user?.role === "RESPONDER") && (
            <button
              onClick={() => navigate("/my-disasters")}
              className={linkClass("/my-disasters")}
            >
              My Disasters
            </button>
          )}

          {/* Reporter-only */}
          {user?.role === "REPORTER" && (
            <>
              <button
                onClick={() => navigate("/my-requests")}
                className={linkClass("/my-requests")}
              >
                My Requests
              </button>
              <button
                onClick={() => navigate("/contributions")}
                className={linkClass("/contributions")}
              >
                Contributions
              </button>
            </>
          )}

          {/* Responder-only */}
          {user?.role === "RESPONDER" && (
            <>
              <button
                onClick={() => navigate("/all-requests")}
                className={linkClass("/all-requests")}
              >
                All Requests
              </button>
              <button
                onClick={() => navigate("/my-contributions")}
                className={linkClass("/my-contributions")}
              >
                My Contributions
              </button>
            </>
          )}

          {/* Admin-only */}
          {user?.role === "ADMIN" && (
            <>
              <button
                onClick={() => navigate("/admin/dashboard")}
                className={linkClass("/admin", true)} // highlight on any /admin/*
              >
                Admin Dashboard
              </button>
              <button
                onClick={() => navigate("/admin/summary")}
                className={linkClass("/admin/summary")}
              >
                Summary
              </button>
              <button
                onClick={() => navigate("/admin/users")}
                className={linkClass("/admin/users")}
              >
                Users
              </button>
              <button
                onClick={() => navigate("/admin/disasters")}
                className={linkClass("/admin/disasters")}
              >
                Disasters
              </button>
              <button
                onClick={() => navigate("/admin/requests")}
                className={linkClass("/admin/requests")}
              >
                Requests
              </button>
              <button
                onClick={() => navigate("/admin/contributions")}
                className={linkClass("/admin/contributions")}
              >
                Contributions
              </button>
              <button
                onClick={() => navigate("/admin/notifications")}
                className={linkClass("/admin/notifications")}
              >
                Notifications
              </button>
            </>
          )}

          {/* Shared */}
          <button
            onClick={() => navigate("/notifications")}
            className={linkClass("/notifications")}
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

        {/* Subtle shimmer style */}
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

      {/* Spacer so UI content is pushed below Navbar */}
      <div className="h-[64px]" />

      {/* Logout Confirmation Modal */}
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

export default Navbar;
