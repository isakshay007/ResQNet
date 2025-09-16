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
import "leaflet/dist/leaflet.css";
import ReportDisasterForm from "./ReportDisasterForm";
import ResourceRequestForm from "./ResourceRequestForm";
import ModalWrapper from "./ModalWrapper.jsx";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";
import { getDisasterIcon, reporterIcon, responderIcon } from "./mapIcons.js";
import MapLegend from "./MapLegend";
import DisasterPopup from "./DisasterPopup";

// Map click handler
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
  const [allRequests, setAllRequests] = useState([]);
  const [myRequests, setMyRequests] = useState([]);
  const [contributions, setContributions] = useState([]);
  const [resourceModal, setResourceModal] = useState(null);

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

  const fetchRequestsAndContributions = async () => {
    try {
      // Reporter’s own requests
      const myReqRes = await axios.get("http://localhost:8080/api/requests/my", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setMyRequests(myReqRes.data);

      // All requests (reporters are allowed to view global list too)
      const allReqRes = await axios.get("http://localhost:8080/api/requests", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setAllRequests(allReqRes.data);

      // All contributions (backend filters by reporter’s scope automatically)
      const contribRes = await axios.get("http://localhost:8080/api/contributions", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setContributions(contribRes.data);
    } catch (err) {
      console.error("Failed to fetch requests/contributions:", err);
    }
  };

  useEffect(() => {
    if (token) {
      fetchDisasters();
      fetchRequestsAndContributions();
    }
  }, [token]);

  const handleDisasterSuccess = (newDisaster) => {
    setDisasters((prev) => [...prev, newDisaster]);
    setSelectedPosition(null);
  };

  // === Group contributions by responder+location ===
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
        center={[42.34, -71.0895]} // Default near Northeastern
        zoom={14}
        scrollWheelZoom
        className="w-full h-full"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <CurrentLocation setCurrentLocation={setCurrentLocation} />
        <ClickHandler onMapClick={setSelectedPosition} />

        {/* Reporter location */}
        {currentLocation && (
          <Marker position={currentLocation} icon={reporterIcon}>
            <Tooltip direction="top">📍 My Location (Reporter – Purple)</Tooltip>
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
                <Tooltip direction="top">🤝 Contribution Location (Responder – Blue)</Tooltip>
                <Popup>
                  <div>
                    <h4 className="font-bold">Responder Contribution</h4>
                    <p>
                      <strong>Contributor:</strong> {c.responderEmail}
                    </p>
                    <p>
                      <strong>Categories:</strong> {c.categories.join(", ")}
                    </p>
                    <p>
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

        {/* Disasters */}
        {disasters.map((d) => {
          const relatedRequests = allRequests.filter((r) => r.disasterId === d.id);
          const disasterContributions = contributions.filter((c) =>
            relatedRequests.some((r) => r.id === c.requestId)
          );

          const status =
            relatedRequests.length > 0
              ? relatedRequests.every((r) => r.status === "FULFILLED")
                ? "fulfilled"
                : relatedRequests.some((r) => r.fulfilledQuantity > 0)
                ? "partial"
                : "reported"
              : "reported";

          return (
            <Marker
              key={d.id}
              position={[d.latitude, d.longitude]}
              icon={getDisasterIcon(status)}
            >
              <Tooltip direction="top">
                {status === "reported" && "⚠️ Reported Disaster"}
                {status === "partial" && "🟠 Partial Resource Fulfillment"}
                {status === "fulfilled" && "✅ Fulfilled Disaster"}
              </Tooltip>
              <Popup minWidth={420} maxWidth={500}>
                <DisasterPopup
                  disaster={d}
                  requests={relatedRequests}
                  contributions={disasterContributions}
                  onRequestResources={() => setResourceModal({ id: d.id })}
                />
              </Popup>
            </Marker>
          );
        })}

        {/* New disaster creation */}
        {selectedPosition && selectedPosition.formType === "disaster" && (
          <Marker
            position={selectedPosition.position}
            icon={getDisasterIcon("reported")}
          >
            <Tooltip direction="top">⚠️ Reported Disaster</Tooltip>
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

      {/* Legend (Reporter role) */}
      <MapLegend role="reporter" />

      {/* Resource Request Modal */}
      <ModalWrapper
        isOpen={!!resourceModal}
        onClose={() => setResourceModal(null)}
      >
        <ResourceRequestForm
          disasterId={resourceModal?.id}
          onSuccess={() => {
            setResourceModal(null);
            fetchRequestsAndContributions();
          }}
          onClose={() => setResourceModal(null)}
        />
      </ModalWrapper>
    </div>
  );
}

export default ReporterMapView;
