import React, { useEffect, useState } from "react";
import api from "../utils/api"; // use axios wrapper
import Navbar from "../components/Navbar/Navbar";
import Footer from "../components/Footer";
import { useAuth } from "../context/AuthContext";

function Notifications() {
  const { token } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch all notifications for logged-in user
  const fetchNotifications = async () => {
    try {
      const res = await api.get("/notifications"); // wrapper auto handles headers
      setNotifications(res.data);
    } catch (err) {
      console.error("Failed to fetch notifications:", err);
    } finally {
      setLoading(false);
    }
  };

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

      // Optional: auto-refresh every 60s
      const interval = setInterval(fetchNotifications, 60000);
      return () => clearInterval(interval);
    }
  }, [token]);

  const unreadCount = notifications.filter((n) => !n.read).length;

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
            <div className="flex flex-col items-center text-gray-600 py-6">
              <span className="text-4xl mb-2">🔔</span>
              <p>No notifications yet.</p>
            </div>
          ) : (
            <ul className="space-y-4">
              {notifications.map((n) => (
                <li
                  key={n.id}
                  className={`p-4 rounded-lg border shadow-sm transition ${
                    n.read ? "bg-gray-100" : "bg-teal-50"
                  }`}
                >
                  <div className="flex justify-between items-center mb-2">
                    <span
                      className={`font-semibold ${
                        n.read ? "text-gray-600" : "text-teal-700"
                      }`}
                    >
                      {n.type || "SYSTEM"}
                    </span>
                    <span className="text-xs text-gray-500">
                      {n.createdAt
                        ? new Date(n.createdAt).toLocaleString()
                        : "-"}
                    </span>
                  </div>

                  <p className="text-gray-700 mb-3">{n.message}</p>

                  <div className="flex space-x-3">
                    {!n.read && (
                      <button
                        onClick={() => markAsRead(n.id)}
                        className="px-3 py-1 bg-teal-500 text-white rounded hover:bg-teal-600 text-sm"
                      >
                        Mark as Read
                      </button>
                    )}
                    {n.deletable && (
                      <button
                        onClick={() => deleteNotification(n.id)}
                        className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 text-sm"
                      >
                        Delete
                      </button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
}

export default Notifications;
