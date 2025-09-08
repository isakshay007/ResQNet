import React, { useEffect, useState } from "react";
import api from "../utils/api"; // axios wrapper
import Navbar from "../components/Navbar/Navbar";
import Footer from "../components/Footer";

function MyDisasters() {
  const [disasters, setDisasters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchDisasters = async () => {
      try {
        const res = await api.get("/disasters"); // GET /api/disasters
        setDisasters(res.data);
      } catch (err) {
        console.error("Failed to fetch disasters:", err);
        setError("Could not load disasters. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    fetchDisasters();
  }, []);

  return (
    <div
      className="flex flex-col min-h-screen 
                 bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 
                 text-white animate-gradient-x"
    >
      {/* Navbar */}
      <Navbar active="my-disasters" />

      {/* Page Content */}
      <main className="flex-1 px-6 py-8">
        <div className="bg-white/95 text-gray-900 rounded-2xl shadow-lg p-6 animate-fadeIn">
          {/* Heading with gradient teal text */}
          <h2
            className="text-3xl font-extrabold mb-6 
                       bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                       bg-clip-text text-transparent"
          >
            My Disasters
          </h2>

          {loading && <p className="text-gray-600">Loading...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {!loading && !error && disasters.length === 0 && (
            <p className="text-gray-600">No disasters reported yet.</p>
          )}

          {!loading && !error && disasters.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse rounded-lg overflow-hidden shadow-md">
                <thead>
                  <tr className="bg-teal-600 text-white text-left">
                    <th className="p-3">Type</th>
                    <th className="p-3">Severity</th>
                    <th className="p-3">Description</th>
                    <th className="p-3">Location</th>
                    <th className="p-3">Reported By</th>
                  </tr>
                </thead>
                <tbody>
                  {disasters.map((d) => (
                    <tr
                      key={d.id}
                      className="odd:bg-gray-50 even:bg-gray-100 hover:bg-teal-50 transition"
                    >
                      <td className="p-3 font-semibold text-gray-800">
                        {d.type}
                      </td>
                      <td
                        className={`p-3 font-bold ${
                          d.severity === "HIGH"
                            ? "text-red-600"
                            : d.severity === "MEDIUM"
                            ? "text-yellow-600"
                            : "text-green-600"
                        }`}
                      >
                        {d.severity}
                      </td>
                      <td className="p-3 text-gray-700">{d.description}</td>
                      <td className="p-3 text-gray-600 text-sm">
                        {d.latitude?.toFixed(4)}, {d.longitude?.toFixed(4)}
                      </td>
                      <td className="p-3 text-gray-800">{d.reporterEmail}</td>
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

export default MyDisasters;
