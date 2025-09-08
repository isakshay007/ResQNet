// src/components/MapView/ReporterMapView.jsx
import React, { useState, useEffect } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Tooltip,
  useMap,
  useMapEvents,
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import ReportDisasterForm from "./ReportDisasterForm";
import ResourceRequestForm from "./ResourceRequestForm";
import ModalWrapper from "./ModalWrapper";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";

//  Emoji map for contributions
const emojiMap = {
  food: "🍞",
  water: "💧",
  medical: "🏥",
  shelter: "⛺",
};

// Disaster icon generator with emoji overlays
function getDisasterIcon(status, contributions = []) {
  let colorUrl = "https://maps.google.com/mapfiles/ms/icons/red-dot.png"; // default 🔴

  if (status === "partial") {
    colorUrl = "https://maps.google.com/mapfiles/ms/icons/orange-dot.png"; // 🟠
  } else if (status === "fulfilled") {
    colorUrl = "https://maps.google.com/mapfiles/ms/icons/green-dot.png"; // 🟢
  }

  const uniqueContributions =
    status === "fulfilled" ? [] : [...new Set(contributions.map((c) => c.toLowerCase()))];

  // stack emojis vertically
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

// Reporter location icon (purple)
const reporterIcon = L.icon({
  iconUrl: "https://maps.google.com/mapfiles/ms/icons/purple-dot.png",
  iconSize: [32, 32],
  iconAnchor: [16, 32],
});

// Responder contribution icon (blue)
const responderIcon = L.icon({
  iconUrl: "https://maps.google.com/mapfiles/ms/icons/blue-dot.png",
  iconSize: [32, 32],
  iconAnchor: [16, 32],
});

// Handles map clicks (only for reporters)
function ClickHandler({ onMapClick }) {
  useMapEvents({
    click(e) {
      onMapClick({ formType: "disaster", position: e.latlng });
    },
  });
  return null;
}

// Current location handler
function CurrentLocation({ setCurrentLocation }) {
  const map = useMap();
  const [firstZoom, setFirstZoom] = useState(true);

  useEffect(() => {
    map.locate().on("locationfound", (e) => {
      setCurrentLocation(e.latlng);
      if (firstZoom) {
        map.flyTo(e.latlng, 13);
        setFirstZoom(false);
      }
    });
  }, [map, setCurrentLocation, firstZoom]);

  return null;
}

function ReporterMapView() {
  const { token } = useAuth();

  const [selectedPosition, setSelectedPosition] = useState(null);
  const [disasters, setDisasters] = useState([]);
  const [currentLocation, setCurrentLocation] = useState(null);
  const [requests, setRequests] = useState([]);
  const [contributions, setContributions] = useState([]);
  const [resourceModal, setResourceModal] = useState(null);

  const formatSeverity = (severity) =>
    severity ? severity.charAt(0).toUpperCase() + severity.slice(1).toLowerCase() : "";

  // === API Calls ===
  const fetchDisasters = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/disasters", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setDisasters(res.data);
    } catch (err) {
      console.error(" Failed to fetch disasters:", err);
    }
  };

  const fetchRequests = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/requests/my", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setRequests(res.data);

      // fetch contributions for each request
      const contribs = [];
      for (let r of res.data) {
        const cRes = await axios.get(
          `http://localhost:8080/api/contributions/request/${r.id}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );
        cRes.data.forEach((c) => contribs.push(c));
      }
      setContributions(contribs);
    } catch (err) {
      console.error(" Failed to fetch requests/contributions:", err);
    }
  };

  useEffect(() => {
    if (token) {
      fetchDisasters();
      fetchRequests();
    }
  }, [token]);

  const handleDisasterSuccess = (newDisaster) => {
    setDisasters((prev) => [...prev, newDisaster]);
    setSelectedPosition(null);
  };

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
        <ClickHandler onMapClick={setSelectedPosition} />

        {/* Reporter location pin */}
        {currentLocation && (
          <Marker position={currentLocation} icon={reporterIcon}>
            <Tooltip direction="top">My Location (Reporter)</Tooltip>
          </Marker>
        )}

        {/* Contributor locations (blue pins) */}
        {contributions.map(
          (c) =>
            c.latitude &&
            c.longitude && (
              <Marker
                key={`contrib-${c.id}`}
                position={[c.latitude, c.longitude]}
                icon={responderIcon}
              >
                <Popup>
                  <div>
                    <h4 className="font-bold">Responder Contribution</h4>
                    <p>
                      <strong>Contributor:</strong> {c.responderEmail}
                    </p>
                    <p>
                      <strong>Quantity:</strong> {c.contributedQuantity} ({c.category})
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

        {/* Disasters with emojis */}
        {disasters.map((d) => {
          const myRequests = requests.filter((r) => r.disasterId === d.id);
          const disasterContributions = contributions.filter((c) =>
            myRequests.some((r) => r.id === c.requestId)
          );

          const status =
            myRequests.every((r) => r.status === "FULFILLED")
              ? "fulfilled"
              : myRequests.some((r) => r.fulfilledQuantity > 0)
              ? "partial"
              : "reported";

          // use contributions for emojis
          const contribCategories = disasterContributions.map((c) => c.category);

          return (
            <Marker
              key={d.id}
              position={[d.latitude, d.longitude]}
              icon={getDisasterIcon(status, contribCategories)}
            >
              <Popup minWidth={420} maxWidth={500}>
                <div className="space-y-2 text-sm">
                  <h3 className="font-bold text-lg text-red-600">
                    {d.type} {d.severity && `(${formatSeverity(d.severity)} Alert)`}
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
                    {myRequests.map((r) => (
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

                  {/* Reporter can add a request */}
                  <button
                    onClick={() => setResourceModal({ id: d.id })}
                    className="px-3 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                  >
                    Request Resources
                  </button>
                </div>
              </Popup>
            </Marker>
          );
        })}

        {/* Reporter creating a new disaster */}
        {selectedPosition && selectedPosition.formType === "disaster" && (
          <Marker
            position={selectedPosition.position}
            icon={getDisasterIcon("reported")}
          >
            <Popup minWidth={420} maxWidth={500}>
              <ReportDisasterForm
                position={selectedPosition.position}
                onSuccess={handleDisasterSuccess}
                onClose={() => setSelectedPosition(null)}
              />
            </Popup>
          </Marker>
        )}
      </MapContainer>

      {/* Legend Box */}
      <div className="absolute bottom-16 right-4 bg-white/90 backdrop-blur-sm 
                      border border-gray-300 rounded-md shadow-sm 
                      p-3 text-xs z-[1000] w-60">
        <h4 className="font-semibold text-gray-600 text-center mb-2">Map Legend</h4>
        <div className="space-y-2">
          <div className="flex items-center space-x-2">
            <span className="w-3.5 h-3.5 rounded-full bg-purple-600 inline-block"></span>
            <span>My Location (Reporter)</span>
          </div>
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
            <span>Responder Contributions</span>
          </div>
          <div className="flex items-center space-x-2">
            <span>🍞💧🏥⛺</span>
            <span>Contribution Categories</span>
          </div>
        </div>
      </div>

      {/* Resource request modal */}
      <ModalWrapper isOpen={!!resourceModal} onClose={() => setResourceModal(null)}>
        <ResourceRequestForm
          disasterId={resourceModal?.id}
          onSuccess={() => {
            setResourceModal(null);
            fetchRequests();
          }}
          onClose={() => setResourceModal(null)}
        />
      </ModalWrapper>
    </div>
  );
}

export default ReporterMapView;
