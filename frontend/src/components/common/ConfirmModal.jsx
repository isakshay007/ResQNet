import React, { useEffect } from "react";

function ConfirmModal({ 
  title = "Are you sure?", 
  message = "", 
  onConfirm, 
  onCancel 
}) {
  // ✅ Close on ESC key
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === "Escape") onCancel();
    };
    window.addEventListener("keydown", handleEsc);
    return () => window.removeEventListener("keydown", handleEsc);
  }, [onCancel]);

  return (
    <div
      className="fixed inset-0 flex items-center justify-center bg-black/50 z-50"
      onClick={onCancel} // close if backdrop clicked
      role="dialog"
      aria-modal="true"
    >
      <div
        className="bg-white rounded-2xl shadow-2xl p-6 w-96 text-center transform transition-all duration-200 scale-95 animate-fadeIn"
        onClick={(e) => e.stopPropagation()} //  prevent closing when clicking inside
      >
        {/* Title */}
        <h2
          className="text-2xl font-extrabold mb-4 
                     bg-gradient-to-r from-teal-700 via-teal-600 to-teal-500 
                     bg-clip-text text-transparent"
        >
          {title}
        </h2>

        {/* Optional message */}
        {message && <p className="text-gray-700 mb-6">{message}</p>}

        {/* Actions */}
        <div className="flex justify-center space-x-4">
          <button
            onClick={onConfirm}
            className="px-5 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
          >
            Yes
          </button>
          <button
            onClick={onCancel}
            className="px-5 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmModal;
