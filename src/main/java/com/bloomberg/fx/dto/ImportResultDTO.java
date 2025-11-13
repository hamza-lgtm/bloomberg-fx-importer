package com.bloomberg.fx.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportResultDTO {
    private int successCount;
    private int failureCount;
    private List<FailedDeal> failures;

    @Data
    @Builder
    public static class FailedDeal {
        private String dealId;
        private String error;
        
        public FailedDeal(String dealId, String error) {
            this.dealId = dealId;
            this.error = error;
        }
    }
}