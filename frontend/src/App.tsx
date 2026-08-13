import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import { AppShell } from "./pages/AppShell";
import { DirectoryPage } from "./pages/DirectoryPage";
import { EmployeePage } from "./pages/EmployeePage";
import { ForbiddenPage } from "./pages/ForbiddenPage";
import { InsightsPage } from "./pages/InsightsPage";
import { LoginPage } from "./pages/LoginPage";

const theme = createTheme({
  palette: { primary: { main: "#0d47a1" } },
});

const queryClient = new QueryClient();

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route element={<AppShell />}>
                <Route path="/directory" element={<DirectoryPage />} />
                <Route path="/employees/:id" element={<EmployeePage />} />
                <Route path="/insights" element={<InsightsPage />} />
                <Route path="/403" element={<ForbiddenPage />} />
              </Route>
              <Route path="*" element={<Navigate to="/directory" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </QueryClientProvider>
    </ThemeProvider>
  );
}
