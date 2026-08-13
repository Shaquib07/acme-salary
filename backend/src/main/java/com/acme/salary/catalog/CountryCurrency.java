package com.acme.salary.catalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CountryCurrency {

    public record Pair(String countryCode, String countryName, String currencyCode) {
    }

    private static final List<Pair> PAIRS = List.of(
            new Pair("US", "United States", "USD"),
            new Pair("GB", "United Kingdom", "GBP"),
            new Pair("IN", "India", "INR"),
            new Pair("DE", "Germany", "EUR"),
            new Pair("SG", "Singapore", "SGD"),
            new Pair("AU", "Australia", "AUD"),
            new Pair("CA", "Canada", "CAD"),
            new Pair("JP", "Japan", "JPY"),
            new Pair("BR", "Brazil", "BRL"),
            new Pair("AE", "United Arab Emirates", "AED"));

    private static final Map<String, Pair> BY_COUNTRY = new LinkedHashMap<>();

    static {
        PAIRS.forEach(p -> BY_COUNTRY.put(p.countryCode(), p));
    }

    private CountryCurrency() {
    }

    public static List<Pair> all() {
        return PAIRS;
    }

    public static Optional<Pair> byCountry(String countryCode) {
        if (countryCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_COUNTRY.get(countryCode.toUpperCase()));
    }

    public static void requireMatchingCurrency(String countryCode, String currencyCode) {
        Pair pair = byCountry(countryCode)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported country: " + countryCode));
        if (!pair.currencyCode().equalsIgnoreCase(currencyCode)) {
            throw new IllegalArgumentException(
                    "Currency " + currencyCode + " does not match country " + countryCode + " (" + pair.currencyCode() + ")");
        }
    }
}
