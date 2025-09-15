// src/components/MapView/ResponderMapView.jsx
import React, { useState, useEffect } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Tooltip,
  useMap,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";
import ModalWrapper from "./ModalWrapper.jsx";
import ContributionForm from "./ContributionForm";
import {
  getDisasterIcon,
  responderIcon,
  responderLocationIcon,
} from "./mapIcons.js";
import MapLegend from "./MapLegend";

// Map severity → gradient
const severityGradient = {
  HIGH: "from-red-600 to-purple-600",
  MEDIUM: "from-orange-500 to-yellow-500",
  LOW: "from-green-500 to-teal-500",
};

// Current location marker
function CurrentLocation({ setCurrentLocation }) {
  const map = useMap();
  useEffect(() => {
    map.locate().on("locationfound", (e) => {
      setCurrentLocation(e.latlng);
      map.flyTo(e.latlng, 13);
    });
  }, [map, setCurrentLocation]);
  return null;
}

function ResponderMapView() {
  const { token } = useAuth();

  const [disasters, setDisasters] = useState([]);
  const [requests, setRequests] = useState([]);
  const [contributions, setContributions] = useState([]);
  const [currentLocation, setCurrentLocation] = useState(null);
  const [contributionModal, setContributionModal] = useState(null);

  const [showAllRequests, setShowAllRequests] = useState({});
  const [showAllContribs, setShowAllContribs] = useState({});

  // === API Calls ===
  const fetchDisasters = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/disasters", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setDisasters(res.data);
    } catch (err) {
      console.error("Failed to fetch disasters:", err);
    }
  };

  const fetchRequests = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/requests", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setRequests(res.data);
    } catch (err) {
      console.error("Failed to fetch requests:", err);
    }
  };

  const fetchContributionsForRequest = async (requestId) => {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/contributions/request/${requestId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      return res.data;
    } catch (err) {
      console.error(`Failed to fetch contributions for request ${requestId}:`, err);
      return [];
    }
  };

  const fetchAllContributions = async () => {
    let all = [];
    for (let req of requests) {
      const contribs = await fetchContributionsForRequest(req.id);
      all = [...all, ...contribs];
    }
    setContributions(all);
  };

  useEffect(() => {
    if (token) {
      fetchDisasters();
      fetchRequests();
    }
  }, [token]);

  useEffect(() => {
    if (requests.length > 0) {
      fetchAllContributions();
    }
  }, [requests]);

  // === Helpers ===
  const getRequestsForDisaster = (disasterId) =>
    requests.filter((r) => r.disasterId === disasterId);

  const getContributionsForDisaster = (disasterId) =>
    contributions.filter((c) => {
      const req = requests.find((r) => r.id === c.requestId);
      return req && req.disasterId === disasterId;
    });

  // === Group contributions (responder+location) ===
  const groupedContributions = Object.values(
    contributions.reduce((acc, c) => {
      const key = `${c.responderEmail}-${c.latitude}-${c.longitude}`;
      if (!acc[key]) {
        acc[key] = {
          responderEmail: c.responderEmail,
          latitude: c.latitude,
          longitude: c.longitude,
          categories: [c.category],
          totalQuantity: c.contributedQuantity,
          updatedAt: c.updatedAt,
        };
      } else {
        acc[key].categories.push(c.category);
        acc[key].totalQuantity += c.contributedQuantity;
        acc[key].updatedAt =
          new Date(c.updatedAt) > new Date(acc[key].updatedAt)
            ? c.updatedAt
            : acc[key].updatedAt;
      }
      return acc;
    }, {})
  );

  return (
    <div className="relative w-full h-full">
      <MapContainer
        center={[42.34, -71.0895]}
        zoom={14}
        scrollWheelZoom
        className="w-full h-full"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <CurrentLocation setCurrentLocation={setCurrentLocation} />

        {/* My current location */}
        {currentLocation && (
          <Marker
            position={currentLocation}
            icon={responderLocationIcon}
            zIndexOffset={1000}
          >
            <Tooltip direction="top">📍 My Location (Responder – Blue)</Tooltip>
            <Popup>You are here (Responder)</Popup>
          </Marker>
        )}

        {/* Grouped contribution pins */}
        {groupedContributions.map(
          (c, idx) =>
            c.latitude &&
            c.longitude && (
              <Marker
                key={`contrib-${idx}`}
                position={[c.latitude, c.longitude]}
                icon={responderIcon}
              >
                <Tooltip direction="top">🤝 Contribution Location</Tooltip>
                <Popup>
                  <div className="p-2 space-y-1">
                    <h4 className="font-bold text-blue-600">
                      Responder Contribution
                    </h4>
                    <p className="text-sm">
                      <strong>Contributor:</strong> {c.responderEmail}
                    </p>
                    <p className="text-sm">
                      <strong>Categories:</strong> {c.categories.join(", ")}
                    </p>
                    <p className="text-sm">
                      <strong>Total Quantity:</strong> {c.totalQuantity}
                    </p>
                    {c.updatedAt && (
                      <p className="text-xs text-gray-500">
                        {new Date(c.updatedAt).toLocaleString()}
                      </p>
                    )}
                  </div>
                </Popup>
              </Marker>
            )
        )}

        {/* Disaster pins */}
        {disasters.map((d) => {
          const disasterRequests = getRequestsForDisaster(d.id);
          const disasterContributions = getContributionsForDisaster(d.id);

          const status =
            disasterRequests.length > 0
              ? disasterRequests.every((r) => r.status === "FULFILLED")
                ? "fulfilled"
                : disasterRequests.some((r) => r.fulfilledQuantity > 0)
                ? "partial"
                : "reported"
              : "reported";

          const visibleRequests = showAllRequests[d.id]
            ? disasterRequests
            : disasterRequests.slice(0, 3);
          const visibleContribs = showAllContribs[d.id]
            ? disasterContributions
            : disasterContributions.slice(0, 3);

          return (
            <Marker
              key={d.id}
              position={[d.latitude, d.longitude]}
              icon={getDisasterIcon(status)}
            >
              <Tooltip direction="top">
                {status === "reported" && "⚠️ Reported Disaster"}
                {status === "partial" && "🟠 Partial Fulfillment"}
                {status === "fulfilled" && "✅ Fulfilled Disaster"}
              </Tooltip>
              <Popup minWidth={420} maxWidth={500}>
                <div className="space-y-4 text-sm">
                  {/* Gradient Header */}
                  <div
                    className={`bg-gradient-to-r ${
                      severityGradient[d.severity?.toUpperCase()] ||
                      "from-gray-500 to-blue-600"
                    } text-white px-4 py-2 rounded-lg shadow`}
                  >
                    <h3 className="font-bold text-base break-words">
                      {d.type} ({d.severity?.toUpperCase()})
                    </h3>
                  </div>

                  {/* Details */}
                  <p>
                    <strong>Description:</strong> {d.description}
                  </p>
                  <p>
                    <strong>Reported By:</strong> {d.reporterEmail}
                  </p>
                  {d.createdAt && (
                    <p className="text-gray-500 text-xs italic">
                      Reported On: {new Date(d.createdAt).toLocaleString()}
                    </p>
                  )}

                  {/* Requests */}
                  <div>
                    <h4 className="font-semibold">Requests</h4>
                    {disasterRequests.length > 0 ? (
                      <>
                        <ul className="space-y-2 mt-2">
                          {visibleRequests.map((r) => (
                            <li
                              key={r.id}
                              className="flex justify-between items-center px-3 py-2 bg-gray-50 rounded-lg border shadow-sm"
                            >
                              <span className="font-medium capitalize">
                                {r.category} – {r.fulfilledQuantity}/
                                {r.requestedQuantity}
                              </span>
                              <span
                                className={`ml-2 px-2 py-0.5 rounded-full text-xs font-bold border ${
                                  r.status === "FULFILLED"
                                    ? "bg-green-100 text-green-700 border-green-300"
                                    : r.status === "PARTIAL"
                                    ? "bg-yellow-100 text-yellow-700 border-yellow-300"
                                    : "bg-red-100 text-red-700 border-red-300"
                                }`}
                              >
                                {r.status}
                              </span>
                              {r.status !== "FULFILLED" && (
                                <button
                                  onClick={() =>
                                    setContributionModal({
                                      requestId: r.id,
                                      requestCategory: r.category,
                                    })
                                  }
                                  className="ml-3 px-3 py-1 text-xs font-semibold bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                                >
                                  Contribute
                                </button>
                              )}
                            </li>
                          ))}
                        </ul>
                        {disasterRequests.length > 3 && (
                          <div className="text-right mt-1">
                            <button
                              onClick={() =>
                                setShowAllRequests((prev) => ({
                                  ...prev,
                                  [d.id]: !prev[d.id],
                                }))
                              }
                              className="text-blue-600 text-xs hover:underline"
                            >
                              {showAllRequests[d.id]
                                ? "Show Less"
                                : `${disasterRequests.length - 3} more +`}
                            </button>
                          </div>
                        )}
                      </>
                    ) : (
                      <p className="text-xs text-gray-500 italic mt-1">None</p>
                    )}
                  </div>

                  {/* Contributions */}
                  <div>
                    <h4 className="font-semibold">Contributions</h4>
                    {disasterContributions.length > 0 ? (
                      <>
                        <ul className="space-y-2 mt-2">
                          {visibleContribs.map((c) => (
                            <li
                              key={c.id}
                              className="px-3 py-2 bg-gray-50 rounded-lg border text-xs flex justify-between items-center shadow-sm"
                            >
                              <span className="font-medium break-words">
                                {c.responderEmail}
                              </span>
                              <span className="text-gray-700">
                                {c.contributedQuantity} ({c.category})
                              </span>
                            </li>
                          ))}
                        </ul>
                        {disasterContributions.length > 3 && (
                          <div className="text-right mt-1">
                            <button
                              onClick={() =>
                                setShowAllContribs((prev) => ({
                                  ...prev,
                                  [d.id]: !prev[d.id],
                                }))
                              }
                              className="text-blue-600 text-xs hover:underline"
                            >
                              {showAllContribs[d.id]
                                ? "Show Less"
                                : `${disasterContributions.length - 3} more +`}
                            </button>
                          </div>
                        )}
                      </>
                    ) : (
                      <p className="text-xs text-gray-500 italic mt-1">None</p>
                    )}
                  </div>
                </div>
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>

      <MapLegend role="responder" />

      {/* Contribution Modal */}
      <ModalWrapper
        isOpen={!!contributionModal}
        onClose={() => setContributionModal(null)}
      >
        <ContributionForm
          requestId={contributionModal?.requestId}
          requestCategory={contributionModal?.requestCategory}
          onSuccess={() => {
            setContributionModal(null);
            fetchAllContributions();
            fetchDisasters();
            fetchRequests();
          }}
          onClose={() => setContributionModal(null)}
        />
      </ModalWrapper>
    </div>
  );
}

export default ResponderMapView;
