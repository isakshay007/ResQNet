// src/pages/Admin/ManageRequests.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../utils/api";
import AdminNavbar from "../../components/Navbar/AdminNavbar";
import Footer from "../../components/Footer";

function ManageRequests() {
  const navigate = useNavigate();
  const [requests, setRequests] = useState([]);
  const [filteredRequests, setFilteredRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // filters
  const [filterCategory, setFilterCategory] = useState("None");
  const [filterStatus, setFilterStatus] = useState("None");

  // pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    const fetchRequests = async () => {
      try {
        const res = await api.get("/admin/requests");
        setRequests(res.data);
        setFilteredRequests(res.data);
      } catch (err) {
        console.error("Failed to fetch requests:", err);
        setError("Could not load requests.");
      } finally {
        setLoading(false);
      }
    };
    fetchRequests();
  }, []);

  // handle filter
  const handleSearch = () => {
    let results = requests;

    if (filterCategory !== "None") {
      results = results.filter(
        (r) => r.category.toLowerCase() === filterCategory.toLowerCase()
      );
    }
    if (filterStatus !== "None") {
      results = results.filter(
        (r) => r.status.toLowerCase() === filterStatus.toLowerCase()
      );
    }

    setFilteredRequests(results);
    setCurrentPage(1);
  };

  // pagination calculations
  const totalPages = Math.ceil(filteredRequests.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentItems = filteredRequests.slice(
    startIndex,
    startIndex + itemsPerPage
  );

  // delete request
  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this request?")) return;
    try {
      await api.delete(`/admin/request/${id}`);
      setRequests(requests.filter((r) => r.id !== id));
      setFilteredRequests(filteredRequests.filter((r) => r.id !== id));
    } catch (err) {
      console.error("Failed to delete request:", err);
      alert("Error deleting request.");
    }
  };

  // status badge helper
  const renderStatus = (status) => {
    const s = status?.toUpperCase();
    let color =
      s === "FULFILLED"
        ? "bg-green-100 text-green-700 border-green-300"
        : s === "PENDING"
        ? "bg-yellow-100 text-yellow-700 border-yellow-300"
        : s === "PARTIAL"
        ? "bg-amber-100 text-amber-700 border-amber-300"
        : "bg-gray-100 text-gray-700 border-gray-300";

    return (
      <span
        className={`px-3 py-1 rounded-full text-xs font-bold border ${color}`}
      >
        {s}
      </span>
    );
  };

  return (
    <div
      className="flex flex-col min-h-screen 
                 bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 
                 text-white animate-gradient-x"
    >
      {/* Navbar */}
      <AdminNavbar />

      {/* Page Content */}
      <main className="flex-1 px-6 py-8">
        <div className="bg-white/95 text-gray-900 rounded-2xl shadow-lg p-6 animate-fadeIn">
          {/* Header with filters + back button */}
          <div className="flex justify-between items-center mb-6">
            <h2
              className="text-3xl font-extrabold 
                         bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                         bg-clip-text text-transparent"
            >
              Manage Requests
            </h2>

            <div className="flex flex-wrap gap-4 items-end">
              {/* Category Filter */}
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Category
                </label>
                <select
                  value={filterCategory}
                  onChange={(e) => setFilterCategory(e.target.value)}
                  className="border p-2 rounded-lg text-gray-800"
                >
                  <option value="None">None</option>
                  <option value="Food">Food</option>
                  <option value="Water">Water</option>
                  <option value="Shelter">Shelter</option>
                  <option value="Medical">Medical</option>
                </select>
              </div>

              {/* Status Filter */}
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Status
                </label>
                <select
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                  className="border p-2 rounded-lg text-gray-800"
                >
                  <option value="None">None</option>
                  <option value="Pending">Pending</option>
                  <option value="Partial">Partial</option>
                  <option value="Fulfilled">Fulfilled</option>
                </select>
              </div>

              {/* Filter Button */}
              <button
                onClick={handleSearch}
                className="bg-teal-600 text-white px-4 py-2 rounded-lg hover:bg-teal-700 transition"
              >
                Done
              </button>

              {/* Go Back Button */}
              <button
                onClick={() => navigate("/admin/dashboard")}
                className="bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 transition"
              >
                ← Go Back
              </button>
            </div>
          </div>

          {/* Loading / Error */}
          {loading && <p className="text-gray-600">Loading requests...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {/* Empty State */}
          {!loading && !error && filteredRequests.length === 0 && (
            <p className="text-gray-600">No requests found.</p>
          )}

          {/* Table */}
          {!loading && !error && filteredRequests.length > 0 && (
            <>
              <div className="overflow-x-auto">
                <table className="w-full table-fixed border-collapse rounded-lg overflow-hidden shadow">
                  <thead>
                    <tr className="bg-teal-600 text-white text-left text-sm uppercase tracking-wider">
                      <th className="p-3 w-[10%]">ID</th>
                      <th className="p-3 w-[15%]">Category</th>
                      <th className="p-3 w-[15%]">Requested Qty</th>
                      <th className="p-3 w-[20%]">Status</th>
                      <th className="p-3 w-[25%]">Reporter</th>
                      <th className="p-3 w-[15%] text-center">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentItems.map((r) => (
                      <tr
                        key={r.id}
                        className="odd:bg-gray-50 even:bg-gray-100 hover:bg-teal-50 transition"
                      >
                        <td className="p-3 font-semibold text-gray-800">
                          {r.id}
                        </td>
                        <td className="p-3 text-gray-700">{r.category}</td>
                        <td className="p-3 text-gray-700">
                          {r.requestedQuantity}
                        </td>
                        <td className="p-3">{renderStatus(r.status)}</td>
                        <td className="p-3">
                          <span className="px-3 py-1 rounded-full text-xs font-medium border bg-blue-100 text-blue-700 border-blue-300">
                            {r.reporterEmail}
                          </span>
                        </td>
                        <td className="p-3 text-center">
                          <button
                            onClick={() => handleDelete(r.id)}
                            className="px-3 py-1 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                          >
                            🗑 Delete
                          </button>
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

export default ManageRequests;
