package com.bloomberg.fx;

// No Spring or JUnit imports are needed!
import com.bloomberg.fx.dto.FxDealRequestDTO;
import com.bloomberg.fx.model.FxDeal;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A simple utility class for creating test data.
 * This is NOT a test class, so it has no annotations.
 */
public class TestData {

    public static FxDealRequestDTO createValidDTO(String id) {
        FxDealRequestDTO dto = new FxDealRequestDTO();
        dto.setDealUniqueId(id);
        dto.setFromCurrency("USD");
        dto.setToCurrency("EUR");
        dto.setDealTimestamp(Instant.now().minusSeconds(60));
        dto.setDealAmount(BigDecimal.valueOf(1000.50));
        return dto;
    }

    public static FxDeal mapToEntity(FxDealRequestDTO dto) {
        return FxDeal.builder()
                .dealUniqueId(dto.getDealUniqueId())
                .fromCurrency(dto.getFromCurrency())
                .toCurrency(dto.getToCurrency())
                .dealTimestamp(dto.getDealTimestamp())
                .dealAmount(dto.getDealAmount())
                .build();
    }
}