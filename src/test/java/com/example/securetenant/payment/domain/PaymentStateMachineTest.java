package com.example.securetenant.payment.domain;

import com.example.securetenant.shared.api.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentStateMachineTest {

    @Test
    void createdCanAuthorizeFailOrCancel() {
        assertThat(PaymentStateMachine.transition(PaymentStatus.CREATED, PaymentStatus.AUTHORIZED))
                .isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(PaymentStateMachine.transition(PaymentStatus.CREATED, PaymentStatus.FAILED))
                .isEqualTo(PaymentStatus.FAILED);
        assertThat(PaymentStateMachine.transition(PaymentStatus.CREATED, PaymentStatus.CANCELLED))
                .isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void happyPathReachesSettled() {
        PaymentStatus authorized = PaymentStateMachine.transition(PaymentStatus.CREATED, PaymentStatus.AUTHORIZED);
        PaymentStatus captured = PaymentStateMachine.transition(authorized, PaymentStatus.CAPTURED);
        assertThat(PaymentStateMachine.transition(captured, PaymentStatus.SETTLED))
                .isEqualTo(PaymentStatus.SETTLED);
    }

    @Test
    void settledIsTerminal() {
        assertThatThrownBy(() -> PaymentStateMachine.transition(PaymentStatus.SETTLED, PaymentStatus.FAILED))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createdAndAuthorizedAreRetryable() {
        assertThat(PaymentStateMachine.isRetryable(PaymentStatus.CREATED)).isTrue();
        assertThat(PaymentStateMachine.isRetryable(PaymentStatus.AUTHORIZED)).isTrue();
        assertThat(PaymentStateMachine.isRetryable(PaymentStatus.SETTLED)).isFalse();
        assertThat(PaymentStateMachine.isRetryable(PaymentStatus.FAILED)).isFalse();
    }

    @Test
    void createdAndAuthorizedAreCancellable() {
        assertThat(PaymentStateMachine.isCancellable(PaymentStatus.CREATED)).isTrue();
        assertThat(PaymentStateMachine.isCancellable(PaymentStatus.AUTHORIZED)).isTrue();
        assertThat(PaymentStateMachine.isCancellable(PaymentStatus.SETTLED)).isFalse();
        assertThat(PaymentStateMachine.isOpen(PaymentStatus.SETTLED)).isTrue();
        assertThat(PaymentStateMachine.isOpen(PaymentStatus.FAILED)).isFalse();
    }
}
