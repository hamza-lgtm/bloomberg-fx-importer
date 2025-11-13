package com.bloomberg.fx.service;
// ... imports ...
import com.bloomberg.fx.dto.FxDealRequestDTO;
import com.bloomberg.fx.dto.ImportResultDTO;
import com.bloomberg.fx.model.FxDeal;
import com.bloomberg.fx.repository.FxDealRepository;
import com.bloomberg.fx.TestData;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class FxDealServiceTest {

    @Mock
    private FxDealRepository repository;

    // We use a real validator to test validation logic
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @InjectMocks
    private FxDealService service;

    @BeforeEach
    void setUp() {
        // Manually inject the real validator into the service
        service = new FxDealService(repository, validator);
    }

    @Test
    void testImportDealsPartialSuccessNoRollback() {
        // 1. Valid Deal
        FxDealRequestDTO validDeal = TestData.createValidDTO("id-1");

        // 2. Invalid Deal (bad currency code)
        FxDealRequestDTO invalidDeal = TestData.createValidDTO("id-2");
        invalidDeal.setFromCurrency("USDEUR");

        // 3. Duplicate Deal
        FxDealRequestDTO duplicateDeal = TestData.createValidDTO("id-3");
        
        // --- NEW MOCKS ---
        // Mock behavior for id-1 (Valid)
        when(repository.existsById("id-1")).thenReturn(false);
        // We don't need to mock save() for the success case in a unit test,
        // but it's good practice.
        when(repository.save(any(FxDeal.class))).thenReturn(null);

        // Mock behavior for id-3 (Duplicate)
        when(repository.existsById("id-3")).thenReturn(true);
        // Note: repository.save() will NOT be called for id-3,
        // so we no longer need to mock it to throw an exception.
        
        List<FxDealRequestDTO> requests = List.of(validDeal, invalidDeal, duplicateDeal);

        // Act
        ImportResultDTO result = service.importDeals(requests);

        // Assert
        // 1 succeeded (id-1)
        assertThat(result.getSuccessCount()).isEqualTo(1);
        // 2 failed (id-2 validation, id-3 duplicate check)
        assertThat(result.getFailureCount()).isEqualTo(2);

        // Verify save was only called ONCE (for the valid deal)
        verify(repository, times(1)).save(any(FxDeal.class));
        
        // Check failure details
        assertThat(result.getFailures())
                .extracting(ImportResultDTO.FailedDeal::getDealId)
                .containsExactlyInAnyOrder("id-2", "id-3");
        
        assertThat(result.getFailures())
                .filteredOn(f -> f.getDealId().equals("id-3"))
                .extracting(ImportResultDTO.FailedDeal::getError)
                .anyMatch(error -> error.contains("Duplicate deal"));
    }
   @Test
    void testImportDealsHandlesUnexpectedException() {
        // 1. Create a valid deal
        FxDealRequestDTO validDeal = TestData.createValidDTO("id-unexpected");

        // 2. Mock the repository to throw an unexpected exception
        //    (e.g., a generic RuntimeException instead of a DataIntegrityViolationException)
        when(repository.existsById("id-unexpected"))
                .thenThrow(new RuntimeException("Database is down"));

        // 3. Act
        ImportResultDTO result = service.importDeals(List.of(validDeal));

        // 4. Assert
        // Verify the generic 'catch (Exception e)' block was executed
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getDealId()).isEqualTo("id-unexpected");
        assertThat(result.getFailures().get(0).getError()).contains("Unexpected error: Database is down");
    }
}