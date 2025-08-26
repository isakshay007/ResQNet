import axios from "axios";

// Base Axios instance
const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// Interceptor: inject JWT if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token"); // always up-to-date
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
