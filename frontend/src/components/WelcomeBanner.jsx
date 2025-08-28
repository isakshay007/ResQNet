import React from "react";

function WelcomeBanner({ user }) {
  return (
    <div className="p-6 text-center bg-gradient-to-r from-teal-400 via-cyan-400 to-blue-500 text-white shadow-md">
      <h1 className="text-2xl font-bold">
        Welcome, {user?.name || user?.email} ({user?.role})
      </h1>
    </div>
  );
}

export default WelcomeBanner;   // ✅ default export
