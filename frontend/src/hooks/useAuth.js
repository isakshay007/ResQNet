
import { useState } from "react";
import { jwtDecode } from "jwt-decode";

export function useAuth() {
  const [token, setToken] = useState(() => localStorage.getItem("token") || null);

  const [user, setUser] = useState(() => {
    const savedToken = localStorage.getItem("token");
    if (savedToken) {
      try {
        const decoded = jwtDecode(savedToken);
        if (decoded.exp && decoded.exp * 1000 < Date.now()) {
          localStorage.removeItem("token");
          return null;
        }
        return { email: decoded.sub, role: decoded.role };
      } catch (err) {
        console.error("Invalid token", err);
      }
    }
    return null;
  });

  const login = (token) => {
    localStorage.setItem("token", token);
    setToken(token);
    try {
      const decoded = jwtDecode(token);
      setUser({ email: decoded.sub, role: decoded.role });
    } catch (err) {
      console.error("Failed to decode token", err);
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
  };

  return { token, user, login, logout, isAuthenticated: !!user };
}
