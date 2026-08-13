package com.acme.salary.insight;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InsightServiceTest {

    @Test
    void doesNotAddMixedCurrenciesWhenConvertingIndividually() {
        Map<String, BigDecimal> fx = Map.of("USD", BigDecimal.ONE, "INR", new BigDecimal("0.012"));
        BigDecimal usdPayroll = new BigDecimal("100000");
        BigDecimal inrPayroll = new BigDecimal("100000");
        BigDecimal naiveSum = usdPayroll.add(inrPayroll);
        BigDecimal converted = InsightService.toUsd(usdPayroll, "USD", fx)
                .add(InsightService.toUsd(inrPayroll, "INR", fx));
        assertThat(naiveSum).isEqualByComparingTo("200000");
        assertThat(converted).isEqualByComparingTo("101200.000");
        assertThat(converted).isNotEqualByComparingTo(naiveSum);
    }

    @Test
    void assignsPayBandsInUsd() {
        assertThat(InsightService.bandFor(new BigDecimal("29999.99"))).isEqualTo("0-30k");
        assertThat(InsightService.bandFor(new BigDecimal("30000"))).isEqualTo("30-50k");
        assertThat(InsightService.bandFor(new BigDecimal("149999"))).isEqualTo("100-150k");
        assertThat(InsightService.bandFor(new BigDecimal("150000"))).isEqualTo("150k+");
    }
}
