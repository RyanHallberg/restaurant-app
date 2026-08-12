package com.ryanhallberg.restaurant.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ryanhallberg.restaurant.common.error.ConflictException;
import com.ryanhallberg.restaurant.common.error.PaymentDeclinedException;
import com.ryanhallberg.restaurant.menu.MenuService;
import com.ryanhallberg.restaurant.menu.dto.MenuItemResponse;
import com.ryanhallberg.restaurant.orders.dto.CreateOrderRequest;
import com.ryanhallberg.restaurant.orders.dto.UpdateOrderStatusRequest;

class OrderServiceTest {

    private OrderRepository repository;
    private MenuService menuService;
    private OrderService service;

    @BeforeEach
    void setUp() {
        repository = mock(OrderRepository.class);
        menuService = mock(MenuService.class);
        service = new OrderService(repository, menuService, new MockPaymentService());
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static MenuItemResponse item(long id, String name, int priceCents) {
        return new MenuItemResponse(id, 1L, name, null, priceCents, null, true);
    }

    private static CreateOrderRequest request(String cardNumber, CreateOrderRequest.OrderLineRequest... lines) {
        return new CreateOrderRequest(List.of(lines),
                new CreateOrderRequest.PaymentRequest(cardNumber, "12/30", "123"));
    }

    @Test
    void totalIsComputedFromServerPricesNotTheClient() {
        when(menuService.listAvailableByIds(anyCollection()))
                .thenReturn(List.of(item(1, "Margherita", 1600), item(2, "Cold Brew", 600)));

        var response = service.create(7L, request("4111111111111111",
                new CreateOrderRequest.OrderLineRequest(1L, 2),
                new CreateOrderRequest.OrderLineRequest(2L, 3)));

        assertThat(response.totalCents()).isEqualTo(1600 * 2 + 600 * 3);
        assertThat(response.status()).isEqualTo("PLACED");
        assertThat(response.paymentReference()).startsWith("MOCK-");
        assertThat(response.items()).hasSize(2);
    }

    @Test
    void unavailableItemRejectsTheWholeOrder() {
        when(menuService.listAvailableByIds(anyCollection()))
                .thenReturn(List.of(item(1, "Margherita", 1600)));

        assertThatThrownBy(() -> service.create(7L, request("4111111111111111",
                new CreateOrderRequest.OrderLineRequest(1L, 1),
                new CreateOrderRequest.OrderLineRequest(99L, 1))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("99");
        verify(repository, never()).save(any());
    }

    @Test
    void ordersAboveTheSanityCeilingAreRejectedBeforePayment() {
        when(menuService.listAvailableByIds(anyCollection()))
                .thenReturn(List.of(item(1, "Gold-Leaf Tasting", 100_000)));

        // 11 lines x $1000 x 20 = $220,000 — over the $10k ceiling.
        var lines = java.util.stream.IntStream.range(0, 11)
                .mapToObj(i -> new CreateOrderRequest.OrderLineRequest(1L, 20))
                .toArray(CreateOrderRequest.OrderLineRequest[]::new);

        assertThatThrownBy(() -> service.create(7L, request("4111111111111111", lines)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("exceeds");
        verify(repository, never()).save(any());
    }

    @Test
    void magicCardDeclinesAndNothingIsSaved() {
        when(menuService.listAvailableByIds(anyCollection()))
                .thenReturn(List.of(item(1, "Margherita", 1600)));

        assertThatThrownBy(() -> service.create(7L, request(MockPaymentService.DECLINE_CARD,
                new CreateOrderRequest.OrderLineRequest(1L, 1))))
                .isInstanceOf(PaymentDeclinedException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void statusFollowsTheLegalStateMachine() {
        var order = new Order(7L, 1000, "MOCK-x");
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        service.updateStatus(1L, new UpdateOrderStatusRequest("PREPARING"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);

        assertThatThrownBy(() -> service.updateStatus(1L, new UpdateOrderStatusRequest("COMPLETED")))
                .isInstanceOf(ConflictException.class);

        service.updateStatus(1L, new UpdateOrderStatusRequest("READY"));
        service.updateStatus(1L, new UpdateOrderStatusRequest("COMPLETED"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);

        assertThatThrownBy(() -> service.updateStatus(1L, new UpdateOrderStatusRequest("CANCELLED")))
                .isInstanceOf(ConflictException.class);
    }
}
