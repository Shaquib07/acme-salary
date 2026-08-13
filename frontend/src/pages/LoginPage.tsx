import { Alert, Box, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import { FormEvent, useState } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export function LoginPage() {
  const { user, login } = useAuth();
  const [email, setEmail] = useState("hr@acme.test");
  const [password, setPassword] = useState("Password123!");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  if (user) {
    return <Navigate to="/directory" replace />;
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await login(email, password);
    } catch {
      setError("Invalid email or password");
    } finally {
      setBusy(false);
    }
  }

  return (
    <Box sx={{ minHeight: "100vh", display: "grid", placeItems: "center", bgcolor: "#f4f6f8" }}>
      <Paper sx={{ p: 4, width: 420 }} elevation={2}>
        <Typography variant="h5" gutterBottom>
          ACME Salary
        </Typography>
        <Typography color="text.secondary" sx={{ mb: 2 }}>
          Sign in to manage how the org pays people.
        </Typography>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <Stack component="form" gap={2} onSubmit={onSubmit}>
          <TextField label="Email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="username" />
          <TextField
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
          <Button type="submit" variant="contained" disabled={busy}>
            Sign in
          </Button>
        </Stack>
        <Typography variant="caption" display="block" sx={{ mt: 2 }} color="text.secondary">
          Demo: admin@acme.test · hr@acme.test · finance@acme.test — password Password123!
        </Typography>
      </Paper>
    </Box>
  );
}
