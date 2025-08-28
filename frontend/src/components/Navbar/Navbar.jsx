import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="flex justify-between items-center bg-gray-900 text-white px-6 py-3 shadow-md">
      {/* Left side: Dynamic Title */}
      <h1
        className="text-lg font-bold cursor-pointer"
        onClick={() => navigate("/dashboard")}
      >
        {user?.role || "User"} – ResQNet Dashboard
      </h1>

      {/* Right side: Links */}
      <nav className="flex space-x-6">
        <button onClick={() => navigate("/my-disasters")}> My Disasters</button>
        <button onClick={() => navigate("/my-requests")}>  My Requests</button>
        <button onClick={() => navigate("/contributions")}> Contributions</button>
        <button onClick={() => navigate("/notifications")}> Notifications</button>
        <button
          onClick={logout}
          className="bg-red-500 px-3 py-1 rounded hover:bg-red-600"
        >
          Logout
        </button>
      </nav>
    </header>
  );
}

export default Navbar;
