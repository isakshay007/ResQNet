import React, { useEffect } from "react";
import { FiCheckCircle, FiX } from "react-icons/fi";

function SuccessToast({ message, onClose, duration = 4000 }) {
  useEffect(() => {
    if (duration) {
      const timer = setTimeout(onClose, duration);
      return () => clearTimeout(timer);
    }
  }, [duration, onClose]);

  return (
    <div className="fixed top-6 right-6 bg-green-100 border border-green-400 text-green-800 px-6 py-4 rounded-lg shadow-lg flex items-center space-x-3 animate-fade-in-down z-[9999]">
      <FiCheckCircle className="text-green-600 text-2xl" />
      <span className="font-semibold">{message}</span>
      <button onClick={onClose} className="ml-2 text-green-600 hover:text-green-800">
        <FiX />
      </button>
    </div>
  );
}

export default SuccessToast;
