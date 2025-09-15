// src/components/MapView/ReportDisasterForm.jsx
import React, { useState } from "react";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";
import { FiAlertTriangle } from "react-icons/fi";
import toast from "react-hot-toast";

function ReportDisasterForm({ position, onSuccess, onClose }) {
  const { token } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // helper to normalize strings
  const normalize = (str) =>
    str
      ?.replace(/[\p{Emoji_Presentation}\p{Emoji}\uFE0F]/gu, "")
      .trim()
      .toLowerCase();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const form = new FormData(e.target);
    const type = form.get("type");
    const severity = form.get("severity");
    const description = form.get("description");

    if (!type || !severity || !description) {
      setError("⚠️ All fields are required.");
      return;
    }

    const data = {
      type: normalize(type),
      severity: normalize(severity),
      description: description.trim().toLowerCase(),
      latitude: position.lat,
      longitude: position.lng,
    };

    try {
      setLoading(true);
      const res = await axios.post("http://localhost:8080/api/disasters", data, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (onSuccess) onSuccess(res.data);

      e.target.reset();
      toast.success(" Disaster successfully reported!");
      onClose?.(); // close popup if provided
    } catch (err) {
      console.error("Report failed:", err.response?.data || err.message);
      toast.error(
        err.response?.data?.message ||
          " Failed to report disaster. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center w-full h-full relative">
      <form
        onSubmit={handleSubmit}
        className="space-y-5 max-w-md w-full p-6 bg-white rounded-xl shadow-xl border-t-4 border-red-600 relative"
      >
        {/* Header */}
        <div className="flex items-center justify-center space-x-2 mb-2">
          <FiAlertTriangle className="text-red-600 text-2xl" />
          <h2 className="font-extrabold text-xl text-gray-800">
            Report Disaster
          </h2>
        </div>

        {/* Error */}
        {error && (
          <p className="text-red-500 text-sm text-center font-medium">{error}</p>
        )}

        {/* Type */}
        <div>
          <label className="block text-gray-700 font-medium mb-1">
            Disaster Type
          </label>
          <select
            name="type"
            defaultValue=""
            className="w-full border border-gray-300 p-3 rounded-lg text-gray-800 focus:ring-2 focus:ring-red-500"
          >
            <option value="" disabled>
              Select Type
            </option>
            <option value="flood">🌊 Flood</option>
            <option value="fire">🔥 Fire</option>
            <option value="earthquake">🌍 Earthquake</option>
            <option value="storm">⛈️ Storm</option>
            <option value="other">⚠️ Other</option>
          </select>
        </div>

        {/* Severity */}
        <div>
          <label className="block text-gray-700 font-medium mb-1">
            Severity
          </label>
          <select
            name="severity"
            defaultValue=""
            className="w-full border border-gray-300 p-3 rounded-lg text-gray-800 focus:ring-2 focus:ring-red-500"
          >
            <option value="" disabled>
              Select Severity
            </option>
            <option value="low">🟢 Low</option>
            <option value="medium">🟠 Medium</option>
            <option value="high">🔴 High</option>
          </select>
        </div>

        {/* Location */}
        <div>
          <label className="block text-gray-700 font-medium mb-1">Location</label>
          <input
            type="text"
            readOnly
            value={`Lat: ${position.lat.toFixed(4)}, Lng: ${position.lng.toFixed(
              4
            )}`}
            className="w-full border border-gray-300 p-3 rounded-lg bg-gray-100 text-gray-700"
          />
        </div>

        {/* Description */}
        <div>
          <label className="block text-gray-700 font-medium mb-1">
            Description
          </label>
          <textarea
            name="description"
            placeholder="Describe the disaster..."
            rows="4"
            className="w-full border border-gray-300 p-3 rounded-lg text-gray-800 focus:ring-2 focus:ring-red-500"
          />
        </div>

        {/* Buttons */}
        <div className="flex space-x-3 pt-2">
          <button
            type="submit"
            disabled={loading}
            className={`flex-1 flex items-center justify-center py-3 rounded-lg font-bold text-white transition ${
              loading
                ? "bg-red-400 cursor-not-allowed"
                : "bg-red-600 hover:bg-red-700"
            }`}
          >
            🚨 {loading ? "Submitting..." : "Submit Report"}
          </button>
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="flex-1 bg-gray-400 text-white py-3 rounded-lg font-semibold hover:bg-gray-500 disabled:opacity-60"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

export default ReportDisasterForm;
