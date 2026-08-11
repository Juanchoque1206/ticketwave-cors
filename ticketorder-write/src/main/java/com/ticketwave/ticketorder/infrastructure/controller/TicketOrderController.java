package com.ticketwave.ticketorder.infrastructure.controller;

import com.ticketwave.ticketorder.application.AuthenticationContext;
import com.ticketwave.ticketorder.application.CancelOrderUseCase;
import com.ticketwave.ticketorder.application.CreateOrderUseCase;
import com.ticketwave.ticketorder.application.OrderService;
import com.ticketwave.ticketorder.infrastructure.dto.order.CreateOrderRequest;
import com.ticketwave.ticketorder.infrastructure.dto.order.OrderResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Public API of the TicketOrder service: owns the reservation lifecycle. It is
 * the single place that persists orders; the monolith only observes the order
 * through RabbitMQ domain events.
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Ticket Orders", description = "Reservation + purchase flow (TicketOrder microservice)")
public class TicketOrderController {

    private static final String DEFAULT_USERNAME = "demo";
    private static final String DEFAULT_IP = "unknown";

    private final OrderService orderService;
    private final CreateOrderUseCase createOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    public TicketOrderController(OrderService orderService,
                                 CreateOrderUseCase createOrderUseCase,
                                 CancelOrderUseCase cancelOrderUseCase) {
        this.orderService = orderService;
        this.createOrderUseCase = createOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> reserve(@RequestBody CreateOrderRequest request) {
        AuthenticationContext ctx = new AuthenticationContext(DEFAULT_USERNAME, DEFAULT_IP);
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrderUseCase.reserve(ctx, request));
    }

    /*@GetMapping
    public ResponseEntity<List<OrderResponse>> myOrders() {
        AuthenticationContext ctx = new AuthenticationContext(DEFAULT_USERNAME, DEFAULT_IP);
        return ResponseEntity.ok(orderService.listOrdersForUser(ctx));
    }*/

    /*@GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> get(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }*/

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID orderId) {
        cancelOrderUseCase.cancel(orderId);
        return ResponseEntity.noContent().build();
    }
}
