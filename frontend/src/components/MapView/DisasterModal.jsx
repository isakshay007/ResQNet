import React from "react";

function DisasterModal({ position, onClose, children }) {
  if (!position) return null; // only show if active

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-[9999]">
      <div className="bg-white rounded-lg shadow-xl w-[500px] max-w-full p-6 relative">
        
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute top-3 right-3 text-gray-600 hover:text-red-600 text-xl font-bold"
        >
          ✖
        </button>

        {children}
      </div>
    </div>
  );
}

export default DisasterModal;
