// src/pages/Admin/AdminMapDashboard.jsx
import React, { useEffect, useState } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Tooltip,
  useMap,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";
import AdminNavbar from "../../components/Navbar/AdminNavbar";
import Footer from "../../components/Footer";
import {
  getDisasterIcon,
  reporterIcon,
  responderIcon,
  adminIcon,
} from "../../components/MapView/mapIcons.js";
import MapLegend from "../../components/MapView/MapLegend";

// --- Admin current location handler ---
function AdminLocation({ setAdminLocation }) {
  const map = useMap();
  useEffect(() => {
    map.locate().on("locationfound", (e) => {
      setAdminLocation(e.latlng);
      map.flyTo(e.latlng, 13);
    });
  }, [map, setAdminLocation]);
  return null;
}

// --- Format severity nicely ---
const formatSeverity = (severity) =>
  severity ? severity.charAt(0).toUpperCase() + severity.slice(1).toLowerCase() : "";

function AdminMapDashboard() {
  const navigate = useNavigate();
  const { token } = useAuth();

  const [disasters, setDisasters] = useState([]);
  const [requests, setRequests] = useState([]);
  const [contributions, setContributions] = useState([]);
  const [reporters, setReporters] = useState([]);
  const [responders, setResponders] = useState([]);
  const [adminLocation, setAdminLocation] = useState(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // === API Calls ===
  const fetchData = async () => {
    try {
      const [disRes, reqRes, userRes] = await Promise.all([
        axios.get("http://localhost:8080/api/disasters", {
          headers: { Authorization: `Bearer ${token}` },
        }),
        axios.get("http://localhost:8080/api/requests", {
          headers: { Authorization: `Bearer ${token}` },
        }),
        axios.get("http://localhost:8080/api/users", {
          headers: { Authorization: `Bearer ${token}` },
        }),
      ]);

      setDisasters(disRes.data);
      setRequests(reqRes.data);

      // Fetch all contributions per request
      let allContribs = [];
      for (let r of reqRes.data) {
        const cRes = await axios.get(
          `http://localhost:8080/api/contributions/request/${r.id}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        allContribs = [...allContribs, ...cRes.data];
      }
      setContributions(allContribs);

      // Split users
      const allUsers = userRes.data;
      setReporters(allUsers.filter((u) => u.role === "REPORTER"));
      setResponders(allUsers.filter((u) => u.role === "RESPONDER"));
    } catch (err) {
      console.error("Failed to fetch admin map data:", err);
      setError("Could not load map data.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (token) fetchData();
  }, [token]);

  // === Helpers ===
  const getRequestsForDisaster = (disasterId) =>
    requests.filter((r) => r.disasterId === disasterId);

  const getContributionsForDisaster = (disasterId) =>
    contributions.filter((c) => {
      const req = requests.find((r) => r.id === c.requestId);
      return req && req.disasterId === disasterId;
    });

  // === Emoji toggle like Reporter/Responder ===
  useEffect(() => {
    const handlers = [];
    disasters.forEach((d) => {
      const el = document.getElementById(`marker-${d.id}`);
      if (el) {
        const handler = () => {
          const collapsed = el.querySelector(".collapsed");
          const expanded = el.querySelector(".expanded");
          if (!expanded) return;

          const isShown = expanded.classList.contains("show");
          if (!isShown) {
            collapsed.style.opacity = "0";
            setTimeout(() => {
              collapsed.style.display = "none";
              expanded.classList.add("show");
              expanded.classList.remove("hidden");
            }, 200);
          } else {
            expanded.classList.remove("show");
            setTimeout(() => {
              expanded.classList.add("hidden");
              collapsed.style.display = "inline-block";
              collapsed.style.opacity = "1";
            }, 300);
          }
        };
        el.addEventListener("click", handler);
        handlers.push({ el, handler });
      }
    });
    return () => {
      handlers.forEach(({ el, handler }) =>
        el.removeEventListener("click", handler)
      );
    };
  }, [disasters, contributions]);

  return (
    <div className="flex flex-col min-h-screen bg-gradient-to-br from-cyan-400 via-blue-400 to-teal-500 text-white animate-gradient-x">
      <AdminNavbar />

      <main className="flex-1 px-6 py-8">
        <div className="bg-white/95 text-gray-900 rounded-2xl shadow-lg p-6 animate-fadeIn">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-3xl font-extrabold bg-gradient-to-r from-blue-700 via-cyan-600 to-teal-500 bg-clip-text text-transparent">
              Admin Map Dashboard
            </h2>
            <button
              onClick={() => navigate("/admin/dashboard")}
              className="bg-teal-600 text-white px-4 py-2 rounded-lg hover:bg-teal-700 transition"
            >
              ← Go Back
            </button>
          </div>

          {loading && <p className="text-gray-600">Loading map data...</p>}
          {error && <p className="text-red-600">{error}</p>}

          {!loading && !error && (
            <MapContainer
              center={[20.5937, 78.9629]}
              zoom={5}
              scrollWheelZoom
              className="w-full h-[70vh] rounded-lg shadow-lg"
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              <AdminLocation setAdminLocation={setAdminLocation} />

              {/* Admin location */}
              {adminLocation && (
                <Marker position={adminLocation} icon={adminIcon}>
                  <Tooltip direction="top">My Location (Admin)</Tooltip>
                </Marker>
              )}

              {/* Reporters */}
              {reporters.map(
                (r) =>
                  r.latitude &&
                  r.longitude && (
                    <Marker
                      key={r.id}
                      position={[r.latitude, r.longitude]}
                      icon={reporterIcon}
                    >
                      <Tooltip direction="top">{r.name} (Reporter)</Tooltip>
                    </Marker>
                  )
              )}

              {/* Responders */}
              {responders.map(
                (r) =>
                  r.latitude &&
                  r.longitude && (
                    <Marker
                      key={r.id}
                      position={[r.latitude, r.longitude]}
                      icon={responderIcon}
                    >
                      <Tooltip direction="top">{r.name} (Responder)</Tooltip>
                    </Marker>
                  )
              )}

              {/* Disasters with contributions */}
              {disasters.map((d) => {
                const disasterRequests = getRequestsForDisaster(d.id);
                const disasterContributions = getContributionsForDisaster(d.id);

                const status =
                  disasterRequests.every((r) => r.status === "FULFILLED")
                    ? "fulfilled"
                    : disasterRequests.some((r) => r.fulfilledQuantity > 0)
                    ? "partial"
                    : "reported";

                const contribCategories = disasterContributions.map((c) => c.category);

                return (
                  <Marker
                    key={d.id}
                    position={[d.latitude, d.longitude]}
                    icon={getDisasterIcon(status, contribCategories, d.id)}
                  >
                    <Popup minWidth={420} maxWidth={500}>
                      <div className="space-y-2 text-sm">
                        <h3 className="font-bold text-lg text-red-600">
                          {d.type} {d.severity && `(${formatSeverity(d.severity)} Alert)`}
                        </h3>
                        <p><strong>Description:</strong> {d.description}</p>
                        <p><strong>Reported By:</strong> {d.reporterEmail}</p>
                        {d.createdAt && (
                          <p className="text-gray-500 text-xs italic">
                            Reported On: {new Date(d.createdAt).toLocaleString()}
                          </p>
                        )}

                        <h4 className="mt-2 font-semibold">Requests</h4>
                        <ul className="list-disc pl-5">
                          {disasterRequests.map((r) => (
                            <li key={r.id}>
                              {r.category}: {r.fulfilledQuantity}/{r.requestedQuantity} ({r.status})
                            </li>
                          ))}
                        </ul>

                        <h4 className="mt-2 font-semibold">Contributions</h4>
                        <ul className="list-disc pl-5">
                          {disasterContributions.map((c) => (
                            <li key={c.id}>
                              {c.responderEmail}: {c.contributedQuantity} ({c.category})
                            </li>
                          ))}
                        </ul>
                      </div>
                    </Popup>
                  </Marker>
                );
              })}

              {/* Shared Map Legend */}
              <MapLegend role="admin" />
            </MapContainer>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
}

export default AdminMapDashboard;
