// src/components/MapView/ResponderMapView.jsx
import React, { useState, useEffect } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  useMap,
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";
import ModalWrapper from "./ModalWrapper";
import ContributionForm from "./ContributionForm";

//  Emoji map for request categories
const emojiMap = {
  food: "🍞",
  water: "💧",
  medical: "🏥",
  shelter: "⛺",
};

//  Disaster icon (changes with fulfillment + emoji overlays)
function getDisasterIcon(status, contributions = []) {
  let colorUrl = "https://maps.google.com/mapfiles/ms/icons/red-dot.png"; // default 🔴

  if (status === "partial") {
    colorUrl = "https://maps.google.com/mapfiles/ms/icons/orange-dot.png"; // 🟠
  } else if (status === "fulfilled") {
    colorUrl = "https://maps.google.com/mapfiles/ms/icons/green-dot.png"; // 🟢
  }

  // Emojis disappear when fulfilled
  const uniqueContributions =
    status === "fulfilled" ? [] : [...new Set(contributions.map((c) => c.toLowerCase()))];

  // Stack emojis vertically under the pin
  const displayEmojis = uniqueContributions
    .map((c) => `<div>${emojiMap[c] || ""}</div>`)
    .join("");

  return L.divIcon({
    className: "custom-disaster-icon",
    html: `
      <div style="display:flex;flex-direction:column;align-items:center;">
        <img src="${colorUrl}" style="width:32px;height:32px;" />
        <div style="font-size:14px; margin-top:2px;">${displayEmojis}</div>
      </div>
    `,
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });
}

//  Responder icon (blue, current user)
const responderIcon = L.icon({
  iconUrl: "https://maps.google.com/mapfiles/ms/icons/blue-dot.png",
  iconSize: [32, 32],
  iconAnchor: [16, 32],
});

//  Current location marker (responder only)
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
      console.error(" Failed to fetch requests:", err);
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
      console.error(` Failed to fetch contributions for request ${requestId}:`, err);
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

  return (
    <div className="relative w-full h-full">
      <MapContainer
        center={[20.5937, 78.9629]}
        zoom={5}
        scrollWheelZoom
        className="w-full h-full"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <CurrentLocation setCurrentLocation={setCurrentLocation} />

        {/*  Show my current location */}
        {currentLocation && (
          <Marker position={currentLocation} icon={responderIcon}>
            <Popup>You are here (Responder)</Popup>
          </Marker>
        )}

        {/*  Disasters */}
        {disasters.map((d) => {
          const disasterRequests = getRequestsForDisaster(d.id);
          const disasterContributions = getContributionsForDisaster(d.id);

          const status =
            disasterRequests.every((r) => r.status === "FULFILLED")
              ? "fulfilled"
              : disasterRequests.some((r) => r.fulfilledQuantity > 0)
              ? "partial"
              : "reported";

          // ✅ use contributions, not requests
          const contributionsSummary = disasterContributions.map((c) => c.category);

          return (
            <Marker
              key={d.id}
              position={[d.latitude, d.longitude]}
              icon={getDisasterIcon(status, contributionsSummary)}
            >
              <Popup minWidth={420} maxWidth={500}>
                <div className="space-y-2 text-sm">
                  <h3 className="font-bold text-lg text-red-600">
                    {d.type} {d.severity && `(${d.severity})`}
                  </h3>
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

                  <h4 className="mt-2 font-semibold">Requests</h4>
                  <ul className="list-disc pl-5">
                    {disasterRequests.map((r) => (
                      <li key={r.id}>
                        {r.category} – {r.fulfilledQuantity}/{r.requestedQuantity} ({r.status})
                        {status !== "fulfilled" && (
                          <button
                            onClick={() => setContributionModal({ requestId: r.id })}
                            className="ml-2 px-2 py-1 bg-blue-500 text-white text-xs rounded hover:bg-blue-600"
                          >
                            Contribute
                          </button>
                        )}
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
      </MapContainer>

      {/*  Legend */}
      <div className="absolute bottom-16 right-4 bg-white/90 backdrop-blur-sm 
                      border border-gray-300 rounded-md shadow-sm 
                      p-3 text-xs z-[1000] w-64">
        <h4 className="font-semibold text-gray-600 text-center mb-2">Map Legend</h4>
        <div className="space-y-2">
          <div className="flex items-center space-x-2">
            <img src="https://maps.google.com/mapfiles/ms/icons/red-dot.png" className="w-3.5 h-3.5" />
            <span>Disaster 🔴</span>
          </div>
          <div className="flex items-center space-x-2">
            <img src="https://maps.google.com/mapfiles/ms/icons/orange-dot.png" className="w-3.5 h-3.5" />
            <span>Partial 🟠</span>
          </div>
          <div className="flex items-center space-x-2">
            <img src="https://maps.google.com/mapfiles/ms/icons/green-dot.png" className="w-3.5 h-3.5" />
            <span>Fulfilled 🟢</span>
          </div>
          <div className="flex items-center space-x-2">
            <span className="w-3.5 h-3.5 rounded-full bg-blue-500 inline-block"></span>
            <span>My Location (Responder)</span>
          </div>
          <div className="flex items-center space-x-2">
            <span>🍞💧🏥⛺</span>
            <span>Contributions</span>
          </div>
        </div>
      </div>

      {/*  Contribution modal */}
      <ModalWrapper isOpen={!!contributionModal} onClose={() => setContributionModal(null)}>
        <ContributionForm
          requestId={contributionModal?.requestId}
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
