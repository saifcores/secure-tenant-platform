package com.example.securetenant.order.domain;

import com.example.securetenant.shared.api.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateMachineTest {

        @Test
        void createdCanBeConfirmedOrCancelled() {
                assertThat(OrderStateMachine.transition(OrderStatus.CREATED, OrderStatus.CONFIRMED))
                                .isEqualTo(OrderStatus.CONFIRMED);
                assertThat(OrderStateMachine.transition(OrderStatus.CREATED, OrderStatus.CANCELLED))
                                .isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        void confirmedCanBeCompleted() {
                assertThat(OrderStateMachine.transition(OrderStatus.CONFIRMED, OrderStatus.COMPLETED))
                                .isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        void terminalStatesRejectTransitions() {
                assertThatThrownBy(() -> OrderStateMachine.transition(OrderStatus.CANCELLED, OrderStatus.CREATED))
                                .isInstanceOf(BusinessRuleException.class);
                assertThatThrownBy(() -> OrderStateMachine.transition(OrderStatus.COMPLETED, OrderStatus.CANCELLED))
                                .isInstanceOf(BusinessRuleException.class);
        }
}
