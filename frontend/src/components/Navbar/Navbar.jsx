import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import ConfirmModal from "../common/ConfirmModal"; // reusable modal

function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [showConfirm, setShowConfirm] = useState(false);

  const handleLogout = () => {
    logout();              // clears token + redirects (handled in AuthContext)
    setShowConfirm(false); // close modal
  };

  return (
    <>
      {/*  Fixed Navbar pinned at the top */}
      <header className="fixed top-0 left-0 right-0 z-[1000] flex justify-between items-center bg-white/90 backdrop-blur-lg text-gray-900 px-6 py-3 shadow-md">
        {/* Left side: Dynamic Title with subtle shimmer */}
        <h1
          className="text-lg font-bold cursor-pointer bg-gradient-to-r from-teal-500 via-blue-600 to-teal-500 bg-[length:200%_100%] bg-clip-text text-transparent animate-subtle-shimmer"
          onClick={() => navigate("/dashboard")}
        >
          {user?.role || "User"} – ResQNet Dashboard
        </h1>

        {/* Right side: Links */}
        <nav className="flex space-x-6 items-center">
          <button
            onClick={() => navigate("/my-disasters")}
            className="hover:text-teal-600 transition"
          >
            My Disasters
          </button>
          <button
            onClick={() => navigate("/my-requests")}
            className="hover:text-teal-600 transition"
          >
            My Requests
          </button>
          <button
            onClick={() => navigate("/contributions")}
            className="hover:text-teal-600 transition"
          >
            Contributions
          </button>
          <button
            onClick={() => navigate("/notifications")}
            className="hover:text-teal-600 transition"
          >
            Notifications
          </button>

          {/* Logout triggers confirm modal */}
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
