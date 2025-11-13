package com.bloomberg.fx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class FxDealRequestDTO {

    @NotNull(message = "Deal Unique Id cannot be null")
    private String dealUniqueId;

    @NotNull(message = "From Currency ISO Code cannot be null")
    @Size(min = 3, max = 3, message = "From Currency ISO Code must be 3 characters")
    private String fromCurrency;

    @NotNull(message = "To Currency ISO Code cannot be null")
    @Size(min = 3, max = 3, message = "To Currency ISO Code must be 3 characters")
    private String toCurrency;

    @NotNull(message = "Deal timestamp cannot be null")
    @PastOrPresent(message = "Deal timestamp cannot be in the future")
    private Instant dealTimestamp;

    @NotNull(message = "Deal Amount cannot be null")
    @Positive(message = "Deal Amount must be positive")
    private BigDecimal dealAmount;
}