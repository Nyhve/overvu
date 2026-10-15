import { Route, Routes } from "react-router-dom";
import { ProtectedRoute } from "./routes/ProtectedRoutes";
import { Layout } from "./routes/Layout";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import GoalsPage from "./pages/GoalsPage";
import TasksPage from "./pages/TasksPage";
import HabitsPage from "./pages/HabitsPage.tsx";
import FitnessPage from "./pages/FitnessPage.tsx";
import LearningPage from "./pages/LearningPage.tsx";
import FinancePage from "./pages/FinancePage.tsx";
import AnalyticsPage from "./pages/AnalyticsPage.tsx";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/goals" element={<GoalsPage />} />
          <Route path="/tasks" element={<TasksPage />} />
          <Route path="/habits" element={<HabitsPage />} />
          <Route path="/fitness" element={<FitnessPage />} />
          <Route path="/learning" element={<LearningPage />} />
          <Route path="/finance" element={<FinancePage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;