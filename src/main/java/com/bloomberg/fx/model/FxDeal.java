package com.bloomberg.fx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fx_deals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxDeal {

    @Id
    @Column(name = "deal_unique_id", nullable = false, updatable = false)
    private String dealUniqueId;

    @Column(name = "from_currency", nullable = false)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false)
    private String toCurrency;

    @Column(name = "deal_timestamp", nullable = false)
    private Instant dealTimestamp;

    @Column(name = "deal_amount", nullable = false)
    private BigDecimal dealAmount;
}