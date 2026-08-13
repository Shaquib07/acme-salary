import { employeeQueryString, formatMoney } from "./format";
import { describe, expect, it } from "vitest";

describe("formatMoney", () => {
  it("formats USD without cents for HR density", () => {
    expect(formatMoney(99000, "USD")).toContain("99,000");
  });
});

describe("employeeQueryString", () => {
  it("omits empty filters and keeps pagination", () => {
    expect(employeeQueryString({ q: "ada", page: 1, size: 20, country: "" })).toBe("q=ada&page=1&size=20");
  });
});
