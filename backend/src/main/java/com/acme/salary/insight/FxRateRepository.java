package com.acme.salary.insight;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxRateRepository extends JpaRepository<FxRate, Long> {

    Optional<FxRate> findByFromCurrencyAndToCurrency(String from, String to);

    List<FxRate> findByToCurrency(String to);
}
