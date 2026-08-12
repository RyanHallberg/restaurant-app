package com.ryanhallberg.restaurant.orders;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanhallberg.restaurant.common.error.ConflictException;
import com.ryanhallberg.restaurant.common.error.ForbiddenException;
import com.ryanhallberg.restaurant.common.error.NotFoundException;
import com.ryanhallberg.restaurant.common.web.PageResponse;
import com.ryanhallberg.restaurant.menu.MenuService;
import com.ryanhallberg.restaurant.menu.dto.MenuItemResponse;
import com.ryanhallberg.restaurant.orders.dto.CreateOrderRequest;
import com.ryanhallberg.restaurant.orders.dto.OrderResponse;
import com.ryanhallberg.restaurant.orders.dto.UpdateOrderStatusRequest;

@Service
public class OrderService {

    /** $10,000 — far above any real restaurant order; a sanity ceiling, not a business rule. */
    static final long MAX_ORDER_CENTS = 1_000_000L;

    private final OrderRepository orderRepository;
    private final MenuService menuService;
    private final MockPaymentService paymentService;

    OrderService(OrderRepository orderRepository, MenuService menuService, MockPaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.menuService = menuService;
        this.paymentService = paymentService;
    }

    @Transactional
    public OrderResponse create(long userId, CreateOrderRequest request) {
        var requestedIds = request.items().stream()
                .map(CreateOrderRequest.OrderLineRequest::menuItemId)
                .toList();
        // Server-side re-pricing: the client cart is a convenience, never the
        // source of truth for prices or availability.
        var itemsById = menuService.listAvailableByIds(requestedIds).stream()
                .collect(Collectors.toMap(MenuItemResponse::id, Function.identity()));

        // Money math in long with exact ops: request size and item prices are
        // validation-capped, but an int accumulator wrapping silently is the
        // kind of bug that must be structurally impossible, not just unlikely.
        long totalCents = 0;
        for (var line : request.items()) {
            var item = itemsById.get(line.menuItemId());
            if (item == null) {
                throw new ConflictException(
                        "An item in your cart is no longer available (id %d)".formatted(line.menuItemId()));
            }
            totalCents = Math.addExact(totalCents, (long) item.priceCents() * line.quantity());
        }
        if (totalCents > MAX_ORDER_CENTS) {
            throw new ConflictException("Order total exceeds what we can accept online — please call us");
        }

        var paymentReference = paymentService.charge(request.payment().cardNumber(), (int) totalCents);

        var order = new Order(userId, (int) totalCents, paymentReference);
        for (var line : request.items()) {
            var item = itemsById.get(line.menuItemId());
            order.addItem(new OrderItem(order, item.id(), item.name(), item.priceCents(), line.quantity()));
        }
        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(long id, long userId, boolean admin) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order %d not found".formatted(id)));
        if (!admin && !Long.valueOf(userId).equals(order.getUserId())) {
            throw new ForbiddenException("This order belongs to someone else");
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(long userId, Pageable pageable) {
        return PageResponse.from(orderRepository.findByUserId(userId, pageable).map(OrderService::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> adminList(@Nullable OrderStatus status, Pageable pageable) {
        var page = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return PageResponse.from(page.map(OrderService::toResponse));
    }

    @Transactional
    public OrderResponse updateStatus(long id, UpdateOrderStatusRequest request) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order %d not found".formatted(id)));
        OrderStatus target;
        try {
            target = OrderStatus.valueOf(request.status());
        } catch (IllegalArgumentException ex) {
            throw new ConflictException("Unknown status: " + request.status());
        }
        if (!order.getStatus().canTransitionTo(target)) {
            throw new ConflictException(
                    "Cannot move a %s order to %s".formatted(order.getStatus(), target));
        }
        order.updateStatus(target);
        return toResponse(order);
    }

    private static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalCents(),
                order.getPaymentReference(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new OrderResponse.OrderLineResponse(
                                item.getItemName(), item.getPriceCents(), item.getQuantity()))
                        .toList());
    }
}
