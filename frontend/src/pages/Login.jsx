import { useState } from "react";
import {
  Link,
  useNavigate
} from "react-router-dom";
import api from "../services/api";
import "./Login.css";

function Login() {
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const navigate = useNavigate();

  const handleLogin = async (event) => {
    event.preventDefault();

    setError("");

    try {
      const response = await api.post("/api/auth/login", {
        login: login,
        password: password,
      });

      console.log("Login successful:", response.data);

      localStorage.setItem(
        "accessToken",
        response.data.accessToken
      );

      localStorage.setItem(
        "refreshToken",
        response.data.refreshToken
      );

      navigate("/dashboard");

    } catch (error) {
      console.error("Login failed:", error);

      setError("Invalid username/email or password.");
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">

        <div className="login-logo">
          BuckSave
        </div>

        <p className="login-subtitle">
          Manage your money smarter.
        </p>

        <form
          className="login-form"
          onSubmit={handleLogin}
        >

          <div className="login-field">
            <label>Email or Username</label>

            <input
              type="text"
              placeholder="Enter email or username"
              value={login}
              onChange={(event) => setLogin(event.target.value)}
              required
            />
          </div>

          <div className="login-field">
            <label>Password</label>

            <input
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </div>

          {error && (
            <p style={{ color: "red", fontSize: "14px" }}>
              {error}
            </p>
          )}

          <button
            type="submit"
            className="login-button"
          >
            Login
          </button>
          <Link
  to="/forgot-password"
  className="forgot-password-link"
>
  Forgot Password?
</Link>

        </form>

        <p className="login-register">
          Don't have an account?{" "}
          <Link to="/register">Register</Link>
        </p>

      </div>
    </div>
  );
}

export default Login;