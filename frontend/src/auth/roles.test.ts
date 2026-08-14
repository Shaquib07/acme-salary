import { can } from "./roles";
import { describe, expect, it } from "vitest";

describe("can", () => {
  it("lets managers edit salary and profile but not create", () => {
    expect(can("HR_MANAGER", "editSalary")).toBe(true);
    expect(can("HR_MANAGER", "editEmployee")).toBe(true);
    expect(can("HR_MANAGER", "createEmployee")).toBe(false);
  });

  it("blocks finance from export and edits", () => {
    expect(can("FINANCE_VIEWER", "exportCsv")).toBe(false);
    expect(can("FINANCE_VIEWER", "editSalary")).toBe(false);
    expect(can("HR_ADMIN", "deactivate")).toBe(true);
  });
});
