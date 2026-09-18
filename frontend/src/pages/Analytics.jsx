import { useEffect, useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
} from "recharts";

import api from "../services/api";
import "./Analytics.css";
import BackToDashboard from "../components/BackToDashboard";
import Navbar from "../components/Navbar";

function Analytics() {
  const [monthlyData, setMonthlyData] = useState([]);
  const [categoryData, setCategoryData] = useState([]);
  const [monthlySummary, setMonthlySummary] = useState(null);
  const [highestCategory, setHighestCategory] = useState("");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const [
        monthlyResponse,
        categoryResponse,
        summaryResponse,
        highestCategoryResponse,
      ] = await Promise.all([
        api.get("/api/expenses/monthly-history"),
        api.get("/api/expenses/category-summary"),
        api.get("/api/expenses/monthly-summary"),
        api.get("/api/expenses/highest-category"),
      ]);

      // Sort chronologically ("YYYY-MM" strings sort correctly lexicographically)
      // so the line chart doesn't zig-zag when the backend returns months
      // out of order.
      setMonthlyData(
        Object.entries(monthlyResponse.data)
          .map(([month, amount]) => ({
            month,
            amount: Number(amount),
          }))
          .sort((a, b) => a.month.localeCompare(b.month))
      );

      setCategoryData(
        Object.entries(categoryResponse.data).map(
          ([category, amount]) => ({
            category,
            amount: Number(amount),
          })
        )
      );

      setMonthlySummary(summaryResponse.data);
      setHighestCategory(highestCategoryResponse.data);
    } catch (err) {
      console.error("Failed to load analytics:", err);
      setError("Failed to load analytics data.");
    } finally {
      setLoading(false);
    }
  };

  const totalMonthlySpending =
    monthlySummary?.totalSpending ?? 0;

  const averageMonthlySpending =
    monthlyData.length > 0
      ? monthlyData.reduce(
          (sum, item) => sum + Number(item.amount),
          0
        ) / monthlyData.length
      : 0;

  return (
    <div className="analytics-page">
      <div className="analytics-container">

        {/* Header */}

        <div className="analytics-header">
          <BackToDashboard />

          <div>
            <h1>Analytics</h1>
            <p>
              A visual overview of your spending habits.
            </p>
          </div>
        </div>

        {/* Loading */}

        {loading && (
          <div className="analytics-message">
            Loading analytics...
          </div>
        )}

        {/* Error */}

        {error && (
          <div className="analytics-message error">
            {error}
          </div>
        )}

        {!loading && !error && (
          <>
            {/* Summary Cards */}

            <div className="summary-grid">

              <div className="summary-card purple-summary">
                <div className="summary-icon">
                  💰
                </div>

                <div>
                  <p>This Month</p>

                  <h3>
                    ₹{Number(totalMonthlySpending).toFixed(2)}
                  </h3>
                </div>
              </div>

              <div className="summary-card blue-summary">
                <div className="summary-icon">
                  🏆
                </div>

                <div>
                  <p>Highest Category</p>

                  <h3>
                    {highestCategory || "No data"}
                  </h3>
                </div>
              </div>

              <div className="summary-card green-summary">
                <div className="summary-icon">
                  📊
                </div>

                <div>
                  <p>Monthly Average</p>

                  <h3>
                    ₹{averageMonthlySpending.toFixed(2)}
                  </h3>
                </div>
              </div>

            </div>

            {/* Charts */}

            <div className="charts-grid">

              {/* Monthly Spending */}

              <div className="analytics-card monthly-card">

                <div className="card-heading">
                  <div className="icon-box purple">
                    📈
                  </div>

                  <div>
                    <h2>Monthly Spending</h2>

                    <p>
                      Your spending trend over time
                    </p>
                  </div>
                </div>

                {monthlyData.length === 0 ? (
                  <p className="empty-message">
                    No monthly spending data available.
                  </p>
                ) : (
                  <div className="chart-container">
                    <ResponsiveContainer
                      width="100%"
                      height={280}
                    >
                      <LineChart
                        data={monthlyData}
                        margin={{
                          top: 15,
                          right: 20,
                          left: 10,
                          bottom: 10,
                        }}
                      >
                        <CartesianGrid
                          strokeDasharray="3 3"
                          opacity={0.2}
                        />

                        <XAxis
                          dataKey="month"
                          tick={{ fontSize: 11 }}
                        />

                        <YAxis
                          tick={{ fontSize: 11 }}
                        />

                        <Tooltip
                          formatter={(value) => [
                            `₹${Number(value).toFixed(2)}`,
                            "Spending",
                          ]}
                        />

                        <Line
                          type="linear"
                          dataKey="amount"
                          stroke="#2563eb"
                          strokeWidth={5}
                          strokeOpacity={1}
                          dot={{
                            r: 6,
                            fill: "#2563eb",
                            stroke: "#ffffff",
                            strokeWidth: 2,
                          }}
                          activeDot={{
                            r: 8,
                            fill: "#1d4ed8",
                            stroke: "#ffffff",
                            strokeWidth: 2,
                          }}
                          isAnimationActive={false}
                          connectNulls={true}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                )}

              </div>

              {/* Category Spending */}

              <div className="analytics-card category-card">

                <div className="card-heading">
                  <div className="icon-box blue">
                    📊
                  </div>

                  <div>
                    <h2>Spending by Category</h2>

                    <p>
                      Where your money goes this month
                    </p>
                  </div>
                </div>

                {categoryData.length === 0 ? (
                  <p className="empty-message">
                    No category spending data available.
                  </p>
                ) : (
                  <div className="chart-container">
                    <ResponsiveContainer
                      width="100%"
                      height={280}
                    >
                      <BarChart
                        data={categoryData}
                        layout="vertical"
                        margin={{
                          top: 5,
                          right: 20,
                          left: 10,
                          bottom: 5,
                        }}
                      >
                        <CartesianGrid
                          strokeDasharray="3 3"
                          opacity={0.25}
                        />

                        <XAxis
                          type="number"
                          tick={{ fontSize: 10 }}
                        />

                        <YAxis
                          type="category"
                          dataKey="category"
                          width={120}
                          tick={{ fontSize: 11 }}
                        />

                        <Tooltip
                          formatter={(value) => [
                            `₹${Number(value).toFixed(2)}`,
                            "Spending",
                          ]}
                        />

                        <Bar
                          dataKey="amount"
                          fill="#2563eb"
                          radius={[0, 6, 6, 0]}
                          barSize={22}
                          isAnimationActive={false}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                )}

              </div>

            </div>
          </>
                )}

      </div>

      <Navbar />

    </div>
  );
}

export default Analytics;