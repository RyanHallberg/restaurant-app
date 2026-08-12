package com.ryanhallberg.restaurant.orders;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ryanhallberg.restaurant.common.web.PageResponse;
import com.ryanhallberg.restaurant.orders.dto.CreateOrderRequest;
import com.ryanhallberg.restaurant.orders.dto.OrderResponse;
import com.ryanhallberg.restaurant.orders.dto.UpdateOrderStatusRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Online ordering with mock payment")
public class OrderController {

    private static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createOrder", summary = "Place an order; prices are recomputed server-side. 402 when the mock card declines",
            security = @SecurityRequirement(name = "bearerAuth"))
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, @AuthenticationPrincipal Jwt jwt) {
        return orderService.create(Long.parseLong(jwt.getSubject()), request);
    }

    @GetMapping("/my")
    @Operation(operationId = "listMyOrders", summary = "The caller's orders, newest first", security = @SecurityRequirement(name = "bearerAuth"))
    public PageResponse<OrderResponse> myOrders(
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.myOrders(Long.parseLong(jwt.getSubject()), pageable);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getOrder", summary = "A single order (owner or admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public OrderResponse get(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        boolean admin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(ROLE_ADMIN);
        return orderService.get(id, Long.parseLong(jwt.getSubject()), admin);
    }

    @GetMapping
    @Operation(operationId = "listOrders", summary = "List orders, filterable by status (admin)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public PageResponse<OrderResponse> adminList(
            @RequestParam(required = false) OrderStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.adminList(status, pageable);
    }

    @PatchMapping("/{id}/status")
    @Operation(operationId = "updateOrderStatus", summary = "Advance an order through PLACED -> PREPARING -> READY -> COMPLETED, or cancel (admin)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public OrderResponse updateStatus(@PathVariable long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request);
    }
}
