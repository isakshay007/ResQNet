// src/pages/Admin/AdminDashboard.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion"; //  for animations
import api from "../../utils/api";
import AdminNavbar from "../../components/Navbar/AdminNavbar";
import Footer from "../../components/Footer";

function AdminDashboard() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        setLoading(true);
        await api.get("/admin/summary");
      } catch (err) {
        console.error("Failed to fetch summary:", err);
        setError("Could not load summary data.");
      } finally {
        setLoading(false);
      }
    };
    fetchSummary();
  }, []);

  const features = [
    {
      title: "Manage Users",
      desc: "View and control user accounts",
      icon: "👤",
      color: "from-purple-500 to-pink-500",
      path: "/admin/users",
    },
    {
      title: "Manage Disasters",
      desc: "Oversee reported disasters",
      icon: "⚠️",
      color: "from-red-500 to-orange-500",
      path: "/admin/disasters",
    },
    {
      title: "Manage Requests",
      desc: "Track and manage resource requests",
      icon: "📦",
      color: "from-blue-500 to-indigo-500",
      path: "/admin/requests",
    },
    {
      title: "Manage Contributions",
      desc: "Monitor responder contributions",
      icon: "🤝",
      color: "from-green-500 to-emerald-500",
      path: "/admin/contributions",
    },
    {
      title: "Manage Notifications",
      desc: "Configure and review alerts",
      icon: "🔔",
      color: "from-yellow-500 to-amber-500",
      path: "/admin/notifications",
    },
    {
      title: "System Summary",
      desc: "View overall platform analytics",
      icon: "📊",
      color: "from-teal-500 to-cyan-500",
      path: "/admin/summary",
    },
  ];

  // Animation variants
  const cardVariants = {
    hidden: { opacity: 0, y: 40 },
    visible: (i) => ({
      opacity: 1,
      y: 0,
      transition: { delay: i * 0.15, duration: 0.6, ease: "easeOut" },
    }),
  };

  return (
    <div
      className="flex flex-col min-h-screen 
                 bg-gradient-to-br from-cyan-400 via-sky-500 to-blue-600 
                 text-white animate-gradient-x"
    >
      {/* Admin Navbar */}
      <AdminNavbar />

      {/* Page Content */}
      <main className="flex-1 px-6 py-12">
        <div className="bg-gradient-to-b from-blue-50 to-white text-gray-900 rounded-2xl shadow-2xl p-12">
          {/* Heading with animated gradient */}
          <div className="mb-12 text-center">
            <h2
              className="text-5xl font-extrabold 
                         bg-gradient-to-r from-teal-600 via-sky-500 to-blue-600 
                         bg-clip-text text-transparent animate-pulse"
            >
              Admin Dashboard
            </h2>
            <p className="text-gray-600 mt-3 text-base">
              Manage all aspects of the ResQNet platform
            </p>
          </div>

          {/* Error */}
          {error && <p className="text-red-600 text-center">{error}</p>}

          {/* Features Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-10">
            {features.map((f, idx) => (
              <motion.div
                key={idx}
                custom={idx}
                initial="hidden"
                animate="visible"
                variants={cardVariants}
                onClick={() => navigate(f.path)}
                className={`group cursor-pointer rounded-2xl shadow-lg 
                            hover:shadow-2xl p-10 flex flex-col items-center justify-center
                            transition transform hover:-translate-y-2 
                            bg-gradient-to-br ${f.color}`}
              >
                {/* Icon */}
                <div
                  className="w-20 h-20 flex items-center justify-center 
                             text-4xl rounded-full bg-white/20 text-white shadow-lg
                             group-hover:scale-110 transition"
                >
                  {f.icon}
                </div>
                {/* Title */}
                <h3 className="mt-6 text-xl font-bold text-white drop-shadow-lg">
                  {f.title}
                </h3>
                {/* Description */}
                <p className="text-sm text-white/90 mt-2 text-center">
                  {f.desc}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}

export default AdminDashboard;
