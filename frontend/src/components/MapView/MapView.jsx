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
import { useNavigate } from "react-router-dom";

const redIcon = new L.Icon({
  iconUrl: "https://maps.google.com/mapfiles/ms/icons/red-dot.png",
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32],
});

const greenIcon = new L.Icon({
  iconUrl: "https://maps.google.com/mapfiles/ms/icons/green-dot.png",
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32],
});

function ClickHandler({ onMapClick }) {
  useMapEvents({
    click(e) {
      onMapClick({ formType: "disaster", position: e.latlng });
    },
  });
  return null;
}

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

function MapView() {
  const { token } = useAuth();
  const navigate = useNavigate();

  const [selectedPosition, setSelectedPosition] = useState(null);
  const [disasters, setDisasters] = useState([]);
  const [currentLocation, setCurrentLocation] = useState(null);
  const [requests, setRequests] = useState([]);
  const [resourceModal, setResourceModal] = useState(null); // { id }

  const formatSeverity = (severity) =>
    severity.charAt(0).toUpperCase() + severity.slice(1).toLowerCase();

  const fetchDisasters = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/disasters", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setDisasters(res.data);
    } catch (err) {
      console.error("❌ Failed to fetch disasters:", err);
    }
  };

  const fetchRequests = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/requests/my", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setRequests(res.data);
    } catch (err) {
      console.error("❌ Failed to fetch requests:", err);
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

        {currentLocation && (
          <Marker position={currentLocation} icon={greenIcon}>
            <Tooltip direction="top" offset={[0, -20]} opacity={1}>
              Your current location 🟢
            </Tooltip>
          </Marker>
        )}

        {disasters.map((d) => {
          const disasterRequestCount = requests.filter(
            (r) => r.disasterId === d.id
          ).length;

          return (
            <Marker key={d.id} position={[d.latitude, d.longitude]} icon={redIcon}>
              <Popup minWidth={420} maxWidth={500} closeOnClick={false} autoClose={false}>
                <div className="space-y-2 text-sm">
                  <h3 className="font-bold text-lg text-red-600">
                    {d.type} ({formatSeverity(d.severity)} Alert)
                  </h3>
                  <p><strong>Description:</strong> {d.description}</p>
                  <p><strong>Reported By:</strong> {d.reporterEmail || "Anonymous"}</p>
                  {d.createdAt && (
                    <p className="text-gray-500 text-xs italic">
                      Reported On: {new Date(d.createdAt).toLocaleString()}
                    </p>
                  )}
                  <p className="text-gray-700 mt-2 font-semibold">
                    Request Count: {disasterRequestCount}
                  </p>

                  <div className="flex justify-center space-x-4 mt-3">
                    <button
                      onClick={() => setResourceModal({ id: d.id })}
                      className="px-3 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                    >
                      Request Resources
                    </button>
                    <button
                      onClick={() => navigate("/my-requests")}
                      className="px-3 py-2 bg-green-500 text-white rounded hover:bg-green-600"
                    >
                      View Requests
                    </button>
                  </div>
                </div>
              </Popup>
            </Marker>
          );
        })}

        {selectedPosition && selectedPosition.formType === "disaster" && (
          <Marker position={selectedPosition.position} icon={redIcon}>
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

      {/* ✅ Legend Box in Bottom Right */}

      <div className="absolute bottom-16 right-4 bg-white/90 backdrop-blur-sm 
                      border border-gray-300 rounded-md shadow-sm 
                      p-3 text-xs z-[1000] w-40">
        <h4 className="font-semibold text-gray-600 text-center mb-2">Legend</h4>

        <div className="space-y-2">
          {/* Current Location */}
          <div className="flex items-center space-x-2">
            <img
              src="https://maps.google.com/mapfiles/ms/icons/green-dot.png"
              alt="Current Location"
              className="w-3.5 h-3.5"
            />
            <span className="text-gray-700">Current Location</span>
          </div>

          {/* Reported Disaster */}
          <div className="flex items-center space-x-2">
            <img
              src="https://maps.google.com/mapfiles/ms/icons/red-dot.png"
              alt="Reported Disaster"
              className="w-3.5 h-3.5"
            />
            <span className="text-gray-700">Reported Disaster</span>
          </div>
        </div>
      </div>

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

export default MapView;
