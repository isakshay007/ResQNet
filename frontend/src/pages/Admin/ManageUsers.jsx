// src/pages/Admin/ManageUsers.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../utils/api";
import AdminNavbar from "../../components/Navbar/AdminNavbar";
import Footer from "../../components/Footer";

function ManageUsers() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // filters
  const [roleFilter, setRoleFilter] = useState("None");

  // pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const res = await api.get("/admin/users");
        setUsers(res.data);
        setFilteredUsers(res.data);
      } catch (err) {
        console.error("Failed to fetch users:", err);
        setError("Could not load users. Please try again later.");
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
  }, []);

  // role badge helper
  const renderRole = (role) => {
    const r = role?.toUpperCase();
    let color =
      r === "ADMIN"
        ? "bg-red-100 text-red-700 border-red-300"
        : r === "RESPONDER" || r === "REPORTER"
        ? "bg-green-100 text-green-700 border-green-300"
        : "bg-gray-100 text-gray-700 border-gray-300";

    return (
      <span
        className={`px-3 py-1 rounded-full text-xs font-bold border ${color}`}
      >
        {r}
      </span>
    );
  };

  // handle filter
  const handleSearch = () => {
    let results = users;

    if (roleFilter !== "None") {
      results = results.filter(
        (u) => u.role.toLowerCase() === roleFilter.toLowerCase()
      );
    }

    setFilteredUsers(results);
    setCurrentPage(1); // reset pagination
  };

  // pagination calculations
  const totalPages = Math.ceil(filteredUsers.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentItems = filteredUsers.slice(
    startIndex,
    startIndex + itemsPerPage
  );

  // delete user
  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this user?")) return;
    try {
      await api.delete(`/admin/users/${id}`);
      setUsers(users.filter((u) => u.id !== id));
      setFilteredUsers(filteredUsers.filter((u) => u.id !== id));
    } catch (err) {
      console.error("Failed to delete user:", err);
      alert("Error deleting user.");
    }
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
              Manage Users
            </h2>

            <div className="flex flex-wrap gap-4 items-end">
              {/* Role Filter */}
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Role
                </label>
                <select
                  value={roleFilter}
                  onChange={(e) => setRoleFilter(e.target.value)}
                  className="border p-2 rounded-lg text-gray-800"
                >
                  <option value="None">None</option>
                  <option value="admin">Admin</option>
                  <option value="responder">Responder</option>
                  <option value="reporter">Reporter</option>
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
          {loading && <p className="text-gray-600">Loading users...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {/* Empty State */}
          {!loading && !error && filteredUsers.length === 0 && (
            <p className="text-gray-600">No users found.</p>
          )}

          {/* Table */}
          {!loading && !error && filteredUsers.length > 0 && (
            <>
              <div className="overflow-x-auto">
                <table className="w-full table-fixed border-collapse rounded-lg overflow-hidden shadow">
                  <thead>
                    <tr className="bg-teal-600 text-white text-left text-sm uppercase tracking-wider">
                      <th className="p-3 w-[10%]">ID</th>
                      <th className="p-3 w-[20%]">Name</th>
                      <th className="p-3 w-[25%]">Email</th>
                      <th className="p-3 w-[20%]">Role</th>
                      <th className="p-3 w-[15%] text-center">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentItems.map((u) => (
                      <tr
                        key={u.id}
                        className="odd:bg-gray-50 even:bg-gray-100 hover:bg-teal-50 transition"
                      >
                        <td className="p-3 font-semibold text-gray-800">
                          {u.id}
                        </td>
                        <td className="p-3 text-gray-700">{u.name}</td>
                        <td className="p-3">
                          <span className="px-3 py-1 rounded-full text-xs font-medium border bg-blue-100 text-blue-700 border-blue-300">
                            {u.email}
                          </span>
                        </td>
                        <td className="p-3">{renderRole(u.role)}</td>
                        <td className="p-3 text-center">
                          <button
                            onClick={() => handleDelete(u.id)}
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

export default ManageUsers;
