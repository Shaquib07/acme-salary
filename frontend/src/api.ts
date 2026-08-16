import type { Role } from "./auth/roles";

const TOKEN_KEY = "acme.jwt";
const API_BASE = (import.meta.env.VITE_API_BASE ?? "").replace(/\/$/, "");

export type Employee = {
  id: number;
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  jobTitle: string;
  countryCode: string;
  currencyCode: string;
  annualSalary: number;
  employmentType: string;
  status: string;
  hiredOn: string;
  updatedAt: string;
  lastEditor?: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type Me = { email: string; displayName: string; role: Role };

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem(TOKEN_KEY);
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");
  if (init.body && !(init.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  const response = await fetch(`${API_BASE}${path}`, { ...init, headers });
  if (response.status === 401) {
    localStorage.removeItem(TOKEN_KEY);
    throw new Error("unauthorized");
  }
  if (response.status === 403) {
    const err = new Error("forbidden");
    (err as Error & { status: number }).status = 403;
    throw err;
  }
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    const fieldErrors =
      body.fields && typeof body.fields === "object"
        ? Object.values(body.fields).filter(Boolean).join(", ")
        : "";
    throw new Error(body.message || fieldErrors || "Request failed");
  }
  if (response.headers.get("content-type")?.includes("text/csv")) {
    return (await response.text()) as T;
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export const api = {
  setToken(token: string | null) {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  },
  token() {
    return localStorage.getItem(TOKEN_KEY);
  },
  login(email: string, password: string) {
    return request<{ token: string; email: string; displayName: string; role: Role }>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },
  me() {
    return request<Me>("/api/auth/me");
  },
  employees(query: string) {
    return request<PageResponse<Employee>>(`/api/employees?${query}`);
  },
  employee(id: number) {
    return request<Employee>(`/api/employees/${id}`);
  },
  createEmployee(body: unknown) {
    return request<Employee>("/api/employees", { method: "POST", body: JSON.stringify(body) });
  },
  patchSalary(id: number, annualSalary: number) {
    return request<Employee>(`/api/employees/${id}/salary`, {
      method: "PATCH",
      body: JSON.stringify({ annualSalary }),
    });
  },
  updateEmployee(id: number, body: unknown) {
    return request<Employee>(`/api/employees/${id}`, { method: "PATCH", body: JSON.stringify(body) });
  },
  exportCsv(query: string) {
    return request<string>(`/api/employees/export?${query}`);
  },
  filters() {
    return request<{
      countries: { countryCode: string; countryName: string; currencyCode: string }[];
      departments: string[];
      statuses: string[];
    }>("/api/meta/filters");
  },
  summary() {
    return request<{
      activeHeadcount: number;
      payrollByCurrency: { currencyCode: string; headcount: number; payroll: number; averageSalary: number }[];
      approximateUsdPayroll: number;
      usdDisclaimer: string;
    }>("/api/insights/summary");
  },
  byCountry() {
    return request<
      { countryCode: string; currencyCode: string; headcount: number; payroll: number; averageSalary: number }[]
    >("/api/insights/by-country");
  },
  byDepartment() {
    return request<
      { department: string; currencyCode: string; headcount: number; payroll: number; averageSalary: number }[]
    >("/api/insights/by-department");
  },
  payBands() {
    return request<{ band: string; headcount: number }[]>("/api/insights/pay-bands");
  },
};
