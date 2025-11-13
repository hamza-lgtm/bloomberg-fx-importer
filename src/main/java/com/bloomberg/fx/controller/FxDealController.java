package com.bloomberg.fx.controller;

import com.bloomberg.fx.dto.FxDealRequestDTO;
import com.bloomberg.fx.dto.ImportResultDTO;
import com.bloomberg.fx.service.FxDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
public class FxDealController {

    private final FxDealService dealService;

    @PostMapping("/import")
    public ResponseEntity<ImportResultDTO> importDeals(@RequestBody List<FxDealRequestDTO> dealRequests) {
        ImportResultDTO result = dealService.importDeals(dealRequests);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}