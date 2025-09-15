// src/pages/Admin/ManageContributions.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../utils/api";
import AdminNavbar from "../../components/Navbar/AdminNavbar";
import Footer from "../../components/Footer";

function ManageContributions() {
  const navigate = useNavigate();
  const [contributions, setContributions] = useState([]);
  const [filteredContributions, setFilteredContributions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // filters
  const [filterCategory, setFilterCategory] = useState("None");

  // pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    const fetchContributions = async () => {
      try {
        const res = await api.get("/admin/contributions"); // Admin endpoint
        setContributions(res.data);
        setFilteredContributions(res.data);
      } catch (err) {
        console.error("Failed to fetch contributions:", err);
        setError("Could not load contributions.");
      } finally {
        setLoading(false);
      }
    };
    fetchContributions();
  }, []);

  // handle filter
  const handleSearch = () => {
    let results = contributions;

    if (filterCategory !== "None") {
      results = results.filter(
        (c) => c.category.toLowerCase() === filterCategory.toLowerCase()
      );
    }

    setFilteredContributions(results);
    setCurrentPage(1); // reset pagination
  };

  // pagination calculations
  const totalPages = Math.ceil(filteredContributions.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentItems = filteredContributions.slice(
    startIndex,
    startIndex + itemsPerPage
  );

  // delete contribution
  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this contribution?")) return;
    try {
      await api.delete(`/admin/contributions/${id}`);
      setContributions(contributions.filter((c) => c.id !== id));
      setFilteredContributions(filteredContributions.filter((c) => c.id !== id));
    } catch (err) {
      console.error("Failed to delete contribution:", err);
      alert("Error deleting contribution.");
    }
  };

  return (
    <div
      className="flex flex-col min-h-screen 
                 bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 
                 text-white animate-gradient-x"
    >
      <AdminNavbar />

      <main className="flex-1 px-6 py-8">
        <div className="bg-white/95 text-gray-900 rounded-2xl shadow-lg p-6 animate-fadeIn">
          {/* Header with filters + back button */}
          <div className="flex justify-between items-center mb-6">
            <h2
              className="text-3xl font-extrabold 
                         bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                         bg-clip-text text-transparent"
            >
              Manage Contributions
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
          {loading && <p className="text-gray-600">Loading contributions...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {/* Empty State */}
          {!loading && !error && filteredContributions.length === 0 && (
            <p className="text-gray-600">No contributions found.</p>
          )}

          {/* Table */}
          {!loading && !error && filteredContributions.length > 0 && (
            <>
              <div className="overflow-x-auto">
                <table className="w-full table-fixed border-collapse rounded-lg overflow-hidden shadow">
                  <thead>
                    <tr className="bg-teal-600 text-white text-left text-sm uppercase tracking-wider">
                      <th className="p-3 w-[8%]">ID</th>
                      <th className="p-3 w-[12%]">Request ID</th>
                      <th className="p-3 w-[15%]">Category</th>
                      <th className="p-3 w-[15%]">Quantity</th>
                      <th className="p-3 w-[25%]">Responder</th>
                      <th className="p-3 w-[15%]">Updated At</th>
                      <th className="p-3 w-[10%] text-center">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentItems.map((c) => (
                      <tr
                        key={c.id}
                        className="odd:bg-gray-50 even:bg-gray-100 hover:bg-teal-50 transition"
                      >
                        <td className="p-3 font-semibold text-gray-800">{c.id}</td>
                        <td className="p-3 text-gray-700">{c.requestId}</td>
                        <td className="p-3 text-gray-700">{c.category}</td>
                        <td className="p-3 text-gray-700">{c.contributedQuantity}</td>
                        <td className="p-3">
                          <span className="px-3 py-1 rounded-full text-xs font-medium border bg-blue-100 text-blue-700 border-blue-300">
                            {c.responderEmail}
                          </span>
                        </td>
                        <td className="p-3 text-gray-600 text-sm">
                          {new Date(c.updatedAt).toLocaleString()}
                        </td>
                        <td className="p-3 text-center">
                          <button
                            onClick={() => handleDelete(c.id)}
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

      <Footer />
    </div>
  );
}

export default ManageContributions;
