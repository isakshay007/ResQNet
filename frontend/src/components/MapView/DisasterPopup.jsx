// src/components/MapView/DisasterPopup.jsx
import React, { useState } from "react";

// Category → Icon mapping
const categoryIcons = {
  food: "🍞",
  water: "💧",
  shelter: "🏠",
  medical: "⚕️",
};

function DisasterPopup({ disaster, requests = [], contributions = [], onRequestResources }) {
  const [showAllRequests, setShowAllRequests] = useState(false);
  const [showAllContribs, setShowAllContribs] = useState(false);

  // Format severity label
  const formatSeverity = (severity) =>
    severity ? severity.charAt(0).toUpperCase() + severity.slice(1).toLowerCase() : "";

  // Status color mapping
  const getStatusBadge = (status) => {
    switch (status) {
      case "FULFILLED":
        return <span className="text-green-600 font-semibold">(Fulfilled)</span>;
      case "PARTIAL":
        return <span className="text-orange-500 font-semibold">(Partial)</span>;
      case "REPORTED":
      default:
        return <span className="text-gray-500 italic">(Reported)</span>;
    }
  };

  // Gradient mapping based on severity
  const severityGradient = {
    High: "from-red-600 to-purple-600",
    Medium: "from-orange-500 to-yellow-500",
    Low: "from-green-500 to-teal-500",
  };

  const gradientClass =
    severityGradient[formatSeverity(disaster.severity)] || "from-gray-500 to-blue-600";

  // Limit items shown initially
  const visibleRequests = showAllRequests ? requests : requests.slice(0, 3);
  const visibleContributions = showAllContribs ? contributions : contributions.slice(0, 3);

  return (
    <div className="space-y-5 text-sm">
      {/* Gradient Header */}
      <div
        className={`bg-gradient-to-r ${gradientClass} text-white px-4 py-2 rounded-lg shadow`}
      >
        <h3 className="font-bold text-base break-words">
          {disaster.type}{" "}
          {disaster.severity && `(${formatSeverity(disaster.severity)} Alert)`}
        </h3>
      </div>

      {/* Description */}
      <p className="text-gray-800">
        <strong>Description:</strong>{" "}
        <span className="break-words">{disaster.description}</span>
      </p>

      {/* Reporter + Reported On */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between text-xs">
        <p className="text-gray-800">
          <strong>Reported By:</strong>{" "}
          <span className="break-words">{disaster.reporterEmail}</span>
        </p>
        {disaster.createdAt && (
          <p className="text-gray-500 italic mt-1 sm:mt-0">
            {new Date(disaster.createdAt).toLocaleString()}
          </p>
        )}
      </div>

      {/* Requests */}
      <div>
        <h4 className="font-semibold">Requests</h4>
        {requests.length > 0 ? (
          <>
            <ul className="space-y-2 mt-2">
              {visibleRequests.map((r) => (
                <li
                  key={r.id}
                  className="px-3 py-2 bg-gray-50 rounded-lg border text-xs flex justify-between items-center shadow-sm"
                >
                  <span className="font-medium flex items-center gap-1">
                    {categoryIcons[r.category?.toLowerCase()] || "📦"}{" "}
                    {r.category?.charAt(0).toUpperCase() + r.category?.slice(1).toLowerCase()}
                  </span>
                  <span className="text-gray-700">
                    {r.fulfilledQuantity}/{r.requestedQuantity} {getStatusBadge(r.status)}
                  </span>
                </li>
              ))}
            </ul>
            {requests.length > 3 && (
              <div className="text-right mt-1">
                <button
                  onClick={() => setShowAllRequests(!showAllRequests)}
                  className="text-blue-600 text-xs hover:underline"
                >
                  {showAllRequests ? "Show Less" : `${requests.length - 3} more +`}
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
        {contributions.length > 0 ? (
          <>
            <ul className="space-y-2 mt-2">
              {visibleContributions.map((c) => (
                <li
                  key={c.id}
                  className="px-3 py-2 bg-gray-50 rounded-lg border text-xs flex justify-between items-center shadow-sm"
                >
                  <span className="font-medium break-words">{c.responderEmail}</span>
                  <span>
                    {c.contributedQuantity} (
                    {categoryIcons[c.category?.toLowerCase()] || "📦"}{" "}
                    {c.category?.charAt(0).toUpperCase() + c.category?.slice(1).toLowerCase()})
                  </span>
                </li>
              ))}
            </ul>
            {contributions.length > 3 && (
              <div className="text-right mt-1">
                <button
                  onClick={() => setShowAllContribs(!showAllContribs)}
                  className="text-blue-600 text-xs hover:underline"
                >
                  {showAllContribs ? "Show Less" : `${contributions.length - 3} more +`}
                </button>
              </div>
            )}
          </>
        ) : (
          <p className="text-xs text-gray-500 italic mt-1">None</p>
        )}
      </div>

      {/* Request Resources button */}
      <button
        onClick={onRequestResources}
        className="w-full mt-3 px-4 py-2 bg-blue-500 text-white rounded-lg text-sm font-semibold hover:bg-blue-600 transition shadow"
      >
        Request Resources +
      </button>
    </div>
  );
}

export default DisasterPopup;
