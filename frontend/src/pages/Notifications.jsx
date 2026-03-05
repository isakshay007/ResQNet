// src/pages/Notifications.jsx
import React, { useEffect, useState, useCallback } from "react";
import api from "../utils/api";
import Navbar from "../components/Navbar/Navbar";
import Footer from "../components/Footer";
import { useAuth } from "../context/AuthContext";
import { useWebSocket } from "../hooks/useWebSocket";
import { FiAlertCircle, FiBell, FiInfo } from "react-icons/fi";

function Notifications() {
  const { token } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  // pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const fetchNotifications = async () => {
    try {
      const res = await api.get("/notifications");
      setNotifications(res.data);
    } catch (err) {
      console.error("Failed to fetch notifications:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleNewNotification = useCallback((notification) => {
    setNotifications((prev) => {
      if (prev.some((n) => n.id === notification.id)) return prev;
      return [notification, ...prev];
    });
    setCurrentPage(1);
  }, []);

  useWebSocket(handleNewNotification);

  const markAsRead = async (id) => {
    try {
      await api.put(`/notifications/${id}/read`);
      fetchNotifications();
    } catch (err) {
      console.error("Failed to mark as read:", err);
    }
  };

  const deleteNotification = async (id) => {
    try {
      await api.delete(`/notifications/${id}`);
      fetchNotifications();
    } catch (err) {
      console.error("Failed to delete notification:", err);
    }
  };

  useEffect(() => {
    if (token) {
      fetchNotifications();

      // auto-refresh every 60s
      const interval = setInterval(fetchNotifications, 60000);
      return () => clearInterval(interval);
    }
  }, [token]);

  const unreadCount = notifications.filter((n) => !n.read).length;

  // Pick icon based on type
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

  // pagination calculations
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
      <Navbar active="notifications" notificationCount={unreadCount} />

      <main className="flex-1 px-6 py-8">
        <div className="bg-white/95 text-gray-900 rounded-2xl shadow-lg p-6 animate-fadeIn">
          <h2
            className="text-3xl font-extrabold mb-6 
                       bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                       bg-clip-text text-transparent"
          >
            Notifications
          </h2>

          {loading ? (
            <p className="text-gray-600">Loading...</p>
          ) : notifications.length === 0 ? (
            <div className="flex flex-col items-center text-gray-500 py-12">
              <FiBell size={40} className="mb-3 text-teal-500" />
              <p className="text-lg font-medium">You’re all caught up!</p>
              <p className="text-sm">No new notifications right now.</p>
            </div>
          ) : (
            <>
              <ul className="space-y-4">
                {currentItems.map((n) => (
                  <li
                    key={n.id}
                    className={`p-4 rounded-lg border shadow-sm transition flex items-start gap-3 ${
                      n.read ? "bg-gray-100" : "bg-teal-50 border-teal-200"
                    } hover:shadow-md`}
                  >
                    {/* Icon */}
                    <div className="mt-1">{getIcon(n.type)}</div>

                    {/* Content */}
                    <div className="flex-1">
                      <div className="flex justify-between items-center mb-1">
                        <span
                          className={`font-semibold ${
                            n.read ? "text-gray-700" : "text-teal-700"
                          }`}
                        >
                          {n.type || "System"}
                          {!n.read && (
                            <span className="ml-2 px-2 py-0.5 text-xs bg-teal-600 text-white rounded-full">
                              New
                            </span>
                          )}
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
                        {!n.read && (
                          <button
                            onClick={() => markAsRead(n.id)}
                            className="px-3 py-1 bg-teal-500 text-white rounded hover:bg-teal-600 text-xs font-medium"
                          >
                            Mark as Read
                          </button>
                        )}
                        {n.deletable && (
                          <button
                            onClick={() => deleteNotification(n.id)}
                            className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 text-xs font-medium"
                          >
                            Delete
                          </button>
                        )}
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

export default Notifications;
