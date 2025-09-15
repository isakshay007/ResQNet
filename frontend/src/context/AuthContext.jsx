// src/context/AuthContext.js
import { createContext, useContext, useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import axios from "axios";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      try {
        const decoded = jwtDecode(token);

        // ⏳ Token expiry check
        if (decoded.exp && decoded.exp * 1000 < Date.now()) {
          logout(); // will auto redirect now
        } else {
          setUser({ email: decoded.sub, role: decoded.role });
          axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
        }
      } catch (err) {
        console.error("Invalid token", err);
        logout(); // will auto redirect now
      }
    } else {
      setUser(null);
      delete axios.defaults.headers.common["Authorization"];
    }
    setLoading(false);
  }, [token]);

  const login = (newToken) => {
    localStorage.setItem("token", newToken);
    setToken(newToken);
    try {
      const decoded = jwtDecode(newToken);
      setUser({ email: decoded.sub, role: decoded.role });
    } catch (err) {
      console.error("Failed to decode token", err);
      setUser(null);
    }
    axios.defaults.headers.common["Authorization"] = `Bearer ${newToken}`;
  };

  //  Always redirects to /login
  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
    delete axios.defaults.headers.common["Authorization"];

    window.location.href = "/login"; // force reload + redirect
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        login,
        logout,
        isAuthenticated: !!token && !!user,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
