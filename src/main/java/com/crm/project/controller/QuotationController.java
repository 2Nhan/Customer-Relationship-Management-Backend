package com.crm.project.controller;

import com.crm.project.dto.request.OrderCreationFromQuotationRequest;
import com.crm.project.dto.request.QuotationCreationRequest;
import com.crm.project.dto.response.MyApiResponse;
import com.crm.project.dto.response.OrderResponse;
import com.crm.project.dto.response.QuotationResponse;
import com.crm.project.internal.PageInfo;
import com.crm.project.service.OrderService;
import com.crm.project.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/quotations")
public class QuotationController {
    private final QuotationService quotationService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<MyApiResponse> createQuotation(@RequestBody @Valid QuotationCreationRequest request) {
        QuotationResponse response = quotationService.createQuotation(request);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/{id}/mail")
    public ResponseEntity<MyApiResponse> sendQuotationEmail(@PathVariable String id) throws Exception {
        QuotationResponse response = quotationService.sendQuotationEmail(id);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .message("Quotation email sent successfully")
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MyApiResponse> getQuotationById(@PathVariable("id") String id) {
        QuotationResponse quotationResponse = quotationService.getQuotation(id);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(quotationResponse)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<MyApiResponse> getAllQuotations(@RequestParam(name = "pageNo", required = false, defaultValue = "1") int pageNumber,
                                                          @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                          @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                                                          @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        Page<QuotationResponse> responses = quotationService.getAllQuotations(pageNumber, pageSize, sortBy, sortOrder);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(responses.getContent())
                .pagination(new PageInfo<>(responses))
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/{id}/orders")
    public ResponseEntity<MyApiResponse> createOrderFromQuotation(@PathVariable String id, @RequestBody @Valid OrderCreationFromQuotationRequest request) {
        OrderResponse response = orderService.createOrderFromQuotation(id, request);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MyApiResponse> deleteQuotation(@PathVariable String id) {
        quotationService.deleteQuotation(id);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .message("Quotation deleted successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/summary")
    public ResponseEntity<MyApiResponse> getQuotationStatusSummary() {
        Map<String, Long> summary = quotationService.getQuotationStatusSummary();
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(summary)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<MyApiResponse> searchQuotations(@RequestParam String query,
                                                          @RequestParam(name = "pageNo", required = false, defaultValue = "1") int pageNumber,
                                                          @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                          @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                                                          @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        Page<QuotationResponse> response = quotationService.searchQuotations(query, pageNumber, pageSize, sortBy, sortOrder);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(response.getContent())
                .pagination(new PageInfo<>(response))
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
