import React, { useState } from "react";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";

function ResourceRequestForm({ disasterId, onSuccess, onClose }) {
  const { token } = useAuth();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const form = new FormData(e.target);

    const data = {
      category: form.get("category"),
      requestedQuantity: parseInt(form.get("requestedQuantity"), 10),
      disasterId,
    };

    try {
      setLoading(true);
      const res = await axios.post("http://localhost:8080/api/requests", data, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      alert("✅ Resource request submitted successfully!");
      console.log("✅ Resource request created:", res.data);

      e.target.reset(); // clear form fields
      onSuccess(res.data); // notify parent to refresh
    } catch (err) {
      console.error("❌ Failed to request resources:", err.response?.data || err.message);
      alert(
        `❌ Failed to submit resource request.\n${
          err.response?.data?.message || err.message
        }`
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full">
      {/* Title + Close */}
      <div className="flex justify-between items-center mb-3">
        <h2 className="font-bold text-lg text-blue-700">Request Resources</h2>
        <button
          type="button"
          onClick={onClose}
          className="text-gray-500 hover:text-gray-700 text-lg font-bold"
        >
          ✕
        </button>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Category */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Category
          </label>
          <select
            name="category"
            className="w-full border rounded-lg p-2 focus:ring-2 focus:ring-blue-500"
            required
          >
            <option value="">-- Select --</option>
            <option value="Food">Food</option>
            <option value="Water">Water</option>
            <option value="Shelter">Shelter</option>
            <option value="Medical">Medical</option>
          </select>
        </div>

        {/* Quantity */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Requested Quantity
          </label>
          <input
            type="number"
            name="requestedQuantity"
            className="w-full border rounded-lg p-2 focus:ring-2 focus:ring-blue-500"
            min="1"
            required
          />
        </div>

        {/* Buttons */}
        <div className="flex justify-end space-x-3 pt-2">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400"
            disabled={loading}
          >
            Cancel
          </button>
          <button
            type="submit"
            className={`px-4 py-2 rounded-lg text-white ${
              loading
                ? "bg-blue-400 cursor-not-allowed"
                : "bg-blue-600 hover:bg-blue-700"
            }`}
            disabled={loading}
          >
            {loading ? "Submitting..." : "Submit"}
          </button>
        </div>
      </form>
    </div>
  );
}

export default ResourceRequestForm;
