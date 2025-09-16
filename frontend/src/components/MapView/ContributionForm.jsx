// src/components/MapView/ContributionForm.jsx
import React, { useState, useEffect } from "react";
import { useAuth } from "../../context/AuthContext";
import { FiGift, FiX } from "react-icons/fi";
import toast from "react-hot-toast";
import api from "../../utils/api"; // centralized axios instance

// Category → Icon mapping
const categoryIcons = {
  food: "🍞",
  water: "💧",
  shelter: "🏠",
  medical: "⚕️",
};

function ContributionForm({ requestId, requestCategory, onSuccess, onClose }) {
  const { token } = useAuth();

  const [itemType, setItemType] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [latitude, setLatitude] = useState(null);
  const [longitude, setLongitude] = useState(null);
  const [loading, setLoading] = useState(false);

  // mock: multiple prefilled items
  const [extraItems] = useState([
    { id: 1, category: "food", quantity: 10 },
    { id: 2, category: "water", quantity: 5 },
    { id: 3, category: "shelter", quantity: 2 },
    { id: 4, category: "medical", quantity: 1 },
  ]);
  const [showAllItems, setShowAllItems] = useState(false);

  // Reset on modal open
  useEffect(() => {
    setQuantity(1);
    if (requestCategory) {
      setItemType(requestCategory.toLowerCase());
    } else {
      setItemType("");
    }
  }, [requestId, requestCategory]);

  // Detect responder location
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setLatitude(pos.coords.latitude);
          setLongitude(pos.coords.longitude);
        },
        () => {
          toast.error("⚠️ Could not fetch location. Please allow location access.");
        }
      );
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (quantity <= 0) {
      toast.error("⚠️ Quantity must be at least 1.");
      return;
    }
    if (!itemType) {
      toast.error("⚠️ Please select an item type.");
      return;
    }

    setLoading(true);
    try {
      await api.post(
        "/contributions",
        {
          requestId,
          contributedQuantity: quantity,
          latitude,
          longitude,
          category: itemType,
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      toast.success("✅ Contribution submitted!");
      if (onSuccess) onSuccess();

      setTimeout(() => {
        onClose?.();
      }, 1500);
    } catch (err) {
      console.error("Contribution failed:", err);
      const msg =
        err.response?.data?.message ||
        "Failed to submit contribution. Try again.";
      toast.error("⚠️ " + msg);
    } finally {
      setLoading(false);
    }
  };

  // limit view
  const visibleItems = showAllItems ? extraItems : extraItems.slice(0, 3);

  return (
    <div className="flex items-center justify-center w-full h-full relative animate-scaleIn">
      <form
        onSubmit={handleSubmit}
        className="space-y-4 w-[520px] max-w-lg p-5 bg-white rounded-2xl shadow-2xl border border-gray-200 relative"
      >
        {/* Close button */}
        <button
          type="button"
          onClick={onClose}
          disabled={loading}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition"
        >
          <FiX className="text-2xl" />
        </button>

        {/* Gradient Header */}
        <div className="bg-gradient-to-r from-blue-600 via-cyan-500 to-sky-400 text-white px-5 py-2.5 rounded-xl shadow-md flex items-center gap-3">
          <FiGift className="text-2xl drop-shadow-sm" />
          <h2 className="font-extrabold text-lg tracking-wide">
            Contribute Resources
          </h2>
        </div>

        {/* Item Type */}
        <div>
          <label className="block text-gray-700 font-semibold text-sm">
            Item Type
          </label>
          <select
            value={itemType}
            onChange={(e) => setItemType(e.target.value)}
            className="w-full border border-gray-300 p-2.5 rounded-lg text-gray-800 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm"
            required
            disabled={!!requestCategory}
          >
            <option value="">-- Select --</option>
            {Object.keys(categoryIcons).map((cat) => (
              <option key={cat} value={cat}>
                {categoryIcons[cat]} {cat.charAt(0).toUpperCase() + cat.slice(1)}
              </option>
            ))}
          </select>
          {requestCategory && (
            <p className="text-xs text-gray-500 mt-1 italic">
              Requested Category: {requestCategory}
            </p>
          )}
        </div>

        {/* Quantity */}
        <div>
          <label className="block text-gray-700 font-semibold text-sm">
            Quantity
          </label>
          <input
            type="number"
            min="1"
            value={quantity}
            onChange={(e) => setQuantity(Number(e.target.value))}
            className="w-full border border-gray-300 p-2.5 rounded-lg text-gray-800 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 shadow-sm"
            required
          />
        </div>

        {/* Location */}
        {latitude && longitude && (
          <div className="px-4 py-2 bg-gray-50 border rounded-lg text-sm text-gray-700 shadow-sm">
            📍 Location detected:{" "}
            <span className="font-semibold text-gray-900">
              {latitude.toFixed(4)}, {longitude.toFixed(4)}
            </span>
          </div>
        )}

        {/* Extra Items */}
        {extraItems.length > 0 && (
          <div>
            <h4 className="font-semibold text-sm text-gray-800 mb-1">
              Previous Items
            </h4>
            <ul className="space-y-1">
              {visibleItems.map((i) => (
                <li
                  key={i.id}
                  className="px-3 py-2 bg-gray-50 rounded-lg border flex justify-between items-center shadow-sm hover:bg-gray-100 transition"
                >
                  <span className="flex items-center gap-2 font-medium">
                    <span className="text-lg">{categoryIcons[i.category]}</span>
                    {i.category}
                  </span>
                  <span className="text-gray-700 font-semibold">
                    ×{i.quantity}
                  </span>
                </li>
              ))}
            </ul>
            {extraItems.length > 3 && (
              <div className="text-right mt-1">
                <button
                  type="button"
                  onClick={() => setShowAllItems(!showAllItems)}
                  className="text-blue-600 text-xs font-semibold hover:underline"
                >
                  {showAllItems ? "Show Less" : `${extraItems.length - 3} more +`}
                </button>
              </div>
            )}
          </div>
        )}

        {/* Buttons */}
        <div className="flex space-x-3 pt-2">
          <button
            type="submit"
            disabled={loading}
            className={`flex-1 py-2.5 rounded-lg font-semibold text-sm text-white shadow-md transition ${
              loading
                ? "bg-gradient-to-r from-blue-400 to-cyan-400 cursor-not-allowed"
                : "bg-gradient-to-r from-blue-600 via-cyan-500 to-sky-500 hover:shadow-lg"
            }`}
          >
            {loading ? "Submitting..." : " Submit "}
          </button>
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="flex-1 bg-gray-300 text-gray-800 py-2.5 rounded-lg font-semibold text-sm hover:bg-gray-400 transition shadow-sm"
          >
            Cancel
          </button>
        </div>
      </form>

      {/* Animations */}
      <style>{`
        @keyframes scaleIn {
          from { opacity: 0; transform: scale(0.96); }
          to { opacity: 1; transform: scale(1); }
        }
        .animate-scaleIn {
          animation: scaleIn 0.25s ease-out;
        }
      `}</style>
    </div>
  );
}

export default ContributionForm;
