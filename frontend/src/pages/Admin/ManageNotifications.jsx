// src/pages/Admin/ManageNotifications.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../utils/api";
import AdminNavbar from "../../components/Navbar/AdminNavbar";
import Footer from "../../components/Footer";
import { useAuth } from "../../context/AuthContext";
import { FiAlertCircle, FiBell, FiInfo } from "react-icons/fi";

function ManageNotifications() {
  const navigate = useNavigate();
  const { token, user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  // fetch notifications
  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const res = await api.get("/admin/notifications", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setNotifications(res.data);
      } catch (err) {
        console.error("Failed to fetch notifications:", err.response || err);
        setError(
          err.response?.data?.message || "Could not load notifications."
        );
      } finally {
        setLoading(false);
      }
    };

    if (token && user?.role === "ADMIN") {
      fetchNotifications();
    } else {
      setError("You must be logged in as an admin to view notifications.");
      setLoading(false);
    }
  }, [token, user]);

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this notification?")) return;
    try {
      await api.delete(`/admin/notifications/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setNotifications((prev) => prev.filter((n) => n.id !== id));
    } catch (err) {
      console.error("Failed to delete notification:", err);
      alert("Error deleting notification.");
    }
  };

  // pick icon
  const getIcon = (type) => {
    switch (type?.toLowerCase()) {
      case "alert":
        return <FiAlertCircle className="text-red-500" size={20} />;
      case "request":
        return <FiBell className="text-blue-500" size={20} />;
      default:
        return <FiInfo className="text-gray-500" size={20} />;
    }
  };

  // pagination calc
  const totalPages = Math.ceil(notifications.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentItems = notifications.slice(
    startIndex,
    startIndex + itemsPerPage
  );

  return (
    <div
      className="flex flex-col min-h-screen 
                 bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 
                 text-white animate-gradient-x"
    >
      <AdminNavbar />

      <main className="flex-1 px-6 py-8">
        <div className="bg-white/95 text-gray-900 rounded-2xl shadow-lg p-6 animate-fadeIn">
          <div className="flex justify-between items-center mb-6">
            <h2
              className="text-3xl font-extrabold 
                         bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                         bg-clip-text text-transparent"
            >
              Manage Notifications
            </h2>
            <button
              onClick={() => navigate("/admin/dashboard")}
              className="px-4 py-2 bg-teal-600 text-white rounded-lg hover:bg-teal-700"
            >
              ← Go Back
            </button>
          </div>

          {loading && <p className="text-gray-600">Loading notifications...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {!loading && !error && notifications.length === 0 && (
            <div className="flex flex-col items-center text-gray-500 py-12">
              <FiBell size={40} className="mb-3 text-teal-500" />
              <p className="text-lg font-medium">No notifications yet</p>
              <p className="text-sm">New updates will appear here.</p>
            </div>
          )}

          {!loading && notifications.length > 0 && (
            <>
              <ul className="space-y-4">
                {currentItems.map((n) => (
                  <li
                    key={n.id}
                    className="p-4 rounded-lg border shadow-sm transition flex items-start gap-3 bg-gray-50 hover:bg-teal-50"
                  >
                    {/* Icon */}
                    <div className="mt-1">{getIcon(n.type)}</div>

                    {/* Content */}
                    <div className="flex-1">
                      <div className="flex justify-between items-center mb-1">
                        <span className="font-semibold text-teal-700">
                          {n.type || "System"}
                        </span>
                        <span className="text-xs text-gray-500">
                          {n.createdAt
                            ? new Date(n.createdAt).toLocaleString()
                            : "-"}
                        </span>
                      </div>

                      <p className="text-gray-700 text-sm leading-snug break-words">
                        {n.message}
                      </p>

                      {/* Actions */}
                      <div className="flex space-x-3 mt-3">
                        <button
                          onClick={() => handleDelete(n.id)}
                          className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 text-xs font-medium"
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex justify-center mt-6 space-x-2">
                  <button
                    disabled={currentPage === 1}
                    onClick={() => setCurrentPage((p) => p - 1)}
                    className={`px-3 py-1 rounded ${
                      currentPage === 1
                        ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                        : "bg-teal-600 text-white hover:bg-teal-700"
                    }`}
                  >
                    Prev
                  </button>
                  {[...Array(totalPages)].map((_, i) => (
                    <button
                      key={i}
                      onClick={() => setCurrentPage(i + 1)}
                      className={`px-3 py-1 rounded ${
                        currentPage === i + 1
                          ? "bg-teal-700 text-white"
                          : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}
                  <button
                    disabled={currentPage === totalPages}
                    onClick={() => setCurrentPage((p) => p + 1)}
                    className={`px-3 py-1 rounded ${
                      currentPage === totalPages
                        ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                        : "bg-teal-600 text-white hover:bg-teal-700"
                    }`}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
}

export default ManageNotifications;
