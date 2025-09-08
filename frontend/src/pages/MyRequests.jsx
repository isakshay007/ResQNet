import React, { useEffect, useState } from "react";
import api from "../utils/api"; // axios wrapper
import Navbar from "../components/Navbar/Navbar";
import Footer from "../components/Footer";

function MyRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchRequests = async () => {
      try {
        const res = await api.get("/requests/my"); // GET /api/requests/my
        setRequests(res.data);
      } catch (err) {
        console.error("Failed to fetch requests:", err);
        setError("Could not load requests. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    fetchRequests();
  }, []);

  return (
    <div
      className="flex flex-col min-h-screen 
                 bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 
                 text-white animate-gradient-x"
    >
      {/* Navbar */}
      <Navbar active="my-requests" />

      {/* Page Content */}
      <main className="flex-1 px-6 py-8">
        <div className="bg-white/95 text-gray-900 rounded-2xl shadow-lg p-6 animate-fadeIn">
          {/* Heading */}
          <h2
            className="text-3xl font-extrabold mb-6 
                       bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                       bg-clip-text text-transparent"
          >
            My Requests
          </h2>

          {loading && <p className="text-gray-600">Loading...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {!loading && !error && requests.length === 0 && (
            <p className="text-gray-600">No requests created yet.</p>
          )}

          {!loading && !error && requests.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse rounded-lg overflow-hidden shadow-md">
                <thead>
                  <tr className="bg-teal-600 text-white text-left">
                    <th className="p-3">Category</th>
                    <th className="p-3">Requested Qty</th>
                    <th className="p-3">Fulfilled Qty</th>
                    <th className="p-3">Status</th>
                    <th className="p-3">Disaster ID</th>
                    <th className="p-3">Created At</th>
                  </tr>
                </thead>
                <tbody>
                  {requests.map((r) => (
                    <tr
                      key={r.id}
                      className="odd:bg-gray-50 even:bg-gray-100 hover:bg-teal-50 transition"
                    >
                      <td className="p-3 font-semibold text-gray-800">
                        {r.category}
                      </td>
                      <td className="p-3 text-gray-700">{r.requestedQuantity}</td>
                      <td className="p-3 text-gray-700">{r.fulfilledQuantity}</td>
                      <td
                        className={`p-3 font-bold ${
                          r.status === "FULFILLED"
                            ? "text-green-600"
                            : "text-yellow-600"
                        }`}
                      >
                        {r.status}
                      </td>
                      <td className="p-3 text-gray-600">{r.disasterId}</td>
                      <td className="p-3 text-gray-600 text-sm">
                        {new Date(r.createdAt).toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default MyRequests;
