package com.ryanhallberg.restaurant.orders;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ryanhallberg.restaurant.common.error.PaymentDeclinedException;

/**
 * Stand-in for a real payment provider. Charges always succeed except for the
 * magic decline card, which lets the frontend demo its failure path. Card
 * details are validated for shape upstream and never stored or logged.
 */
@Service
class MockPaymentService {

    static final String DECLINE_CARD = "4000000000000002";

    String charge(String cardNumber, int amountCents) {
        if (DECLINE_CARD.equals(cardNumber)) {
            throw new PaymentDeclinedException("Your card was declined");
        }
        return "MOCK-" + UUID.randomUUID();
    }
}
