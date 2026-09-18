import { useNavigate } from "react-router-dom";
import "./Profile.css";
import Navbar from "../components/Navbar";

function Profile() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");

    navigate("/login");
  };

  return (
    <div className="profile-page">

      <div className="profile-card">
        <h1>Profile</h1>

        <p>Manage your account.</p>

        <button
          className="logout-button"
          onClick={handleLogout}
        >
          Logout
        </button>
      </div>

      <Navbar />

    </div>
  );
}

export default Profile;