import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import api from "../services/api";

import "./AddExpense.css";

function AddExpense() {
  const navigate = useNavigate();

  const [searchParams] = useSearchParams();

const spendingLimitId =
  searchParams.get("spendingLimitId");

  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [date, setDate] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [categories, setCategories] = useState([]);

  useEffect(() => {
    api.get("/api/categories")
      .then((response) => {
        console.log("Categories:", response.data);
        setCategories(response.data);
      })
      .catch((error) => {
        console.error("Failed to load categories:", error);
      });
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
     const response = await api.post("/api/expenses", {
  amount: Number(amount),
  description: description,
  date: date,
  categoryId: Number(categoryId),
  spendingLimitId: spendingLimitId
    ? Number(spendingLimitId)
    : null,
});
if (spendingLimitId) {
  navigate(`/expenses?spendingLimitId=${spendingLimitId}`);
} else {
  navigate("/dashboard");
}

      console.log("Expense created:", response.data);

      navigate("/dashboard");
    } catch (error) {
      console.error("Failed to create expense:", error);
    }
  };

  return (
    <div className="add-expense-page">

      <div className="add-expense-container">

        <div className="add-expense-header">
          <h1>Add Expense</h1>
          <p>Record a new expense and keep your spending on track.</p>
        </div>

        <div className="add-expense-card">

          <form
            className="expense-form"
            onSubmit={handleSubmit}
          >

            <div className="expense-field">
  <label>Amount</label>

  <input
    type="number"
    placeholder="Enter amount"
    min="0"
    step="0.01"
    value={amount}
    onChange={(event) => setAmount(event.target.value)}
    onKeyDown={(event) => {
      if (["-", "+", "e", "E"].includes(event.key)) {
        event.preventDefault();
      }
    }}
    required
  />

</div>


            <div className="expense-field">
              <label>Description</label>

              <input
                type="text"
                placeholder="e.g. Lunch"
                value={description}
                onChange={(event) =>
                  setDescription(event.target.value)
                }
                required
              />
            </div>

            <div className="expense-field">
              <label>Date</label>

              <input
                type="date"
                value={date}
                onChange={(event) =>
                  setDate(event.target.value)
                }
                required
              />
            </div>

            <div className="expense-field">
              <label>Category</label>

              <select
                value={categoryId}
                onChange={(event) =>
                  setCategoryId(event.target.value)
                }
                required
              >
                <option value="">
                  Select a category
                </option>

                {categories.map((category) => (
                  <option
                    key={category.id}
                    value={category.id}
                  >
                    {category.name}
                  </option>
                ))}
              </select>
            </div>

            <button
              type="submit"
              className="expense-submit"
            >
              Add Expense
            </button>

          </form>

          <Link
            to="/dashboard"
            className="expense-cancel"
          >
            Cancel
          </Link>

        </div>

      </div>

    </div>
  );
}

export default AddExpense;