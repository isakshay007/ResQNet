// src/pages/MyRequests.jsx
import React, { useEffect, useState } from "react";
import api from "../../utils/api"; // axios wrapper
import Navbar from "../../components/Navbar/Navbar";
import Footer from "../../components/Footer";

function MyRequests() {
  const [requests, setRequests] = useState([]);
  const [filteredRequests, setFilteredRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // filters
  const [categoryFilter, setCategoryFilter] = useState("None");
  const [statusFilter, setStatusFilter] = useState("None");

  // pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    const fetchRequests = async () => {
      try {
        const res = await api.get("/requests/my"); // GET /api/requests/my
        setRequests(res.data);
        setFilteredRequests(res.data);
      } catch (err) {
        console.error("Failed to fetch requests:", err);
        setError("Could not load requests. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    fetchRequests();
  }, []);

  // status badge helper
  const renderStatus = (status) => {
    const s = status?.toUpperCase();
    let color =
      s === "FULFILLED"
        ? "bg-green-100 text-green-700 border-green-300"
        : s === "PARTIAL"
        ? "bg-yellow-100 text-yellow-700 border-yellow-300"
        : "bg-red-100 text-red-700 border-red-300";

    return (
      <span
        className={`px-2 py-1 rounded-full text-xs font-bold border ${color}`}
      >
        {s}
      </span>
    );
  };

  // handle filter
  const handleSearch = () => {
    let results = requests;

    if (categoryFilter !== "None") {
      results = results.filter(
        (r) => r.category.toLowerCase() === categoryFilter.toLowerCase()
      );
    }
    if (statusFilter !== "None") {
      results = results.filter(
        (r) => r.status.toLowerCase() === statusFilter.toLowerCase()
      );
    }

    setFilteredRequests(results);
    setCurrentPage(1); // reset pagination
  };

  // pagination calculations
  const totalPages = Math.ceil(filteredRequests.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentItems = filteredRequests.slice(
    startIndex,
    startIndex + itemsPerPage
  );

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
          {/* Header with filters aligned right */}
          <div className="flex justify-between items-center mb-6">
            <h2
              className="text-3xl font-extrabold 
                         bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                         bg-clip-text text-transparent"
            >
              My Requests
            </h2>

            {/* Filter Section */}
            <div className="flex flex-wrap gap-4 items-end">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Category
                </label>
                <select
                  value={categoryFilter}
                  onChange={(e) => setCategoryFilter(e.target.value)}
                  className="border p-2 rounded-lg text-gray-800"
                >
                  <option value="None">None</option>
                  <option value="food">Food</option>
                  <option value="water">Water</option>
                  <option value="medical">Medical</option>
                  <option value="shelter">Shelter</option>
                  <option value="other">Other</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Status
                </label>
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="border p-2 rounded-lg text-gray-800"
                >
                  <option value="None">None</option>
                  <option value="pending">Pending</option>
                  <option value="partial">Partial</option>
                  <option value="fulfilled">Fulfilled</option>
                </select>
              </div>

              <button
                onClick={handleSearch}
                className="bg-teal-600 text-white px-4 py-2 rounded-lg hover:bg-teal-700 transition"
              >
                Done
              </button>
            </div>
          </div>

          {loading && <p className="text-gray-600">Loading...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {!loading && !error && filteredRequests.length === 0 && (
            <p className="text-gray-600">No requests found.</p>
          )}

          {!loading && !error && filteredRequests.length > 0 && (
            <>
              <div className="overflow-x-auto">
                <table className="w-full table-fixed border-collapse rounded-lg overflow-hidden shadow">
                  <thead>
                    <tr className="bg-teal-600 text-white text-left text-sm uppercase tracking-wider">
                      <th className="p-3 w-[18%]">Category</th>
                      <th className="p-3 w-[15%]">Requested Qty</th>
                      <th className="p-3 w-[15%]">Fulfilled Qty</th>
                      <th className="p-3 w-[15%]">Status</th>
                      <th className="p-3 w-[12%]">Disaster ID</th>
                      <th className="p-3 w-[25%]">Created At</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentItems.map((r) => (
                      <tr
                        key={r.id}
                        className="odd:bg-gray-50 even:bg-gray-100 hover:bg-teal-50 transition"
                      >
                        <td className="p-3 font-semibold text-gray-800 capitalize truncate">
                          {r.category}
                        </td>
                        <td className="p-3 text-gray-700">{r.requestedQuantity}</td>
                        <td className="p-3 text-gray-700">{r.fulfilledQuantity}</td>
                        <td className="p-3">{renderStatus(r.status)}</td>
                        <td className="p-3 text-gray-600">{r.disasterId}</td>
                        <td className="p-3 text-gray-600 text-sm truncate">
                          {new Date(r.createdAt).toLocaleString()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

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

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default MyRequests;
