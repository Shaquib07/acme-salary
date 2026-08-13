import { Alert, Card, CardContent, Paper, Stack, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { api } from "../api";
import { formatMoney } from "../lib/format";

export function InsightsPage() {
  const summary = useQuery({ queryKey: ["insights-summary"], queryFn: api.summary });
  const countries = useQuery({ queryKey: ["insights-country"], queryFn: api.byCountry });
  const bands = useQuery({ queryKey: ["insights-bands"], queryFn: api.payBands });
  const depts = useQuery({ queryKey: ["insights-dept"], queryFn: api.byDepartment });

  if (summary.error) {
    return <Alert severity="error">{(summary.error as Error).message}</Alert>;
  }

  return (
    <Stack gap={3}>
      <Typography variant="h5">How ACME pays people</Typography>
      <Stack direction={{ xs: "column", md: "row" }} gap={2}>
        <Card sx={{ flex: 1 }}>
          <CardContent>
            <Typography color="text.secondary">Active headcount</Typography>
            <Typography variant="h4">{summary.data?.activeHeadcount ?? "—"}</Typography>
          </CardContent>
        </Card>
        <Card sx={{ flex: 2 }}>
          <CardContent>
            <Typography color="text.secondary">Approximate USD payroll</Typography>
            <Typography variant="h4">
              {summary.data ? formatMoney(summary.data.approximateUsdPayroll, "USD") : "—"}
            </Typography>
            <Typography variant="caption">{summary.data?.usdDisclaimer}</Typography>
          </CardContent>
        </Card>
      </Stack>
      <Paper sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Payroll by currency (never mixed)
        </Typography>
        <Stack gap={1}>
          {summary.data?.payrollByCurrency.map((row) => (
            <Typography key={row.currencyCode}>
              {row.currencyCode}: {formatMoney(row.payroll, row.currencyCode)} · {row.headcount} people · avg{" "}
              {formatMoney(row.averageSalary, row.currencyCode)}
            </Typography>
          ))}
        </Stack>
      </Paper>
      <Paper sx={{ p: 2, height: 360 }}>
        <Typography variant="h6">Pay bands (USD equivalent)</Typography>
        <ResponsiveContainer>
          <BarChart data={bands.data ?? []}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="band" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="headcount" fill="#1565c0" />
          </BarChart>
        </ResponsiveContainer>
      </Paper>
      <Paper sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          By country
        </Typography>
        {(countries.data ?? []).map((row) => (
          <Typography key={`${row.countryCode}-${row.currencyCode}`}>
            {row.countryCode} ({row.currencyCode}): {row.headcount} · {formatMoney(row.payroll, row.currencyCode)}
          </Typography>
        ))}
      </Paper>
      <Paper sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          By department
        </Typography>
        {(depts.data ?? []).slice(0, 40).map((row) => (
          <Typography key={`${row.department}-${row.currencyCode}`}>
            {row.department} ({row.currencyCode}): {row.headcount} · {formatMoney(row.payroll, row.currencyCode)}
          </Typography>
        ))}
      </Paper>
    </Stack>
  );
}
