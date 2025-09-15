// src/pages/Admin/AdminSummary.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../utils/api";
import AdminNavbar from "../../components/Navbar/AdminNavbar";
import Footer from "../../components/Footer";
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  Title,
} from "chart.js";
import ChartDataLabels from "chartjs-plugin-datalabels";
import { Pie } from "react-chartjs-2";

// Register chart.js components
ChartJS.register(ArcElement, Tooltip, Legend, Title, ChartDataLabels);

function AdminSummary() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const res = await api.get("/admin/summary");
        setSummary(res.data);
      } catch (err) {
        console.error("Failed to fetch summary:", err);
        setError("Could not load admin summary.");
      } finally {
        setLoading(false);
      }
    };
    fetchSummary();
  }, []);

  //  User Roles (Reporter vs Responder only)
  const userRoleCounts = summary?.userRoleCounts || {};
  const filteredRoles = Object.fromEntries(
    Object.entries(userRoleCounts).filter(([role]) =>
      ["REPORTER", "RESPONDER"].includes(role.toUpperCase())
    )
  );
  const userRolesData = {
    labels: Object.keys(filteredRoles),
    datasets: [
      {
        data: Object.values(filteredRoles),
        backgroundColor: ["#0ea5e9", "#14b8a6"], // reporter blue, responder teal
      },
    ],
  };

  //  Request Status (Partial vs Fulfilled)
  const requestStatusCounts = summary?.requestStatusCounts || {};
  const requestStatusData = {
    labels: ["PARTIAL", "FULFILLED"],
    datasets: [
      {
        data: [
          requestStatusCounts.PARTIAL || 0,
          requestStatusCounts.FULFILLED || 0,
        ],
        backgroundColor: ["#f97316", "#22c55e"], // orange + green
      },
    ],
  };

  return (
    <div className="flex flex-col min-h-screen bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 text-white animate-gradient-x">
      <AdminNavbar />

      <main className="flex-1 px-6 py-8">
        <div className="bg-white/95 text-gray-900 rounded-2xl shadow-xl p-8">
          {/* Header */}
          <div className="flex justify-between items-center mb-8">
            <h2 className="text-3xl font-extrabold bg-gradient-to-r from-teal-700 via-cyan-600 to-blue-600 bg-clip-text text-transparent">
               System Insights
            </h2>
            <button
              onClick={() => navigate("/admin/dashboard")}
              className="px-5 py-2.5 bg-teal-600 text-white rounded-lg shadow hover:bg-teal-700 transition"
            >
              ← Go Back
            </button>
          </div>

          {/* Loading/Error */}
          {loading && <p className="text-gray-600">Loading summary...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {summary && (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Stats row full width */}
              <div className="col-span-2 grid grid-cols-2 md:grid-cols-4 gap-6">
                <div className="p-5 bg-gradient-to-tr from-teal-100 to-teal-200 rounded-lg shadow text-center hover:shadow-md transition">
                  <h3 className="font-bold text-base text-teal-700">Users</h3>
                  <p className="text-3xl font-extrabold">{summary.totalUsers}</p>
                </div>
                <div className="p-5 bg-gradient-to-tr from-blue-100 to-blue-200 rounded-lg shadow text-center hover:shadow-md transition">
                  <h3 className="font-bold text-base text-blue-700">Disasters</h3>
                  <p className="text-3xl font-extrabold">{summary.totalDisasters}</p>
                </div>
                <div className="p-5 bg-gradient-to-tr from-indigo-100 to-indigo-200 rounded-lg shadow text-center hover:shadow-md transition">
                  <h3 className="font-bold text-base text-indigo-700">Requests</h3>
                  <p className="text-3xl font-extrabold">{summary.totalRequests}</p>
                </div>
                <div className="p-5 bg-gradient-to-tr from-orange-100 to-orange-200 rounded-lg shadow text-center hover:shadow-md transition">
                  <h3 className="font-bold text-base text-orange-700">Contributions</h3>
                  <p className="text-3xl font-extrabold">{summary.totalContributions}</p>
                </div>
              </div>

              {/* Pie Chart 1: User Roles */}
              <div className="p-6 bg-gray-50 rounded-lg shadow hover:shadow-md transition h-96 flex flex-col">
                <h3 className="text-lg font-semibold mb-4 text-gray-800 text-center">
                  User Roles
                </h3>
                <div className="flex-1">
                  <Pie
                    data={userRolesData}
                    options={{
                      responsive: true,
                      maintainAspectRatio: false,
                      plugins: {
                        legend: { position: "bottom" },
                        datalabels: {
                          formatter: (value) => value,
                          color: "#fff",
                          font: { weight: "bold", size: 14 },
                        },
                      },
                    }}
                  />
                </div>
              </div>

              {/* Pie Chart 2: Request Status */}
              <div className="p-6 bg-gray-50 rounded-lg shadow hover:shadow-md transition h-96 flex flex-col">
                <h3 className="text-lg font-semibold mb-4 text-gray-800 text-center">
                  Request Status
                </h3>
                <div className="flex-1">
                  <Pie
                    data={requestStatusData}
                    options={{
                      responsive: true,
                      maintainAspectRatio: false,
                      plugins: {
                        legend: { position: "bottom" },
                        datalabels: {
                          formatter: (value) => value,
                          color: "#fff",
                          font: { weight: "bold", size: 14 },
                        },
                      },
                    }}
                  />
                </div>
              </div>
            </div>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
}

export default AdminSummary;
