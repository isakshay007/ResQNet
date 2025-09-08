// src/pages/Login.jsx
import React, { useState } from "react";
import { FiEye, FiEyeOff } from "react-icons/fi";
import { useNavigate } from "react-router-dom";
import api from "../utils/api";
import { useAuth } from "../context/AuthContext"; //  now using context instead of local hook

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { login } = useAuth(); //  shared global login from context

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      //  Call backend login API
      const res = await api.post("/auth/login", { email, password });

      if (res.data?.token) {
        //  Save JWT globally (AuthContext handles decoding)
        login(res.data.token);

        //  Redirect after success
        navigate("/dashboard");
      } else {
        setError("Login failed. No token returned from server.");
      }
    } catch (err) {
      console.error("Login error:", err);
      setError(
        err.response?.data?.message ||
          "Invalid email or password. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="min-h-screen flex items-center justify-center 
                 bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 text-white 
                 animate-gradient-x"
    >
      <div className="bg-white/95 backdrop-blur-lg p-12 rounded-2xl shadow-2xl w-full max-w-lg transform scale-105">
        
        {/* Title */}
        <h1 className="text-3xl font-extrabold text-center text-gray-800 mb-4 flex items-center justify-center space-x-2">
          <span>Welcome Back</span>
          <span className="animate-waving-hand">👋</span>
        </h1>

        {/* Subtitle */}
        <p className="text-center text-gray-600 mb-10">
          Login to continue your journey with{" "}
          <span
            onClick={() => navigate("/")}
            className="font-bold text-cyan-600 hover:bg-gradient-to-r hover:from-teal-500 
                       hover:via-cyan-500 hover:to-blue-600 hover:bg-clip-text hover:text-transparent 
                       transition duration-500 cursor-pointer"
          >
            ResQNet
          </span>.
        </p>

        {/* Login Form */}
        <form className="space-y-8" onSubmit={handleSubmit}>
          {error && <p className="text-red-500 text-sm">{error}</p>}

          {/* Email */}
          <div>
            <label
              htmlFor="email"
              className="block text-sm font-semibold text-gray-700 mb-2"
            >
              Email Address
            </label>
            <input
              type="email"
              id="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-3 rounded-lg border border-gray-300 
                         focus:ring-2 focus:ring-cyan-500 focus:outline-none
                         shadow-inner text-gray-800"
              required
            />
          </div>

          {/* Password */}
          <div>
            <label
              htmlFor="password"
              className="block text-sm font-semibold text-gray-700 mb-2"
            >
              Password
            </label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                id="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-3 rounded-lg border border-gray-300 
                           focus:ring-2 focus:ring-cyan-500 focus:outline-none
                           shadow-inner text-gray-800 pr-12"
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute inset-y-0 right-4 flex items-center text-gray-500 hover:text-gray-700"
              >
                {showPassword ? <FiEyeOff size={20} /> : <FiEye size={20} />}
              </button>
            </div>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading}
            className={`w-full py-3 text-lg font-semibold rounded-lg 
                       bg-gradient-to-r from-teal-500 via-cyan-500 to-blue-600 text-white
                       transition-all shadow-md
                       ${loading ? "opacity-60 cursor-not-allowed" : "hover:scale-[1.03] hover:shadow-2xl"}`}
          >
            {loading ? "Logging in..." : "Login"}
          </button>
        </form>

        {/* Footer */}
        <p className="text-center text-sm text-gray-600 mt-8">
          Don’t have an account?{" "}
          <span
            onClick={() => navigate("/register")}
            className="font-semibold text-cyan-600 hover:underline cursor-pointer"
          >
            Register here
          </span>
        </p>
      </div>

      {/* Hand wave animation */}
      <style>{`
        .animate-waving-hand {
          animation: wave 2s infinite;
          transform-origin: 70% 70%;
          display: inline-block;
        }
        @keyframes wave {
          0% { transform: rotate(0deg); }
          15% { transform: rotate(14deg); }
          30% { transform: rotate(-8deg); }
          40% { transform: rotate(14deg); }
          50% { transform: rotate(-4deg); }
          60% { transform: rotate(10deg); }
          70% { transform: rotate(0deg); }
          100% { transform: rotate(0deg); }
        }
      `}</style>
    </div>
  );
}

export default Login;
