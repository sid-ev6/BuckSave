import { useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";
import "./ForgotPassword.css";

function ForgotPassword() {

  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    setMessage("");
    setError("");
    setLoading(true);

    try {

      const response = await api.post(
        "/api/auth/forgot-password",
        { email }
      );

      setMessage(response.data);

    } catch (error) {

      setError(
        error.response?.data?.message ||
        "Unable to process password reset request."
      );

    } finally {

      setLoading(false);

    }
  };

  return (
    <div className="forgot-password-page">

      <div className="forgot-password-card">

        <h1>Forgot Password?</h1>

        <p>
          Enter your email address and we'll help you
          reset your password.
        </p>

        <form onSubmit={handleSubmit}>

          <input
            type="email"
            placeholder="Email address"
            value={email}
            onChange={(e) =>
              setEmail(e.target.value)
            }
            required
          />

          <button
            type="submit"
            disabled={loading}
          >
            {loading
              ? "Sending..."
              : "Reset Password"}
          </button>

        </form>

        {message && (
          <p className="success-message">
            {message}
          </p>
        )}

        {error && (
          <p className="error-message">
            {error}
          </p>
        )}

        <Link to="/login">
          ← Back to Login
        </Link>

      </div>

    </div>
  );
}

export default ForgotPassword;