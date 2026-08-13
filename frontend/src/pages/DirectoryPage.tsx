import {
  Alert,
  Box,
  Button,
  Drawer,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Snackbar,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { DataGrid, type GridColDef, type GridPaginationModel, type GridSortModel } from "@mui/x-data-grid";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, type Employee } from "../api";
import { useAuth } from "../auth/AuthContext";
import { employeeQueryString, formatMoney } from "../lib/format";

export function DirectoryPage() {
  const { allowed } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [q, setQ] = useState("");
  const [country, setCountry] = useState("");
  const [department, setDepartment] = useState("");
  const [status, setStatus] = useState("ACTIVE");
  const [pagination, setPagination] = useState<GridPaginationModel>({ page: 0, pageSize: 20 });
  const [sort, setSort] = useState("lastName,asc");
  const [selected, setSelected] = useState<Employee | null>(null);
  const [salary, setSalary] = useState("");
  const [toast, setToast] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);

  const query = employeeQueryString({
    q,
    country,
    department,
    status,
    page: pagination.page,
    size: pagination.pageSize,
    sort,
  });

  const filters = useQuery({ queryKey: ["filters"], queryFn: api.filters });
  const list = useQuery({ queryKey: ["employees", query], queryFn: () => api.employees(query) });

  const saveSalary = useMutation({
    mutationFn: () => api.patchSalary(selected!.id, Number(salary)),
    onSuccess: () => {
      setToast("Salary updated");
      setSelected(null);
      queryClient.invalidateQueries({ queryKey: ["employees"] });
    },
  });

  const columns: GridColDef<Employee>[] = useMemo(
    () => [
      { field: "employeeNumber", headerName: "ID", width: 110 },
      { field: "lastName", headerName: "Last", width: 130 },
      { field: "firstName", headerName: "First", width: 130 },
      { field: "department", headerName: "Department", width: 140 },
      { field: "countryCode", headerName: "Country", width: 90 },
      {
        field: "annualSalary",
        headerName: "Annual salary",
        width: 150,
        valueGetter: (_v, row) => formatMoney(row.annualSalary, row.currencyCode),
      },
      { field: "status", headerName: "Status", width: 110 },
    ],
    [],
  );

  async function onExport() {
    const csv = await api.exportCsv(query);
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "employees.csv";
    a.click();
    URL.revokeObjectURL(url);
  }

  return (
    <Stack gap={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h5">Employee directory</Typography>
        <Stack direction="row" gap={1}>
          {allowed("exportCsv") && (
            <Button variant="outlined" onClick={onExport}>
              Export CSV
            </Button>
          )}
          {allowed("createEmployee") && (
            <Button variant="contained" onClick={() => setCreateOpen(true)}>
              Add employee
            </Button>
          )}
        </Stack>
      </Stack>
      <Stack direction="row" gap={2} flexWrap="wrap">
        <TextField label="Search" value={q} onChange={(e) => setQ(e.target.value)} size="small" />
        <FormControl size="small" sx={{ minWidth: 140 }}>
          <InputLabel>Country</InputLabel>
          <Select value={country} label="Country" onChange={(e) => setCountry(e.target.value)}>
            <MenuItem value="">All</MenuItem>
            {filters.data?.countries.map((c) => (
              <MenuItem key={c.countryCode} value={c.countryCode}>
                {c.countryCode}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>Department</InputLabel>
          <Select value={department} label="Department" onChange={(e) => setDepartment(e.target.value)}>
            <MenuItem value="">All</MenuItem>
            {filters.data?.departments.map((d) => (
              <MenuItem key={d} value={d}>
                {d}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 140 }}>
          <InputLabel>Status</InputLabel>
          <Select value={status} label="Status" onChange={(e) => setStatus(e.target.value)}>
            <MenuItem value="">All</MenuItem>
            <MenuItem value="ACTIVE">ACTIVE</MenuItem>
            <MenuItem value="INACTIVE">INACTIVE</MenuItem>
          </Select>
        </FormControl>
      </Stack>
      {list.error && <Alert severity="error">{(list.error as Error).message}</Alert>}
      <Box sx={{ height: 640 }}>
        <DataGrid
          rows={list.data?.content ?? []}
          columns={columns}
          rowCount={list.data?.totalElements ?? 0}
          loading={list.isLoading}
          paginationMode="server"
          sortingMode="server"
          paginationModel={pagination}
          onPaginationModelChange={setPagination}
          pageSizeOptions={[20, 50, 100]}
          onSortModelChange={(model: GridSortModel) => {
            const first = model[0];
            setSort(first ? `${first.field},${first.sort}` : "lastName,asc");
          }}
          onRowClick={(params) => {
            if (allowed("editSalary")) {
              setSelected(params.row);
              setSalary(String(params.row.annualSalary));
            } else {
              navigate(`/employees/${params.row.id}`);
            }
          }}
          disableRowSelectionOnClick
        />
      </Box>
      <Drawer anchor="right" open={Boolean(selected)} onClose={() => setSelected(null)}>
        <Box sx={{ width: 360, p: 3 }}>
          {selected && (
            <Stack gap={2}>
              <Typography variant="h6">
                {selected.firstName} {selected.lastName}
              </Typography>
              <Typography color="text.secondary">{selected.email}</Typography>
              <Typography>
                {selected.countryCode} · {selected.currencyCode}
              </Typography>
              <TextField
                label="Annual salary"
                value={salary}
                onChange={(e) => setSalary(e.target.value)}
                type="number"
                error={Number(salary) <= 0}
                helperText={Number(salary) <= 0 ? "Salary must be greater than 0" : " "}
              />
              <Button
                variant="contained"
                disabled={Number(salary) <= 0 || saveSalary.isPending}
                onClick={() => saveSalary.mutate()}
              >
                Save salary
              </Button>
              <Button onClick={() => navigate(`/employees/${selected.id}`)}>Open profile</Button>
            </Stack>
          )}
        </Box>
      </Drawer>
      <CreateDrawer open={createOpen} onClose={() => setCreateOpen(false)} onCreated={() => setToast("Employee created")} />
      <Snackbar open={Boolean(toast)} autoHideDuration={3000} onClose={() => setToast(null)} message={toast} />
    </Stack>
  );
}

function CreateDrawer({
  open,
  onClose,
  onCreated,
}: {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}) {
  const queryClient = useQueryClient();
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    department: "Engineering",
    jobTitle: "Engineer",
    countryCode: "US",
    currencyCode: "USD",
    annualSalary: "80000",
    employmentType: "FULL_TIME",
    hiredOn: "2024-01-15",
  });
  const create = useMutation({
    mutationFn: () =>
      api.createEmployee({
        ...form,
        annualSalary: Number(form.annualSalary),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["employees"] });
      onCreated();
      onClose();
    },
  });

  return (
    <Drawer anchor="right" open={open} onClose={onClose}>
      <Box sx={{ width: 380, p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Add employee
        </Typography>
        <Stack gap={2}>
          {Object.entries({
            firstName: "First name",
            lastName: "Last name",
            email: "Email",
            department: "Department",
            jobTitle: "Job title",
            countryCode: "Country",
            currencyCode: "Currency",
            annualSalary: "Annual salary",
            hiredOn: "Hired on",
          }).map(([key, label]) => (
            <TextField
              key={key}
              label={label}
              value={form[key as keyof typeof form]}
              onChange={(e) => setForm({ ...form, [key]: e.target.value })}
            />
          ))}
          <Button variant="contained" onClick={() => create.mutate()} disabled={create.isPending}>
            Create
          </Button>
          {create.error && <Alert severity="error">{(create.error as Error).message}</Alert>}
        </Stack>
      </Box>
    </Drawer>
  );
}
