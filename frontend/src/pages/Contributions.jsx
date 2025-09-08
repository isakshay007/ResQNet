import React, { useEffect, useState } from "react";
import api from "../utils/api";
import Navbar from "../components/Navbar/Navbar";
import Footer from "../components/Footer";

function Contributions() {
  const [contributions, setContributions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchReporterContributions = async () => {
      try {
        // Step 1: get all my requests
        const requestsRes = await api.get("/requests/my");
        const requests = requestsRes.data;

        // Step 2: for each request, get contributions
        const allContributions = [];
        for (let req of requests) {
          const contribRes = await api.get(`/contributions/request/${req.id}`);
          contribRes.data.forEach((c) =>
            allContributions.push({ ...c, requestCategory: req.category })
          );
        }

        setContributions(allContributions);
      } catch (err) {
        console.error("Failed to fetch contributions:", err);
        setError("Could not load contributions. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    fetchReporterContributions();
  }, []);

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
          {/* Heading */}
          <h2
            className="text-3xl font-extrabold mb-6 
                       bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                       bg-clip-text text-transparent"
          >
            Contributions to My Requests
          </h2>

          {loading && <p className="text-gray-600">Loading...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {!loading && !error && contributions.length === 0 && (
            <p className="text-gray-600">No contributions received yet.</p>
          )}

          {!loading && !error && contributions.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse rounded-lg overflow-hidden shadow-md">
                <thead>
                  <tr className="bg-teal-600 text-white text-left">
                    <th className="p-3">Request ID</th>
                    <th className="p-3">Category</th>
                    <th className="p-3">Contributed Qty</th>
                    <th className="p-3">Responder</th>
                    <th className="p-3">Location</th>
                    <th className="p-3">Updated At</th>
                  </tr>
                </thead>
                <tbody>
                  {contributions.map((c) => (
                    <tr
                      key={c.id}
                      className="odd:bg-gray-50 even:bg-gray-100 hover:bg-teal-50 transition"
                    >
                      <td className="p-3 font-semibold text-gray-800">{c.requestId}</td>
                      <td className="p-3 text-gray-700">{c.requestCategory}</td>
                      <td className="p-3 text-gray-700">{c.contributedQuantity}</td>
                      <td className="p-3 text-gray-700">{c.responderEmail}</td>
                      <td className="p-3 text-gray-600 text-sm">
                        {c.latitude?.toFixed(4)}, {c.longitude?.toFixed(4)}
                      </td>
                      <td className="p-3 text-gray-600 text-sm">
                        {new Date(c.updatedAt).toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
}

export default Contributions;
