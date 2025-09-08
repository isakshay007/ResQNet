import React, { useState } from "react";
import api from "../utils/api";
import { useNavigate, Link } from "react-router-dom";
import { FiEye, FiEyeOff } from "react-icons/fi";

function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [role, setRole] = useState("REPORTER");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    try {
      //  Call correct backend endpoint for self-registration
      await api.post("/auth/register", { name, email, password, role });

      // Redirect to login after success
      navigate("/login");
    } catch (err) {
      console.error("Register error:", err);
      setError("Registration failed. Try again.");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-teal-400 via-cyan-400 to-blue-500 text-white animate-gradient-x">
      <div className="bg-white/95 backdrop-blur-lg p-10 rounded-2xl shadow-2xl w-full max-w-md">
        
        {/* Title */}
        <h1 className="text-3xl font-extrabold text-center text-gray-800 mb-4">
          Create Account ✨
        </h1>

        {/* Register Form */}
        <form className="space-y-6" onSubmit={handleSubmit}>
          {error && <p className="text-red-500 text-sm">{error}</p>}

          {/* Name */}
          <input
            type="text"
            placeholder="Full Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full px-4 py-3 rounded-lg border border-gray-300 
                       focus:ring-2 focus:ring-cyan-500 focus:outline-none 
                       text-base text-gray-800 shadow-inner"
            required
          />

          {/* Email */}
          <input
            type="email"
            placeholder="Email Address"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full px-4 py-3 rounded-lg border border-gray-300 
                       focus:ring-2 focus:ring-cyan-500 focus:outline-none 
                       text-base text-gray-800 shadow-inner"
            required
          />

          {/* Password */}
          <div className="relative">
            <input
              type={showPassword ? "text" : "password"}
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-3 rounded-lg border border-gray-300 
                         focus:ring-2 focus:ring-cyan-500 focus:outline-none 
                         text-base text-gray-800 pr-12 shadow-inner"
              required
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute inset-y-0 right-3 flex items-center text-gray-500 hover:text-gray-700"
            >
              {showPassword ? <FiEyeOff size={20} /> : <FiEye size={20} />}
            </button>
          </div>

          {/* Confirm Password */}
          <div className="relative">
            <input
              type={showConfirmPassword ? "text" : "password"}
              placeholder="Confirm Password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="w-full px-4 py-3 rounded-lg border border-gray-300 
                         focus:ring-2 focus:ring-cyan-500 focus:outline-none 
                         text-base text-gray-800 pr-12 shadow-inner"
              required
            />
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute inset-y-0 right-3 flex items-center text-gray-500 hover:text-gray-700"
            >
              {showConfirmPassword ? <FiEyeOff size={20} /> : <FiEye size={20} />}
            </button>
          </div>

          {/* Role */}
          <select
            value={role}
            onChange={(e) => setRole(e.target.value)}
            className="w-full px-4 py-3 rounded-lg border border-gray-300 
                       focus:ring-2 focus:ring-cyan-500 focus:outline-none 
                       text-base text-gray-800 shadow-inner"
          >
            <option value="REPORTER">Reporter</option>
            <option value="RESPONDER">Responder</option>
          </select>

          {/* Register Button */}
          <button
            type="submit"
            className="w-full py-3 text-lg font-semibold rounded-lg 
                       bg-gradient-to-r from-teal-500 via-cyan-500 to-blue-600 text-white
                       hover:scale-[1.03] hover:shadow-2xl transition-all shadow-md"
          >
            Register
          </button>
        </form>

        {/* Footer */}
        <p className="text-center text-sm text-gray-600 mt-6">
          Already have an account?{" "}
          <Link
            to="/login"
            className="font-semibold text-cyan-600 hover:underline"
          >
            Login here
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Register;
