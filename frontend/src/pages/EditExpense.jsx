import { useEffect, useState } from "react";
import {
  useNavigate,
  useParams,
  useSearchParams
} from "react-router-dom";
import api from "../services/api";
import "./AddExpense.css";

function EditExpense() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

const spendingLimitIdFromUrl =
  searchParams.get("spendingLimitId");

  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [date, setDate] = useState("");
  const [categoryId, setCategoryId] = useState("");
const [spendingLimitId, setSpendingLimitId] = useState(null);
const [categories, setCategories] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadExpense = async () => {
      try {
        const expenseResponse = await api.get(
          `/api/expenses/${id}`
        );

        const expense = expenseResponse.data;

      setAmount(expense.amount);
setDescription(expense.description);
setDate(expense.date);
setCategoryId(expense.categoryId);
setSpendingLimitId(expense.spendingLimitId);

        const categoriesResponse = await api.get(
          "/api/categories"
        );

        setCategories(categoriesResponse.data);

      } catch (error) {
        console.error(
          "Failed to load expense:",
          error
        );

        setError("Failed to load expense.");
      } finally {
        setLoading(false);
      }
    };

    loadExpense();
  }, [id]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      await api.put(`/api/expenses/${id}`, {
  amount: Number(amount),
  description: description,
  date: date,
  categoryId: Number(categoryId),
  spendingLimitId: spendingLimitId,
});

      if (spendingLimitIdFromUrl) {
  navigate(
    `/expenses?spendingLimitId=${spendingLimitIdFromUrl}`
  );
} else {
  navigate("/expenses");
}

    } catch (error) {
      console.error(
        "Failed to update expense:",
        error
      );

      setError("Failed to update expense.");
    }
  };

  if (loading) {
    return (
      <div className="add-expense-page">
        <div className="add-expense-container">
          <p>Loading expense...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="add-expense-page">
        <div className="add-expense-container">
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="add-expense-page">

      <div className="add-expense-container">

        <div className="add-expense-header">
          <h1>Edit Expense</h1>

          <p>
            Update the details of your expense.
          </p>
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
                min="0"
                step="0.01"
                value={amount}
                onChange={(event) =>
                  setAmount(event.target.value)
                }
                required
              />
            </div>

            <div className="expense-field">
              <label>Description</label>

              <input
                type="text"
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

            {error && (
              <p style={{ color: "red" }}>
                {error}
              </p>
            )}

            <button
              type="submit"
              className="expense-submit"
            >
              Save Changes
            </button>

          </form>

          <button
            type="button"
            className="expense-cancel"
            onClick={() => navigate("/expenses")}
            style={{
              border: "none",
              background: "none",
              width: "100%",
              cursor: "pointer",
            }}
          >
            Cancel
          </button>

        </div>

      </div>

    </div>
  );
}

export default EditExpense;