import { NavLink } from "react-router-dom";
import "./Navbar.css";

function Navbar() {
  return (
    <nav className="dashboard-nav">

      <NavLink to="/dashboard">
        Dashboard
      </NavLink>

      <NavLink to="/expenses">
        Expenses
      </NavLink>

      <NavLink to="/spending-limit">
        Limits
      </NavLink>

      <NavLink to="/analytics">
        Analytics
      </NavLink>

      <NavLink to="/profile">
        Profile
      </NavLink>

    </nav>
  );
}

export default Navbar;