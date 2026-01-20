package com.crm.project.controller;

import com.crm.project.dto.response.MyApiResponse;
import com.crm.project.dto.response.OrderResponse;
import com.crm.project.internal.PageInfo;
import com.crm.project.service.OrderService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<MyApiResponse> getOrderDetails(@PathVariable String id) {
        OrderResponse response = orderService.getOrderDetails(id);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<MyApiResponse> getOrders(@RequestParam(name = "pageNo", required = false, defaultValue = "1") int pageNumber,
                                                   @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                   @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                                                   @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        Page<OrderResponse> responses = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(responses.getContent())
                .pagination(new PageInfo<>(responses))
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MyApiResponse> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .message("Order deleted successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<MyApiResponse> completeOrder(@PathVariable String id) {
        OrderResponse response = orderService.completeOrder(id);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/summary")
    public ResponseEntity<MyApiResponse> getOrdersSummary() {
        Map<String, Long> response = orderService.getOrdersSummary();
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MyApiResponse> cancelOrder(@PathVariable String id) {
        OrderResponse response = orderService.cancelOrder(id);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(response)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<MyApiResponse> searchOrders(@RequestParam String query,
                                                      @RequestParam(name = "pageNo", required = false, defaultValue = "1") int pageNumber,
                                                      @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                                                      @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        Page<OrderResponse> responses = orderService.searchOrders(query, pageNumber, pageSize, sortBy, sortOrder);
        MyApiResponse apiResponse = MyApiResponse.builder()
                .data(responses.getContent())
                .pagination(new PageInfo<>(responses))
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
