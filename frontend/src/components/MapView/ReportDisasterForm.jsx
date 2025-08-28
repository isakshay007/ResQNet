import React from "react";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";

function ReportDisasterForm({ position, onSuccess, onClose }) {
  const { token } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const form = new FormData(e.target);

    const data = {
      type: form.get("type"),
      severity: form.get("severity"),
      description: form.get("description"),
      latitude: position.lat,
      longitude: position.lng,
    };

    try {
      const res = await axios.post("http://localhost:8080/api/disasters", data, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      alert("✅ Disaster reported successfully!");
      console.log("✅ Disaster saved:", res.data);
      onSuccess(res.data);
    } catch (err) {
      console.error("❌ Report failed:", err.response?.data || err.message);
      alert("❌ Failed to report disaster. Please try again.");
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

        {/* Disaster Type */}
        <div>
          <label className="block text-gray-700 font-medium mb-1">
            Disaster Type
          </label>
          <input
            name="type"
            placeholder="Flood, Fire, Earthquake..."
            required
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
            required
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
            required
            rows="4"
            className="w-full border border-gray-300 p-3 rounded-lg text-gray-800 focus:ring-2 focus:ring-red-500"
          />
        </div>

        {/* Buttons */}
        <div className="flex space-x-3 pt-2">
          <button
            type="submit"
            className="flex-1 bg-red-600 text-white py-3 rounded-lg font-semibold hover:bg-red-700"
          >
            Submit
          </button>
          <button
            type="button"
            onClick={onClose}
            className="flex-1 bg-gray-400 text-white py-3 rounded-lg font-semibold hover:bg-gray-500"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

export default ReportDisasterForm;
