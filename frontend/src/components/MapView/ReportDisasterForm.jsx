// src/components/MapView/ReportDisasterForm.jsx
import React, { useState } from "react";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";

function ReportDisasterForm({ position, onSuccess, onClose }) {
  const { token } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const form = new FormData(e.target);
    const type = form.get("type");
    const severity = form.get("severity");
    const description = form.get("description");

    if (!type || !severity || !description) {
      setError("All fields are required.");
      return;
    }

    const data = {
      type,
      severity,
      description,
      latitude: position.lat,
      longitude: position.lng,
    };

    try {
      setLoading(true);
      const res = await axios.post("http://localhost:8080/api/disasters", data, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (onSuccess) onSuccess(res.data);

      e.target.reset(); // clear form
      setTimeout(() => {
        onClose();
      }, 1000);
    } catch (err) {
      console.error("Report failed:", err.response?.data || err.message);
      setError(err.response?.data?.message || "Failed to report disaster. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center w-full h-full">
      <form
        onSubmit={handleSubmit}
        className="space-y-4 max-w-md w-full p-6 bg-white rounded-xl shadow-lg"
      >
        <h2 className="font-bold text-xl text-center text-gray-800 mb-2">
          Report Disaster
        </h2>

        {error && <p className="text-red-500 text-sm">{error}</p>}

        {/* Disaster Type */}
        <div>
          <label className="block text-gray-700 font-medium mb-1">
            Disaster Type
          </label>
          <input
            name="type"
            placeholder="Flood, Fire, Earthquake..."
            className="w-full border border-gray-300 p-3 rounded-lg text-gray-800 focus:ring-2 focus:ring-red-500"
          />
        </div>

        {/* Severity Dropdown */}
        <div>
          <label className="block text-gray-700 font-medium mb-1">
            Severity
          </label>
          <select
            name="severity"
            className="w-full border border-gray-300 p-3 rounded-lg text-gray-800 focus:ring-2 focus:ring-red-500"
          >
            <option value="">Select Severity</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>
        </div>

        {/* Description */}
        <div>
          <label className="block text-gray-700 font-medium mb-1">
            Description
          </label>
          <textarea
            name="description"
            placeholder="Describe the disaster in detail..."
            rows="4"
            className="w-full border border-gray-300 p-3 rounded-lg text-gray-800 focus:ring-2 focus:ring-red-500"
          />
        </div>

        {/* Buttons */}
        <div className="flex space-x-3 pt-2">
          <button
            type="submit"
            disabled={loading}
            className={`flex-1 py-3 rounded-lg font-semibold text-white transition ${
              loading
                ? "bg-red-400 cursor-not-allowed"
                : "bg-red-600 hover:bg-red-700"
            }`}
          >
            {loading ? "Submitting..." : "Submit"}
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
