import { Link } from "react-router-dom";
import "./BackToDashboard.css";

function BackToDashboard() {
  return (
    <Link
      to="/dashboard"
      className="back-to-dashboard"
    >
      ← Back 
    </Link>
  );
}

export default BackToDashboard;