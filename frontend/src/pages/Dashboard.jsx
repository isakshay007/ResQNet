import React from "react";
import { useAuth } from "../context/AuthContext";

function Dashboard() {
  const { user, logout } = useAuth();

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold">Dashboard 🎉</h1>
      {user && (
        <p className="mt-2 text-gray-700">
          Logged in as <strong>{user.email}</strong> ({user.role})
        </p>
      )}
      <button
        onClick={logout}
        className="mt-4 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600"
      >
        Logout
      </button>
    </div>
  );
}

export default Dashboard; // ✅ now App.jsx can import it
