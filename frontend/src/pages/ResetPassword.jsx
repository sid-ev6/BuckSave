import { useState } from "react";
import { Link, useSearchParams, useNavigate } from "react-router-dom";
import api from "../services/api";
import "./ResetPassword.css";

function ResetPassword() {

  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const token = searchParams.get("token");

  const handleSubmit = async (e) => {
    e.preventDefault();

    setMessage("");
    setError("");

    if (!token) {
      setError("Invalid or missing reset token.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    if (newPassword.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }

    setLoading(true);

    try {

      const response = await api.post(
        "/api/auth/reset-password",
        {
          token: token,
          newPassword: newPassword
        }
      );

      setMessage(response.data);

      setNewPassword("");
      setConfirmPassword("");

      setTimeout(() => {
        navigate("/login");
      }, 2000);

    } catch (error) {

      setError(
        error.response?.data?.message ||
        error.response?.data ||
        "Unable to reset password."
      );

    } finally {

      setLoading(false);

    }
  };

  return (
    <div className="reset-password-page">

      <div className="reset-password-card">

        <h1>Reset Password</h1>

        <p>
          Enter your new password below.
        </p>

        <form onSubmit={handleSubmit}>

          <input
            type="password"
            placeholder="New password"
            value={newPassword}
            onChange={(e) =>
              setNewPassword(e.target.value)
            }
            required
          />

          <input
            type="password"
            placeholder="Confirm new password"
            value={confirmPassword}
            onChange={(e) =>
              setConfirmPassword(e.target.value)
            }
            required
          />

          <button
            type="submit"
            disabled={loading}
          >
            {loading
              ? "Resetting..."
              : "Reset Password"}
          </button>

        </form>

        {message && (
          <p className="success-message">
            {message}
            <br />
            Redirecting to login...
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

export default ResetPassword;