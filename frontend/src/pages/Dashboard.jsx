import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../services/api";
import "./Dashboard.css";


function Dashboard() {

    const navigate = useNavigate();
 
  const [dashboardData, setDashboardData] = useState(null);
  const [budgetStatus, setBudgetStatus] = useState(null);
  const [userName, setUserName] = useState("");

 useEffect(() => {
  const loadDashboard = async () => {
    try {
      const response = await api.get("/api/dashboard");

      console.log("Dashboard response:", response.data);

      setDashboardData(response.data);

      setUserName(
        response.data.userName ||
        response.data.name ||
        ""
      );

      const budgetResponse =
        await api.get("/api/spending-limits/budget-status");

      console.log(
        "Budget status:",
        budgetResponse.data
      );

      setBudgetStatus(budgetResponse.data);

    } catch (error) {
      console.error(
        "Dashboard API failed:",
        error
      );
    }
  };

  loadDashboard();
}, []);
  return (
    <div className="dashboard-page">

      <header className="dashboard-header">
  <div>
    <h1>Hi, {userName}</h1>
    <p>Here's your financial overview.</p>
  </div>

 
        <Link to="/expenses/add" className="add-expense-button">
          + Add Expense
        </Link>
      </header>

      <section className="dashboard-cards">

        
        <div className="dashboard-card">
          <p>Monthly Spending</p>
          <h2>₹{dashboardData?.monthlySpending ?? 0}</h2>
        </div>

        <div className="dashboard-card">
          <p>Today's Spending</p>
          <h2>₹{dashboardData?.todaySpending ?? 0}</h2>
        </div>

        <div className="dashboard-card budget-card">

  {budgetStatus?.message ? (

    <>
      <div className="budget-card-header">
        <p>
          {new Date().toLocaleString("en-US", {
            month: "long"
          })} Limit
        </p>
      </div>

      <h2>No limit set</h2>

      <p className="budget-remaining-text">
        Set a monthly limit to track your budget.
      </p>

      <Link
        to="/spending-limit"
        className="add-limit-button"
      >
        + Add{" "}
        {new Date().toLocaleString("en-US", {
          month: "long"
        })} Limit
      </Link>
    </>

  ) : (

    <>
      <div className="budget-card-header">

        <p>
          {new Date().toLocaleString("en-US", {
            month: "long"
          })} Limit
        </p>

        <Link to="/spending-limit">
          Manage
        </Link>

      </div>

      <h2>
        ₹{budgetStatus?.remaining ?? 0}
      </h2>

      <p
        className={
          budgetStatus?.exceeded
            ? "budget-remaining-text budget-card-exceeded"
            : "budget-remaining-text"
        }
      >
        {budgetStatus?.exceeded
          ? "Limit exceeded"
          : "remaining"}
      </p>

      <div
        className={`budget-progress-bar ${
          budgetStatus?.exceeded
            ? "budget-progress-exceeded"
            : ""
        }`}
        style={{
          width: `${
            budgetStatus?.limit
              ? Math.min(
                  (budgetStatus.spent /
                    budgetStatus.limit) *
                    100,
                  100
                )
              : 0
          }%`
        }}
      />

      <div className="budget-card-footer">

        <span>
          Spent
          <strong>
            ₹{budgetStatus?.spent ?? 0}
          </strong>
        </span>

        <span>
          Limit
          <strong>
            ₹{budgetStatus?.limit ?? 0}
          </strong>
        </span>

      </div>
    </>

  )}

</div>

      </section>

      <section className="dashboard-content">

        <div className="dashboard-panel">
          <div className="panel-header">
            <h2>Recent Expenses</h2>
            <Link to="/expenses">View all</Link>
          </div>

          {dashboardData?.recentExpenses?.map((expense) => (
  <div className="expense-row" key={expense.id}>
    <div>
      <strong>{expense.categoryName}</strong>
      <span>{expense.description}</span>
    </div>

    <strong>− ₹{expense.amount}</strong>
  </div>
))}

        </div>
<div className="dashboard-panel">
  <h2>Top Spending Categories — This Month</h2>

  {dashboardData?.categorySpending &&
  Object.entries(dashboardData.categorySpending).length > 0 ? (
    Object.entries(dashboardData.categorySpending)
      .sort(([, amountA], [, amountB]) => amountB - amountA)
      .slice(0, 5)
      .map(([category, amount]) => (
        <div className="category-row" key={category}>
          <span>{category}</span>
          <strong>₹{amount}</strong>
        </div>
      ))
  ) : (
    <p>No spending recorded this month.</p>
  )}
</div>

      </section>

      <nav className="dashboard-nav">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/expenses">Expenses</Link>
        <Link
  to="/spending-limit?addMonthly=true"
  className="add-limit-button"
>Limits</Link>
        <Link to="/analytics">Analytics</Link>
        <Link to="/profile">Profile</Link>
      </nav>

    </div>
  );
}

export default Dashboard;