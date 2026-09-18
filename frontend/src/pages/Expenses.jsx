import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import api from "../services/api";
import "./Expenses.css";
import BackToDashboard from "../components/BackToDashboard";
import { formatDate } from "../utils/dateUtils";

import Navbar from "../components/Navbar";

function Expenses() {

    const [searchParams] = useSearchParams();

const spendingLimitId =
    searchParams.get("spendingLimitId");
  const [expenses, setExpenses] = useState([]);
  const [spendingLimit, setSpendingLimit] = useState(null);
  const [loading, setLoading] = useState(true);
 

  useEffect(() => {


    if (spendingLimitId) {
  api.get(`/api/spending-limits/${spendingLimitId}`)
    .then((response) => {
      setSpendingLimit(response.data);
    })
    .catch((error) => {
      console.error(
        "Failed to load spending limit:",
        error
      );
    });
} else {
  setSpendingLimit(null);
}

  setLoading(true);

  const url = spendingLimitId
    ? `/api/expenses/spending-limit/${spendingLimitId}`
    : "/api/expenses";

 api.get(url)
  .then((response) => {
    console.log("Expenses:", response.data);

    if (spendingLimitId) {
      setExpenses(response.data.expenses || []);
    } else {
      setExpenses(response.data);
    }
  })
    .catch((error) => {
      console.error("Failed to load expenses:", error);
    })
    .finally(() => {
      setLoading(false);
    });

}, [spendingLimitId]);


  const handleDelete = async (id) => {
  const confirmed = window.confirm(
    "Are you sure you want to delete this expense?"
  );

  if (!confirmed) {
    return;
  }

  try {
    await api.delete(`/api/expenses/${id}`);

    setExpenses((currentExpenses) =>
      currentExpenses.filter(
        (expense) => expense.id !== id
      )
    );

  } catch (error) {
    console.error("Failed to delete expense:", error);
  }
};
const sortedExpenses = [...expenses].sort(
  (a, b) =>
    new Date(b.date) - new Date(a.date)
);

  return (
    <div className="expenses-page">

      <div className="expenses-container">

        <div className="expenses-header">

          <div>
            <BackToDashboard />

<h1>
  {spendingLimit
    ? `Expenses for ${
        spendingLimit.month.charAt(0) +
        spendingLimit.month.slice(1).toLowerCase()
      } ${spendingLimit.limitType.toLowerCase()} limit`
    : "Expenses"}
</h1>

<p>
  {spendingLimit
    ? `Limit: ₹${spendingLimit.amount} • ${spendingLimit.limitType}`
    : "Track and manage your spending."}
</p>
          </div>

          <Link
  to={
    spendingLimitId
      ? `/expenses/add?spendingLimitId=${spendingLimitId}`
      : "/expenses/add"
  }
  className="add-expense-button"
>
  + Add Expense
</Link>

        </div>

        <div className="expenses-card">


          {loading ? (
            <p className="expenses-message">
              Loading expenses...
            </p>
          ) : expenses.length === 0 ? (
            <div className="expenses-message">
              <h3>No expenses yet</h3>
              <p>
                Start tracking your spending by adding your first expense.
              </p>

              <Link
  to={
    spendingLimitId
      ? `/expenses/add?spendingLimitId=${spendingLimitId}`
      : "/expenses/add"
  }
  className="add-expense-button"
>
  Add Your First Expense
</Link>
            </div>
          ) : (
            <div className="expenses-list">

              {sortedExpenses.map((expense) => (
                <div
                  className="expense-item"
                  key={expense.id}
                >

                  <div className="expense-info">

                    <div className="expense-category">
                      {expense.categoryName}
                    </div>

                    <div className="expense-description">
                      {expense.description}
                    </div>

                    <div className="expense-date">
  {formatDate(expense.date)}
</div>
                  </div>

                  <div className="expense-actions">
  <div className="expense-amount">
    − ₹{expense.amount}
  </div>

  <Link
  to={
    spendingLimitId
      ? `/expenses/edit/${expense.id}?spendingLimitId=${spendingLimitId}`
      : `/expenses/edit/${expense.id}`
  }
  className="edit-expense-button"
>
  Edit
</Link>

  <button
    className="delete-expense-button"
    onClick={() => handleDelete(expense.id)}
  >
    Delete
  </button>
</div>
                </div>
              ))}

            </div>
          )}

        </div>

            </div>

      <Navbar />

    </div>
  );
}
export default Expenses;