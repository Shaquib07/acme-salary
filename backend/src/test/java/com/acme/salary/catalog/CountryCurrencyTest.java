package com.acme.salary.catalog;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class CountryCurrencyTest {

    @Test
    void acceptsMatchingCountryCurrency() {
        assertDoesNotThrow(() -> CountryCurrency.requireMatchingCurrency("IN", "INR"));
    }

    @Test
    void rejectsMismatchedCurrency() {
        assertThatThrownBy(() -> CountryCurrency.requireMatchingCurrency("IN", "USD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
    }
}
