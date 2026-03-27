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

  useEffect(() => {
    if (!isOpen || !onClose) return;
    const onEscape = (event) => {
      if (event.key === "Escape") {
        onClose();
      }
    };
    window.addEventListener("keydown", onEscape);
    return () => window.removeEventListener("keydown", onEscape);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return createPortal(
    <div
      className="fixed inset-0 z-[1000] flex items-center justify-center bg-black/20"
      aria-modal="true"
      role="dialog"
      onClick={() => onClose?.()}
    >
      {/* Modal box */}
      <div
        className="relative z-[1001] bg-white rounded-lg shadow-xl w-[400px] p-6 border border-gray-200"
        onClick={(event) => event.stopPropagation()}
      >
        {children}
      </div>
    </div>,
    document.body
  );
}

export default ModalWrapper;
