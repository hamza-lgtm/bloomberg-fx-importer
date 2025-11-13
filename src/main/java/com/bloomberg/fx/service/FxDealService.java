package com.bloomberg.fx.service;

import com.bloomberg.fx.dto.FxDealRequestDTO;
import com.bloomberg.fx.dto.ImportResultDTO;
import com.bloomberg.fx.model.FxDeal;
import com.bloomberg.fx.repository.FxDealRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FxDealService {

    private final FxDealRepository repository;
    private final Validator validator; 

    public ImportResultDTO importDeals(List<FxDealRequestDTO> dealRequests) {
        int successCount = 0;
        List<ImportResultDTO.FailedDeal> failures = new ArrayList<>();

        for (FxDealRequestDTO dto : dealRequests) {
            try {
                validateRequest(dto);
                if (repository.existsById(dto.getDealUniqueId())) {
                    throw new DataIntegrityViolationException("Duplicate deal: Deal with ID " + dto.getDealUniqueId() + " already exists.");
                }
                FxDeal deal = mapToEntity(dto);
                repository.save(deal);
                successCount++; 

            } catch (ConstraintViolationException | DataIntegrityViolationException e) { 
                log.warn("Failed to import deal {}: {}", dto.getDealUniqueId(), e.getMessage());
                failures.add(new ImportResultDTO.FailedDeal(dto.getDealUniqueId(), e.getMessage()));
            } catch (Exception e) {
                log.error("Unexpected error importing deal {}: {}", dto.getDealUniqueId(), e.getMessage(), e);
                failures.add(new ImportResultDTO.FailedDeal(dto.getDealUniqueId(), "Unexpected error: " + e.getMessage()));
            }
        }

        return ImportResultDTO.builder()
                .successCount(successCount)
                .failureCount(failures.size())
                .failures(failures)
                .build();
    }

    private void validateRequest(FxDealRequestDTO dto) {
        Set<ConstraintViolation<FxDealRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errorMessages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ConstraintViolationException("Validation failed: " + errorMessages, violations);
        }
    }

    private FxDeal mapToEntity(FxDealRequestDTO dto) {
        return FxDeal.builder()
                .dealUniqueId(dto.getDealUniqueId())
                .fromCurrency(dto.getFromCurrency())
                .toCurrency(dto.getToCurrency())
                .dealTimestamp(dto.getDealTimestamp())
                .dealAmount(dto.getDealAmount())
                .build();
    }
}