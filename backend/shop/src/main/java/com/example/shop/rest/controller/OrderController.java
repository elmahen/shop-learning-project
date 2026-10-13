package com.example.shop.rest.controller;

import com.example.shop.rest.dto.*;
import com.example.shop.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        var order = orderService.createOrder(request.getCustomerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        var order = orderService.getOrder(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderPositionResponse> addItem(
            @PathVariable Long id,
            @RequestBody AddItemRequest request) {
        var position = orderService.addItemToOrder(id, request.getArticleId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderPositionResponse.from(position));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        orderService.removeItemFromOrder(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        var order = orderService.cancelOrder(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/{id}/place")
    public ResponseEntity<OrderResponse> placeOrder(@PathVariable Long id) {
        var order = orderService.placeOrder(id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}