import React from "react";
import Navbar from "../components/Navbar/Navbar";
import MapView from "../components/MapView/MapView";
import Footer from "../components/Footer";

function Dashboard() {
  return (
    <div className="flex flex-col min-h-screen bg-gray-900 text-white">
      {/* Navbar */}
      <Navbar />

      {/* Map Section */}
      <div className="flex-1 px-6 py-6">
        <div className="w-full h-[calc(100vh-160px)] rounded-3xl shadow-2xl overflow-hidden">
          <MapView />
        </div>
      </div>

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default Dashboard;
