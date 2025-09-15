// src/components/MapView/MapLegend.jsx
import React from "react";

function MapLegend({ role }) {
  return (
    <div
      className="absolute bottom-12 right-3 bg-white/95 backdrop-blur-md 
                 border border-gray-300 rounded-lg shadow-md 
                 p-3 text-xs z-[1000] w-56"
    >
      <h4 className="font-semibold text-gray-700 text-center mb-2 text-sm">
        Map Legend
      </h4>

      <div className="space-y-2">
        {/* === Disasters === */}
        <div>
          <p className="text-[10px] font-semibold text-gray-500 mb-1">
            Disasters
          </p>
          <div className="flex items-center space-x-1 mb-0.5">
            <span>⚠️</span>
            <span className="text-gray-700 text-xs">Reported Disaster</span>
          </div>
          <div className="flex items-center space-x-1 mb-0.5">
            <span>🟠</span>
            <span className="text-gray-700 text-xs">
              Partial Resource Fulfillment
            </span>
          </div>
          <div className="flex items-center space-x-1">
            <span>✅</span>
            <span className="text-gray-700 text-xs">Fulfilled Disaster</span>
          </div>
        </div>

        <hr className="border-gray-200" />

        {/* === My Location === */}
        <div>
          <p className="text-[10px] font-semibold text-gray-500 mb-1">
            My Location
          </p>
          {role === "reporter" && (
            <div className="flex items-center space-x-1">
              <span>📍</span>
              <span className="text-gray-700 text-xs">Reporter (Purple)</span>
            </div>
          )}
          {role === "responder" && (
            <>
              <div className="flex items-center space-x-1 mb-0.5">
                <span>📍</span>
                <span className="text-gray-700 text-xs">
                  Responder Location (Blue)
                </span>
              </div>
              <div className="flex items-center space-x-1">
                <span>🤝</span>
                <span className="text-gray-700 text-xs">
                  Responder Contribution (Blue)
                </span>
              </div>
            </>
          )}
          {role === "admin" && (
            <div className="flex items-center space-x-1">
              <span>🛡️</span>
              <span className="text-gray-700 text-xs">Admin</span>
            </div>
          )}
        </div>

        <hr className="border-gray-200" />

        {/* === Contributions === */}
        <div>
          <p className="text-[10px] font-semibold text-gray-500 mb-1">
            Contributions
          </p>
          <div className="flex items-center space-x-1 mb-0.5">
            <span>🍞💧🏥⛺</span>
            <span className="text-gray-700 text-xs">
              {role === "reporter"
                ? "My Requests"
                : role === "admin"
                ? "All Categories"
                : "Categories"}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default MapLegend;
