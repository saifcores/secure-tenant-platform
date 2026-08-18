package com.example.securetenant.payment.domain;

import com.example.securetenant.shared.api.BusinessRuleException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class PaymentStateMachine {

    private static final Map<PaymentStatus, Set<PaymentStatus>> TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        TRANSITIONS.put(PaymentStatus.CREATED, EnumSet.of(PaymentStatus.AUTHORIZED, PaymentStatus.FAILED, PaymentStatus.CANCELLED));
        TRANSITIONS.put(PaymentStatus.AUTHORIZED, EnumSet.of(PaymentStatus.CAPTURED, PaymentStatus.FAILED, PaymentStatus.CANCELLED));
        TRANSITIONS.put(PaymentStatus.CAPTURED, EnumSet.of(PaymentStatus.SETTLED, PaymentStatus.FAILED));
        TRANSITIONS.put(PaymentStatus.SETTLED, EnumSet.noneOf(PaymentStatus.class));
        TRANSITIONS.put(PaymentStatus.FAILED, EnumSet.noneOf(PaymentStatus.class));
        TRANSITIONS.put(PaymentStatus.CANCELLED, EnumSet.noneOf(PaymentStatus.class));
    }

    private PaymentStateMachine() {
    }

    public static PaymentStatus transition(PaymentStatus current, PaymentStatus target) {
        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BusinessRuleException("Cannot transition payment from " + current + " to " + target);
        }
        return target;
    }

    public static boolean isRetryable(PaymentStatus status) {
        return status == PaymentStatus.CREATED || status == PaymentStatus.AUTHORIZED;
    }

    public static boolean isCancellable(PaymentStatus status) {
        return status == PaymentStatus.CREATED || status == PaymentStatus.AUTHORIZED;
    }

    public static boolean isOpen(PaymentStatus status) {
        return status == PaymentStatus.CREATED
                || status == PaymentStatus.AUTHORIZED
                || status == PaymentStatus.CAPTURED
                || status == PaymentStatus.SETTLED;
    }
}
