export type Role = "HR_ADMIN" | "HR_MANAGER" | "FINANCE_VIEWER";

export type Action = "editSalary" | "createEmployee" | "deactivate" | "exportCsv";

const matrix: Record<Role, Action[]> = {
  HR_ADMIN: ["editSalary", "createEmployee", "deactivate", "exportCsv"],
  HR_MANAGER: ["editSalary", "exportCsv"],
  FINANCE_VIEWER: [],
};

export function can(role: Role | undefined, action: Action): boolean {
  if (!role) {
    return false;
  }
  return matrix[role].includes(action);
}
