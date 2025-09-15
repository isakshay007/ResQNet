// src/components/MapView/ResourceRequestForm.jsx
import React, { useState } from "react";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";
import { FiPackage } from "react-icons/fi";
import toast from "react-hot-toast";

function ResourceRequestForm({ disasterId, onSuccess, onClose }) {
  const { token } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // normalize (strip emojis, lowercase)
  const normalize = (str) =>
    str?.replace(/[\p{Emoji_Presentation}\p{Emoji}\uFE0F]/gu, "").trim().toLowerCase();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const form = new FormData(e.target);
    let category = form.get("category");
    const requestedQuantity = parseInt(form.get("requestedQuantity"), 10);

    if (!category || requestedQuantity <= 0) {
      setError("⚠️ All fields are required with valid values.");
      return;
    }

    category = normalize(category);
    const data = { category, requestedQuantity, disasterId };

    try {
      setLoading(true);
      const res = await axios.post("http://localhost:8080/api/requests", data, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (onSuccess) onSuccess(res.data);

      e.target.reset();
      toast.success(" Resource request submitted!");
      onClose?.();
    } catch (err) {
      console.error("Resource request failed:", err.response?.data || err.message);
      toast.error(
        err.response?.data?.message || " Failed to submit resource request."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center w-full h-full relative">
      <form
        onSubmit={handleSubmit}
        className="relative space-y-4 max-w-lg w-full p-6 bg-white rounded-xl shadow-xl border-t-4 border-blue-600"
      >
        {/* Header */}
        <div className="flex items-center justify-center space-x-2 mb-3">
          <FiPackage className="text-blue-600 text-2xl" />
          <h2 className="font-extrabold text-xl text-gray-800">Request Resources</h2>
        </div>

        {error && <p className="text-red-500 text-sm text-center font-medium">{error}</p>}

        {/* Category */}
        <div>
          <label className="block text-gray-700 font-medium text-sm mb-1">
            Resource Category
          </label>
          <select
            name="category"
            className="w-full border border-gray-300 p-3 rounded-lg text-sm text-gray-800 focus:ring-2 focus:ring-blue-500"
            defaultValue=""
          >
            <option value="" disabled>Select Category</option>
            <option value="food">🍞 Food</option>
            <option value="water">💧 Water</option>
            <option value="shelter">🏠 Shelter</option>
            <option value="medical">⚕️ Medical</option>
          </select>
        </div>

        {/* Quantity */}
        <div>
          <label className="block text-gray-700 font-medium text-sm mb-1">
            Requested Quantity
          </label>
          <input
            type="number"
            name="requestedQuantity"
            placeholder="Enter quantity (e.g., 50)"
            className="w-full border border-gray-300 p-3 rounded-lg text-sm text-gray-800 focus:ring-2 focus:ring-blue-500"
            min="1"
          />
        </div>

        {/* Buttons */}
        <div className="flex space-x-3 pt-2">
          <button
            type="submit"
            disabled={loading}
            className={`flex-1 flex items-center justify-center py-2 rounded-lg font-semibold text-sm text-white transition ${
              loading ? "bg-blue-400 cursor-not-allowed" : "bg-blue-600 hover:bg-blue-700"
            }`}
          >
            {loading ? "Submitting..." : "Submit Request"}
          </button>
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="flex-1 bg-gray-400 text-white py-2 rounded-lg font-semibold text-sm hover:bg-gray-500 disabled:opacity-60"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

export default ResourceRequestForm;
