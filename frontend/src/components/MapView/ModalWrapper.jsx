import React, { useEffect } from "react";
import { createPortal } from "react-dom";

function ModalWrapper({ isOpen, onClose, children }) {
  // Prevent background scrolling
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "auto";
    }
    return () => (document.body.style.overflow = "auto");
  }, [isOpen]);

  if (!isOpen) return null;

  return createPortal(
    <div
      className="fixed inset-0 z-[1000] flex items-center justify-center pointer-events-none"
      aria-modal="true"
      role="dialog"
    >
      {/* ✅ No dark backdrop, fully transparent */}
      {/* Modal box */}
      <div className="relative z-[1001] bg-white rounded-lg shadow-xl w-[400px] p-6 pointer-events-auto border border-gray-200">
        {children}
      </div>
    </div>,
    document.body
  );
}

export default ModalWrapper;
