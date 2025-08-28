import React from "react";

function Footer() {
  return (
    <footer className="bg-gray-900 text-white text-center py-3 text-sm">
      © {new Date().getFullYear()} ResQNet
    </footer>
  );
}

export default Footer;   // ✅ MUST be default export
