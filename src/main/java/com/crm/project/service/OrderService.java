package com.crm.project.service;

import com.crm.project.dto.request.OrderCreationFromQuotationRequest;
import com.crm.project.dto.response.OrderResponse;
import com.crm.project.entity.*;
import com.crm.project.exception.AppException;
import com.crm.project.exception.ErrorCode;
import com.crm.project.mapper.OrderMapper;
import com.crm.project.repository.*;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final QuotationRepository quotationRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final LeadRepository leadRepository;
    private final ProductRepository productRepository;
    private final StageService stageService;

    @Transactional
    public OrderResponse createOrderFromQuotation(String quotationId, OrderCreationFromQuotationRequest request) {
        String threadName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();

        Quotation quotation = quotationRepository.findQuotationDetailById(quotationId)
                .orElseThrow(() -> new AppException(ErrorCode.QUOTATION_NOT_FOUND));

        if (orderRepository.existsByQuotationId(quotationId)) {
            System.out.println("[" + threadName + "] ✗ Quotation already has order (detected by check)");
            System.out.println("========================================\n");
            throw new AppException(ErrorCode.QUOTATION_ORDER_EXISTED);
        }

        if (orderRepository.existsByOrderCode(request.getOrderCode())) {
            System.out.println("[" + threadName + "] ✗ Quotation code existed (detected by check)");
            System.out.println("========================================\n");
            throw new AppException(ErrorCode.ORDER_CODE_EXISTED);
        }

        this.updateProductQuantity(quotation.getItems());

        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<OrderItem> orderItems = quotation.getItems().stream().map(orderMapper::fromQuotationItemToOrderItem)
                .toList();

        Order order = Order.builder()
                .orderCode(request.getOrderCode())
                .shippingAddress(request.getShippingAddress())
                .totalAmount(quotation.getFinalTotal())
                .status("Pending")
                .quotation(quotation)
                .orderItems(orderItems)
                .lead(quotation.getLead())
                .createdBy(user)
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);

        orderRepository.save(order);
        System.out.println("[" + threadName + "] ✓ Order saved");

        quotationRepository.save(quotation);

        OrderResponse orderResponse = orderMapper.toOrderResponse(order);
        orderResponse.setBuyerName(order.getLead().getFullName());
        orderResponse.setItems(orderItems.stream().map(orderMapper::fromOrderItemToOrderItemInfo).toList());
        return orderResponse;
    }

    public OrderResponse getOrderDetails(String id) {
        Order order = orderRepository.findOrderWithRelations(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    public Page<OrderResponse> getAllOrders(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Page<String> idPage = orderRepository.findAllIds(pageable);
        List<String> ids = idPage.getContent();

        if (ids.isEmpty()) {
            throw new AppException(ErrorCode.NO_RESULTS);
        }

        List<Order> orders = orderRepository.findAllOrdersWithDetails(ids);
        Map<String, Order> orderMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));

        List<OrderResponse> sortedResponses = ids.stream()
                .map(orderMap::get) // Lấy Order từ Map theo thứ tự ID
                .filter(Objects::nonNull) // Đề phòng null
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(sortedResponses, pageable, idPage.getTotalElements());
    }

    @Transactional
    public void deleteOrder(String id) {
        if (!orderRepository.existsById(id)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        orderRepository.deleteById(id);
    }

    @Transactional
    public OrderResponse completeOrder(String id) {
        Order order = orderRepository.findOrderWithRelations(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        Lead lead = order.getLead();
        leadRepository.updateStatus(lead.getId());
        order.setStatus("Delivered");
        order.setLead(lead);
        orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

    public Map<String, Long> getOrdersSummary() {
        List<Map<String, Object>> count = orderRepository.countByStatus();
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> map : count) {
            result.put((String) map.get("status"), (long) map.get("total"));
        }
        BigDecimal totalAmount = orderRepository.sumTotalAmount();
        result.put("totalAmount", totalAmount.toBigInteger().longValue());
        return result;
    }

    @Transactional
    public OrderResponse cancelOrder(String id) {
        Order order = orderRepository.findOrderWithRelations(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus("Cancelled");
        orderRepository.save(order);
        return orderMapper.toOrderResponse(order);
    }

    public Page<OrderResponse> searchOrders(String query, int pageNumber, int pageSize, String sortBy,
                                            String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        Page<String> idPage = orderRepository.findIdsBySearch(query, pageable);
        List<String> ids = idPage.getContent();
        if (ids.isEmpty()) {
            throw new AppException(ErrorCode.NO_RESULTS);
        }
        List<Order> orders = orderRepository.findAllOrdersWithDetails(ids);

        Map<String, Order> orderMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));

        List<OrderResponse> sortedResponses = ids.stream()
                .map(orderMap::get) // Lấy Order từ Map theo thứ tự ID
                .filter(Objects::nonNull) // Đề phòng null
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(sortedResponses, pageable, idPage.getTotalElements());
    }

    private void updateProductQuantity(List<QuotationItem> items) {

        Map<String, Integer> requiredQuantity = new HashMap<>();
        Map<String, Product> productMap = new HashMap<>();

        for (QuotationItem item : items) {
            Product product = item.getProduct();
            String productId = product.getId();

            productMap.putIfAbsent(productId, product);
            requiredQuantity.merge(productId, item.getQuantity(), Integer::sum);
        }

        List<String> sortedProductIds = requiredQuantity.keySet()
                .stream()
                .sorted()
                .toList();

        Set<String> failedProducts = new HashSet<>();
        List<Product> productsToUpdate = new ArrayList<>();

        for (String productId : sortedProductIds) {

            Product product = productMap.get(productId);
            int needed = requiredQuantity.get(productId);

            if (product.getQuantity() < needed) {
                failedProducts.add(product.getName());
            } else {
                int newQuantity = product.getQuantity() - needed;
                product.setQuantity(newQuantity);

                String newStatus = updateProductStatusOnQuantity(newQuantity);
                if (!newStatus.equals(product.getStatus())) {
                    product.setStatus(newStatus);
                }

                productsToUpdate.add(product);
            }
        }

        if (!failedProducts.isEmpty()) {
            System.out.println("OUT OF STOCK");
            throw new AppException(ErrorCode.OUT_OF_STOCK, failedProducts.toString());
        }

        try {
            productRepository.saveAll(productsToUpdate);
            productRepository.flush();
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            System.out.println("OptimisticLockException");
            throw new AppException(ErrorCode.PRODUCT_CONFLICT);
        }
    }

    private String updateProductStatusOnQuantity(int quantity) {
        if (quantity == 0) {
            return "Out of Stock";
        } else if (quantity <= 10) {
            return "Low Stock";
        }
        return "Active";
    }
}
