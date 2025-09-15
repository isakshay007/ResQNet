// src/components/common/ConfirmModal.jsx
import React, { useEffect } from "react";

function ConfirmModal({
  title = "Are you sure?",
  message = "",
  onConfirm,
  onCancel,
}) {
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === "Escape") onCancel();
    };
    window.addEventListener("keydown", handleEsc);
    return () => window.removeEventListener("keydown", handleEsc);
  }, [onCancel]);

  return (
    <div
      className="fixed inset-0 flex items-center justify-center bg-black/40 backdrop-blur-sm z-[2000]"
      onClick={onCancel}
    >
      <div
        className="bg-white rounded-2xl shadow-2xl p-6 w-[90%] max-w-md text-center relative 
                   transform transition-all duration-300 scale-95 opacity-0 animate-fadeIn"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Title */}
        <h2 className="text-xl font-bold text-gray-800 mb-3">{title}</h2>

        {/* Message */}
        {message && (
          <p className="text-gray-600 mb-6 leading-relaxed">{message}</p>
        )}

        {/* Buttons */}
        <div className="flex justify-center space-x-4">
          <button
            onClick={onConfirm}
            className="px-5 py-2 rounded-lg font-semibold bg-red-500 text-white hover:bg-red-600 transition"
          >
            Yes, Logout
          </button>
          <button
            onClick={onCancel}
            className="px-5 py-2 rounded-lg font-semibold bg-gray-200 text-gray-800 hover:bg-gray-300 transition"
          >
            Cancel
          </button>
        </div>
      </div>

      {/* Animation style */}
      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: scale(0.95); }
          to { opacity: 1; transform: scale(1); }
        }
        .animate-fadeIn {
          animation: fadeIn 0.25s ease-out forwards;
        }
      `}</style>
    </div>
  );
}

export default ConfirmModal;
