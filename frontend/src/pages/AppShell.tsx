import { AppBar, Box, Button, Toolbar, Typography } from "@mui/material";
import { Link as RouterLink, Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export function AppShell() {
  const { user, loading, logout } = useAuth();

  if (loading) {
    return <Typography sx={{ p: 4 }}>Loading…</Typography>;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <Box>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            ACME Salary
          </Typography>
          <Button color="inherit" component={RouterLink} to="/directory">
            Employee Details
          </Button>
          <Button color="inherit" component={RouterLink} to="/insights">
            Pay insights
          </Button>
          <Typography sx={{ mx: 2 }} variant="body2">
            {user.displayName} · {user.role}
          </Typography>
          <Button color="inherit" onClick={logout}>
            Sign out
          </Button>
        </Toolbar>
      </AppBar>
      <Box sx={{ p: 3 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
