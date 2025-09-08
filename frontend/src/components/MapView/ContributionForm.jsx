// src/components/MapView/ContributionForm.jsx
import React, { useState, useEffect } from "react";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";

function ContributionForm({ requestId, requestCategory, onSuccess, onClose }) {
  const { token } = useAuth();

  const [itemType, setItemType] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [latitude, setLatitude] = useState(null);
  const [longitude, setLongitude] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Reset form when modal opens
  useEffect(() => {
    setQuantity(1);
    setError("");
    setSuccess("");
    if (requestCategory) {
      setItemType(requestCategory.toLowerCase()); // auto-fill from request
    } else {
      setItemType("");
    }
  }, [requestId, requestCategory]);

  //  Detect responder's location
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setLatitude(pos.coords.latitude);
          setLongitude(pos.coords.longitude);
        },
        (err) => {
          console.error("Failed to get location:", err);
          setError("Could not fetch location. Please allow location access.");
        }
      );
    } else {
      setError("Geolocation is not supported in this browser.");
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (quantity <= 0) {
      setError("Quantity must be at least 1.");
      return;
    }
    if (!itemType) {
      setError("Please select an item type.");
      return;
    }

    setLoading(true);

    try {
      await axios.post(
        "http://localhost:8080/api/contributions",
        {
          requestId,
          contributedQuantity: quantity,
          latitude,
          longitude,
          category: itemType, //  send category to backend
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setSuccess("Contribution submitted successfully!");
      if (onSuccess) onSuccess();

      // Reset before closing
      setItemType(requestCategory ? requestCategory.toLowerCase() : "");
      setQuantity(1);

      setTimeout(() => {
        onClose();
      }, 1200);
    } catch (err) {
      console.error("Contribution failed:", err);
      setError("Failed to submit contribution. Try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-4">
      <h2 className="text-lg font-bold mb-2">Contribute</h2>

      {error && <p className="text-red-500 text-sm mb-2">{error}</p>}
      {success && <p className="text-green-600 text-sm mb-2">{success}</p>}

      {/* Item Type */}
      <label className="block mb-2">
        Item Type:
        <select
          value={itemType}
          onChange={(e) => setItemType(e.target.value)}
          className="border rounded w-full p-1"
          required
          disabled={!!requestCategory} // lock if predefined
        >
          <option value="">-- Select --</option>
          <option value="food">🍞 Food</option>
          <option value="water">💧 Water</option>
          <option value="medical">🏥 Medical</option>
          <option value="shelter">⛺ Shelter</option>
        </select>
      </label>
      {requestCategory && (
        <p className="text-xs text-gray-500 mb-2">
          Requested Category: {requestCategory}
        </p>
      )}

      {/* Quantity */}
      <label className="block mb-2">
        Quantity:
        <input
          type="number"
          min="1"
          value={quantity}
          onChange={(e) => setQuantity(Number(e.target.value))}
          className="border rounded w-full p-1"
          required
        />
      </label>

      {/* Location */}
      {latitude && longitude && (
        <p className="text-sm text-gray-600 mb-2">
          📍 Location detected: {latitude.toFixed(4)}, {longitude.toFixed(4)}
        </p>
      )}

      <div className="flex justify-end gap-2 mt-3">
        <button
          type="button"
          onClick={onClose}
          className="px-3 py-1 bg-gray-300 rounded"
          disabled={loading}
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={loading} // allow submit even without coords
          className="px-3 py-1 bg-blue-600 text-white rounded"
        >
          {loading ? "Submitting..." : "Submit"}
        </button>
      </div>
    </form>
  );
}

export default ContributionForm;
