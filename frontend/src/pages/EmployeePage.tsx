import { Alert, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth/AuthContext";
import { formatMoney } from "../lib/format";

export function EmployeePage() {
  const { id } = useParams();
  const employeeId = Number(id);
  const { allowed } = useAuth();
  const queryClient = useQueryClient();
  const detail = useQuery({ queryKey: ["employee", employeeId], queryFn: () => api.employee(employeeId) });
  const [salary, setSalary] = useState<string>();
  const save = useMutation({
    mutationFn: () => api.patchSalary(employeeId, Number(salary)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["employee", employeeId] }),
  });
  const deactivate = useMutation({
    mutationFn: () => api.updateEmployee(employeeId, { status: "INACTIVE" }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["employee", employeeId] }),
  });

  if (detail.isLoading) {
    return <Typography>Loading…</Typography>;
  }
  if (detail.error || !detail.data) {
    return <Alert severity="error">Employee not found</Alert>;
  }
  const e = detail.data;
  const value = salary ?? String(e.annualSalary);

  return (
    <Paper sx={{ p: 3, maxWidth: 640 }}>
      <Typography variant="h5">
        {e.firstName} {e.lastName}
      </Typography>
      <Typography color="text.secondary">{e.employeeNumber}</Typography>
      <Stack gap={1} sx={{ mt: 2 }}>
        <div>{e.email}</div>
        <div>
          {e.department} · {e.jobTitle}
        </div>
        <div>
          {e.countryCode} · {e.currencyCode} · {e.status}
        </div>
        <div>Current: {formatMoney(e.annualSalary, e.currencyCode)}</div>
        {e.lastEditor && <div>Last editor: {e.lastEditor}</div>}
      </Stack>
      {allowed("editSalary") && (
        <Stack direction="row" gap={2} sx={{ mt: 3 }}>
          <TextField label="Annual salary" value={value} onChange={(ev) => setSalary(ev.target.value)} type="number" />
          <Button variant="contained" disabled={Number(value) <= 0} onClick={() => save.mutate()}>
            Save
          </Button>
        </Stack>
      )}
      {allowed("deactivate") && e.status === "ACTIVE" && (
        <Button sx={{ mt: 2 }} color="warning" onClick={() => deactivate.mutate()}>
          Deactivate
        </Button>
      )}
      {save.error && <Alert sx={{ mt: 2 }} severity="error">{(save.error as Error).message}</Alert>}
    </Paper>
  );
}
