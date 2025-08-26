import React from "react";
import { useNavigate } from "react-router-dom";

function Welcome() {
  const navigate = useNavigate();

  return (
        <div
        className="min-h-screen flex items-center justify-center 
                    bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 text-white 
                    animate-gradient-x"
        >
        <div className="text-center space-y-8 -mt-16"> {/* ⬅ Moved content up */}
            {/* Branding */}
            <h1 className="text-7xl font-extrabold drop-shadow-lg flex justify-center items-center space-x-2">
            <span className="bg-gradient-to-r from-teal-200 via-white to-cyan-200 bg-clip-text text-transparent animate-gradient-x">
                ResQNet
            </span>
            <span>🌍</span>
            </h1>

        {/* Subtitle */}
        <p className="text-lg md:text-xl max-w-3xl mx-auto leading-relaxed">
          ResQNet connects communities in crisis with 
          the right help, the right people, and the right resources so that support 
          arrives faster, smarter, and safer.
        </p>

        {/* Buttons */}
        <div className="flex justify-center space-x-6 mt-6">
          <button
            onClick={() => navigate("/login")}
            className="px-6 py-3 text-white font-semibold rounded-lg shadow-lg 
                       bg-gradient-to-r from-teal-500 via-cyan-500 to-blue-600 
                       hover:scale-105 hover:shadow-xl transition-transform"
          >
            Login
          </button>
          <button
            onClick={() => navigate("/register")}
            className="px-6 py-3 text-white font-semibold rounded-lg shadow-lg 
                       bg-gradient-to-r from-teal-500 via-cyan-500 to-blue-600 
                       hover:scale-105 hover:shadow-xl transition-transform"
          >
            Register
          </button>
        </div>
      </div>
    </div>
  );
}

export default Welcome;
