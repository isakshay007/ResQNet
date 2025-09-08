import React from "react";

function Footer() {
  return (
    <footer className="w-full px-6 py-4 bg-white/90 backdrop-blur-lg shadow-inner text-center">
      <p className="font-medium text-gray-800">
        © {new Date().getFullYear()} ResQNet. All rights reserved.
      </p>
    </footer>
  );
}

export default Footer;
