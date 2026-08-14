import {
  Alert,
  Button,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth/AuthContext";
import { formatMoney } from "../lib/format";

const EMPLOYMENT_TYPES = ["FULL_TIME", "PART_TIME", "CONTRACT"];

export function EmployeePage() {
  const { id } = useParams();
  const employeeId = Number(id);
  const { allowed } = useAuth();
  const queryClient = useQueryClient();
  const canEdit = allowed("editEmployee");
  const canPay = allowed("editSalary");
  const detail = useQuery({ queryKey: ["employee", employeeId], queryFn: () => api.employee(employeeId) });
  const filters = useQuery({ queryKey: ["filters"], queryFn: api.filters });
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    department: "",
    jobTitle: "",
    countryCode: "",
    currencyCode: "",
    employmentType: "FULL_TIME",
    annualSalary: "",
  });

  useEffect(() => {
    if (!detail.data) {
      return;
    }
    const e = detail.data;
    setForm({
      firstName: e.firstName,
      lastName: e.lastName,
      department: e.department,
      jobTitle: e.jobTitle,
      countryCode: e.countryCode,
      currencyCode: e.currencyCode,
      employmentType: e.employmentType,
      annualSalary: String(e.annualSalary),
    });
  }, [detail.data]);

  const save = useMutation({
    mutationFn: async () => {
      const salary = Number(form.annualSalary);
      if (canPay && !(salary > 0)) {
        throw new Error("Salary must be greater than 0");
      }
      if (canEdit) {
        await api.updateEmployee(employeeId, {
          firstName: form.firstName,
          lastName: form.lastName,
          department: form.department,
          jobTitle: form.jobTitle,
          countryCode: form.countryCode,
          currencyCode: form.currencyCode,
          employmentType: form.employmentType,
        });
      }
      if (canPay) {
        await api.patchSalary(employeeId, salary);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["employee", employeeId] });
      queryClient.invalidateQueries({ queryKey: ["employees"] });
    },
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
  const readOnly = !canEdit && !canPay;

  function setField<K extends keyof typeof form>(key: K, value: (typeof form)[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  return (
    <Paper sx={{ p: 3, maxWidth: 640 }}>
      <Typography variant="h5">
        {e.firstName} {e.lastName}
      </Typography>
      <Typography color="text.secondary">{e.employeeNumber}</Typography>
      <Stack gap={2} sx={{ mt: 2 }}>
        <div>{e.email}</div>
        <div>Status: {e.status}</div>
        {e.lastEditor && <div>Last editor: {e.lastEditor}</div>}
        <div>Current pay: {formatMoney(e.annualSalary, e.currencyCode)}</div>
        {readOnly ? (
          <>
            <div>
              {e.department} · {e.jobTitle}
            </div>
            <div>
              {e.countryCode} · {e.currencyCode} · {e.employmentType}
            </div>
          </>
        ) : (
          <>
            {canEdit && (
              <>
                <TextField label="First name" value={form.firstName} onChange={(ev) => setField("firstName", ev.target.value)} />
                <TextField label="Last name" value={form.lastName} onChange={(ev) => setField("lastName", ev.target.value)} />
                <TextField label="Department" value={form.department} onChange={(ev) => setField("department", ev.target.value)} />
                <TextField label="Job title" value={form.jobTitle} onChange={(ev) => setField("jobTitle", ev.target.value)} />
                <FormControl>
                  <InputLabel>Country</InputLabel>
                  <Select
                    label="Country"
                    value={form.countryCode}
                    onChange={(ev) => {
                      const countryCode = String(ev.target.value);
                      const match = filters.data?.countries.find((c) => c.countryCode === countryCode);
                      setForm((current) => ({
                        ...current,
                        countryCode,
                        currencyCode: match?.currencyCode ?? current.currencyCode,
                      }));
                    }}
                  >
                    {(filters.data?.countries ?? [{ countryCode: form.countryCode, countryName: form.countryCode }]).map(
                      (c) => (
                        <MenuItem key={c.countryCode} value={c.countryCode}>
                          {c.countryCode}
                        </MenuItem>
                      ),
                    )}
                  </Select>
                </FormControl>
                <TextField label="Currency" value={form.currencyCode} slotProps={{ input: { readOnly: true } }} />
                <FormControl>
                  <InputLabel>Employment type</InputLabel>
                  <Select
                    label="Employment type"
                    value={form.employmentType}
                    onChange={(ev) => setField("employmentType", String(ev.target.value))}
                  >
                    {EMPLOYMENT_TYPES.map((type) => (
                      <MenuItem key={type} value={type}>
                        {type}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </>
            )}
            {canPay && (
              <TextField
                label="Annual salary"
                value={form.annualSalary}
                onChange={(ev) => setField("annualSalary", ev.target.value)}
                type="number"
              />
            )}
            <Button
              variant="contained"
              disabled={save.isPending || (canPay && Number(form.annualSalary) <= 0)}
              onClick={() => save.mutate()}
            >
              Save
            </Button>
          </>
        )}
      </Stack>
      {allowed("deactivate") && e.status === "ACTIVE" && (
        <Button sx={{ mt: 2 }} color="warning" onClick={() => deactivate.mutate()}>
          Deactivate
        </Button>
      )}
      {save.isSuccess && (
        <Alert sx={{ mt: 2 }} severity="success">
          Employee saved
        </Alert>
      )}
      {save.error && (
        <Alert sx={{ mt: 2 }} severity="error">
          {(save.error as Error).message}
        </Alert>
      )}
    </Paper>
  );
}
