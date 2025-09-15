import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar/Navbar";
import Footer from "../components/Footer";
import { useAuth } from "../context/AuthContext";
import ReporterMapView from "../components/MapView/ReporterMapView";
import ResponderMapView from "../components/MapView/ResponderMapView";

function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  //  Redirect Admins to their own dashboard
  useEffect(() => {
    if (user?.role === "ADMIN") {
      navigate("/admin/dashboard");
    }
  }, [user, navigate]);

  return (
    <div
      className="flex flex-col min-h-screen 
                 bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 
                 text-white animate-gradient-x"
    >
      {/* Navbar */}
      <Navbar />

      {/* Map Section */}
      <main className="flex-1 px-6 py-6">
        <div
          className="w-full h-[calc(100vh-160px)] rounded-3xl shadow-2xl overflow-hidden 
                     bg-white/95 text-gray-900 animate-fadeIn"
        >
          {user?.role === "REPORTER" && <ReporterMapView />}
          {user?.role === "RESPONDER" && <ResponderMapView />}
        </div>
      </main>

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default Dashboard;
