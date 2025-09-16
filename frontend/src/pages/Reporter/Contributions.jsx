// src/pages/Contributions.jsx
import React, { useEffect, useState } from "react";
import api from "../../utils/api"; // axios wrapper
import Navbar from "../../components/Navbar/Navbar";
import Footer from "../../components/Footer";

// Category → Icon map
const categoryIcons = {
  food: "🍞",
  water: "💧",
  shelter: "🏠",
  medical: "🏥",
};

// Utility: Capitalize first letter
const formatCategory = (category = "") => {
  if (!category) return "";
  const lower = category.toLowerCase();
  return lower.charAt(0).toUpperCase() + lower.slice(1);
};

function Contributions() {
  const [myContributions, setMyContributions] = useState([]);
  const [filteredContributions, setFilteredContributions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // filter
  const [categoryFilter, setCategoryFilter] = useState("None");

  // pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    const fetchMyContributions = async () => {
      try {
        // ✅ Fetch requests created by me
        const myReqRes = await api.get("/requests/my");
        const myReqs = myReqRes.data;

        let contribs = [];
        for (let req of myReqs) {
          const contribRes = await api.get(`/contributions/request/${req.id}`);
          contribs = [...contribs, ...contribRes.data];
        }

        setMyContributions(contribs);
        setFilteredContributions(contribs);
      } catch (err) {
        console.error("Failed to fetch contributions:", err);
        setError("Could not load contributions. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    fetchMyContributions();
  }, []);

  // handle filter
  const handleSearch = () => {
    let base = [...myContributions];

    if (categoryFilter !== "None") {
      base = base.filter(
        (c) => c.category?.toLowerCase() === categoryFilter.toLowerCase()
      );
    }

    setFilteredContributions(base);
    setCurrentPage(1); // reset pagination on filter
  };

  // pagination calculations
  const totalPages = Math.ceil(filteredContributions.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentItems = filteredContributions.slice(
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
      <Navbar active="contributions" />

      <main className="flex-1 px-6 py-8">
        <div className="bg-white/95 text-gray-900 rounded-2xl shadow-lg p-6 animate-fadeIn">
          {/* Header */}
          <div className="flex justify-between items-center mb-6">
            <h2
              className="text-3xl font-extrabold 
                         bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                         bg-clip-text text-transparent"
            >
              Contributions to My Requests
            </h2>

            {/* Filter Section */}
            <div className="flex gap-4 items-end">
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

              <button
                onClick={handleSearch}
                className="bg-teal-600 text-white px-4 py-2 rounded-lg hover:bg-teal-700 transition"
              >
                Apply
              </button>
            </div>
          </div>

          {loading && <p className="text-gray-600">Loading...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {!loading && !error && filteredContributions.length === 0 && (
            <p className="text-gray-600">
              No contributions to your requests yet.
            </p>
          )}

          {!loading && !error && filteredContributions.length > 0 && (
            <>
              <div className="overflow-x-auto">
                <table className="w-full border-collapse rounded-lg overflow-hidden shadow-md">
                  <thead>
                    <tr className="bg-teal-600 text-white text-left text-sm uppercase tracking-wide">
                      <th className="p-3">Request ID</th>
                      <th className="p-3">Category</th>
                      <th className="p-3">Contributed Qty</th>
                      <th className="p-3">Responder</th>
                      <th className="p-3">Location</th>
                      <th className="p-3">Updated At</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentItems.map((c) => {
                      const normalized = c.category?.toLowerCase();
                      return (
                        <tr
                          key={c.id}
                          className="odd:bg-gray-50 even:bg-gray-100 hover:bg-teal-50 transition"
                        >
                          <td className="p-3 font-semibold text-gray-800">
                            {c.requestId}
                          </td>
                          <td className="p-3 text-gray-800 font-medium flex items-center gap-2">
                            <span>{categoryIcons[normalized] || "📦"}</span>
                            <span>{formatCategory(c.category)}</span>
                          </td>
                          <td className="p-3 text-gray-700">
                            {c.contributedQuantity}
                          </td>
                          <td className="p-3">
                            <span className="px-2 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold">
                              {c.responderEmail}
                            </span>
                          </td>
                          <td className="p-3 text-gray-600 text-sm">
                            {c.latitude?.toFixed(4)}, {c.longitude?.toFixed(4)}
                          </td>
                          <td className="p-3 text-gray-600 text-sm">
                            {new Date(c.updatedAt).toLocaleString()}
                          </td>
                        </tr>
                      );
                    })}
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

export default Contributions;
