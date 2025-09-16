import { createContext, useContext, useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import axios from "axios";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const parseUserFromToken = (jwt) => {
    try {
      const decoded = jwtDecode(jwt);

      // Role field may vary depending on backend
      let role =
        decoded.role ||
        decoded.roles?.[0] || // some JWTs use "roles" array
        decoded.authorities?.[0] || // Spring Security default
        null;

      return { email: decoded.sub, role };
    } catch (err) {
      console.error("Failed to decode token", err);
      return null;
    }
  };

  useEffect(() => {
    if (token) {
      const userObj = parseUserFromToken(token);

      if (!userObj) {
        logout();
      } else if (jwtDecode(token).exp * 1000 < Date.now()) {
        // expired
        logout();
      } else {
        setUser(userObj);
        axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
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

    const userObj = parseUserFromToken(newToken);
    setUser(userObj);

    axios.defaults.headers.common["Authorization"] = `Bearer ${newToken}`;
    return userObj; // return user info for role-based redirect
  };

  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
    delete axios.defaults.headers.common["Authorization"];

    // Safe: no dependency on React Router
    window.location.href = "/login";
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
