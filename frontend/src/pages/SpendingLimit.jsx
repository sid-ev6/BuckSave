import "./SpendingLimit.css";
import { useEffect, useState } from "react";
import {
  useNavigate,
  useSearchParams
} from "react-router-dom";
import api from "../services/api";
import BackToDashboard from "../components/BackToDashboard";

import Navbar from "../components/Navbar";

function SpendingLimit() {

  const navigate = useNavigate();

  const [searchParams] = useSearchParams();

  const addMonthly =
    searchParams.get("addMonthly") === "true";


  const [amount, setAmount] =
    useState("");

  const [limitType, setLimitType] =
    useState("MONTHLY");

  const [limits, setLimits] =
    useState([]);

  const [editingId, setEditingId] =
    useState(null);


  const [monthFilter, setMonthFilter] =
    useState("");

  const [typeFilter, setTypeFilter] =
    useState("");


  // ==========================================
  // LOAD SPENDING LIMITS
  // ==========================================

  const fetchLimits = async () => {

    try {

      const response =
        await api.get("/api/spending-limits");

      const currentDate =
        new Date();

      const currentMonth =
        currentDate
          .toLocaleString("en-US", {
            month: "long"
          })
          .toUpperCase();

      const currentYear =
        currentDate.getFullYear();


      // Put current month's monthly limit first
      const sortedLimits =
        [...response.data].sort((a, b) => {

          const aIsCurrentMonthly =
            a.limitType === "MONTHLY" &&
            a.month === currentMonth &&
            a.year === currentYear;

          const bIsCurrentMonthly =
            b.limitType === "MONTHLY" &&
            b.month === currentMonth &&
            b.year === currentYear;


          if (
            aIsCurrentMonthly &&
            !bIsCurrentMonthly
          ) {
            return -1;
          }

          if (
            !aIsCurrentMonthly &&
            bIsCurrentMonthly
          ) {
            return 1;
          }

          return 0;
        });


      setLimits(sortedLimits);

    } catch (error) {

      console.error(
        "Failed to load spending limits:",
        error
      );

    }
  };


  useEffect(() => {

    fetchLimits();

  }, []);


  // ==========================================
  // FILTER LIMITS
  // ==========================================

  const currentYear =
    new Date().getFullYear();


  const filteredLimits =
    limits.filter((limit) => {

      const matchesType =
        !typeFilter ||
        limit.limitType === typeFilter;


      const matchesMonth =
        !monthFilter ||
        (
          limit.month === monthFilter &&
          limit.year === currentYear
        );


      return (
        matchesType &&
        matchesMonth
      );
    });


  // ==========================================
  // DELETE LIMIT
  // ==========================================

  const handleDelete = async (id) => {

    const confirmed =
      window.confirm(
        "Are you sure you want to delete this spending limit?"
      );

    if (!confirmed) {
      return;
    }


    try {

      await api.delete(
        `/api/spending-limits/${id}`
      );

      fetchLimits();

    } catch (error) {

      console.error(
        "Failed to delete spending limit:",
        error
      );

    }
  };


  // ==========================================
  // CREATE / UPDATE LIMIT
  // ==========================================

  const handleSubmit = async (event) => {

  event.preventDefault();

  try {

    if (editingId) {

      // UPDATE → only amount
      await api.put(
        `/api/spending-limits/${editingId}`,
        {
          amount: Number(amount)
        }
      );

    } else {

      // CREATE → amount + limit type
      await api.post(
        "/api/spending-limits",
        {
          amount: Number(amount),
          limitType: limitType
        }
      );

    }

    // Clear form
    setAmount("");
    setLimitType("MONTHLY");
    setEditingId(null);

    // Reload limits
    fetchLimits();

  } catch (error) {

    console.error(
      "Failed to save spending limit:",
      error
    );

    console.error(
      "Server response:",
      error.response?.data
    );
  }
};


  // ==========================================
  // EDIT LIMIT
  // ==========================================

  const handleEdit = (limit) => {

    setEditingId(limit.id);

    setAmount(limit.amount);

    setLimitType(limit.limitType);
  };


  // ==========================================
  // CANCEL EDIT
  // ==========================================

  const handleCancelEdit = () => {

    setEditingId(null);

    setAmount("");

    setLimitType("MONTHLY");
  };


  // ==========================================
  // JSX
  // ==========================================

  return (

    <div className="spending-limit-page">

      <div className="spending-limit-container">


        {/* BACK TO DASHBOARD */}

        <BackToDashboard />


        {/* PAGE TITLE */}

        <h1 className="spending-limit-title">

          {addMonthly

            ? `Set ${new Date().toLocaleString(
                "en-US",
                {
                  month: "long"
                }
              )} Monthly Limit`

            : "Spending Limits"}

        </h1>


        {/* CREATE / UPDATE LIMIT */}

        <div className="spending-limit-form-card">

          <h2>

            {editingId
              ? "Update Spending Limit"
              : "Set Spending Limit"}

          </h2>


          <form
            onSubmit={handleSubmit}
            className="spending-limit-form"
          >


            {/* AMOUNT */}

            <div className="spending-limit-field">

              <label>
                Limit Amount
              </label>

              <input
                type="number"
                placeholder="Enter spending limit"
                min="0"
                step="0.01"
                value={amount}
                onChange={(event) =>
                  setAmount(
                    event.target.value
                  )
                }
                required
              />

            </div>


            {/* LIMIT TYPE */}

<div className="spending-limit-field">

  <label>
    Limit Type
  </label>

  {editingId ? (

    // EDIT → type is read-only
    <input
      type="text"
      value={limitType}
      readOnly
    />

  ) : (

    // CREATE → type can be selected
    <select
      value={limitType}
      onChange={(event) =>
        setLimitType(event.target.value)
      }
      required
    >

      <option value="DAILY">
        Daily
      </option>

      <option value="WEEKLY">
        Weekly
      </option>

      <option value="MONTHLY">
        Monthly
      </option>

    </select>

  )}

</div>

            {/* SUBMIT */}

            <button
              type="submit"
              className="spending-limit-submit"
            >

              {editingId
                ? "Update Limit"
                : "Save Limit"}

            </button>


           {/* CANCEL */}

{editingId && (
  <div className="cancel-button-container">
    <button
      type="button"
      className="spending-limit-cancel"
      onClick={handleCancelEdit}
    >
      Cancel
    </button>
  </div>
)}

          </form>

        </div>


        {/* EXISTING LIMITS */}

        <h2 className="spending-limit-list-title">

          My Spending Limits

        </h2>


        {/* FILTERS */}

        <div className="spending-limit-filters">


          {/* MONTH FILTER */}

          <div className="spending-limit-filter">

            <label>
              Month
            </label>

            <select
              value={monthFilter}
              onChange={(event) =>
                setMonthFilter(
                  event.target.value
                )
              }
            >

              <option value="">
                All Months
              </option>

              <option value="JANUARY">
                January
              </option>

              <option value="FEBRUARY">
                February
              </option>

              <option value="MARCH">
                March
              </option>

              <option value="APRIL">
                April
              </option>

              <option value="MAY">
                May
              </option>

              <option value="JUNE">
                June
              </option>

              <option value="JULY">
                July
              </option>

              <option value="AUGUST">
                August
              </option>

              <option value="SEPTEMBER">
                September
              </option>

              <option value="OCTOBER">
                October
              </option>

              <option value="NOVEMBER">
                November
              </option>

              <option value="DECEMBER">
                December
              </option>

            </select>

          </div>


          {/* TYPE FILTER */}

          <div className="spending-limit-filter">

            <label>
              Limit Type
            </label>

            <select
              value={typeFilter}
              onChange={(event) =>
                setTypeFilter(
                  event.target.value
                )
              }
            >

              <option value="">
                All Types
              </option>

              <option value="DAILY">
                Daily
              </option>

              <option value="WEEKLY">
                Weekly
              </option>

              <option value="MONTHLY">
                Monthly
              </option>

            </select>

          </div>

        </div>


        {/* LIMIT LIST */}

        {filteredLimits.length === 0 ? (

          <div className="spending-limit-empty">

            <p>

              {limits.length === 0
                ? "No spending limits found."
                : "No spending limits match your filters."}

            </p>

          </div>

        ) : (

          <div className="spending-limit-list">


            {filteredLimits.map((limit) => (

              <div
                key={limit.id}
                className="spending-limit-card"
              >


                {/* AMOUNT */}

                <p className="spending-limit-card-amount">

                  ₹{limit.amount}

                </p>


                {/* CURRENT MONTH BADGE */}

                {limit.limitType === "MONTHLY" &&

                  limit.month ===
                    new Date()
                      .toLocaleString(
                        "en-US",
                        {
                          month: "long"
                        }
                      )
                      .toUpperCase() &&

                  limit.year ===
                    new Date().getFullYear() && (

                    <span className="current-month-badge">

                      Current Month

                    </span>

                  )}


                {/* TYPE */}

                <p>

                  <strong>
                    Type:
                  </strong>{" "}

                  {limit.limitType}

                </p>


                {/* MONTH */}

                <p>

                  <strong>
                    Month:
                  </strong>{" "}

                  {limit.month
                    ? limit.month.charAt(0) +
                      limit.month
                        .slice(1)
                        .toLowerCase()
                    : "—"}

                </p>


                {/* YEAR */}

                <p>

                  <strong>
                    Year:
                  </strong>{" "}

                  {limit.year || "—"}

                </p>


                {/* VIEW EXPENSES */}

            <button
  type="button"
  className="view-expenses-button"
  onClick={() =>
    navigate(
      `/expenses?spendingLimitId=${limit.id}`
    )
  }
>
  View Expenses →
</button>


                {/* ACTIONS */}

                <div className="spending-limit-actions">


                  <button
                    type="button"
                    className="spending-limit-edit"
                    onClick={() =>
                      handleEdit(limit)
                    }
                  >

                    Edit

                  </button>


                  <button
                    type="button"
                    className="spending-limit-delete"
                    onClick={() =>
                      handleDelete(
                        limit.id
                      )
                    }
                  >

                    Delete

                  </button>


                </div>

              </div>

            ))}

          </div>

             )}

      </div>

      <Navbar />

    </div>

  );
}

export default SpendingLimit;