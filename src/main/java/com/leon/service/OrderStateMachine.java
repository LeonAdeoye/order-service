package com.leon.service;

import com.leon.model.OrderStateEvents;
import com.leon.model.OrderStates;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderStateMachine
{
    private static final Map<Pair<OrderStates, OrderStateEvents>, OrderStates> transitionMap = new HashMap<>();

    static
    {
        transitionMap.put(Pair.of(OrderStates.NEW_ORDER, OrderStateEvents.SUBMIT_TO_DESK), OrderStates.PENDING_NEW);
        transitionMap.put(Pair.of(OrderStates.PENDING_NEW, OrderStateEvents.OMS_ACCEPT), OrderStates.NEW_ACK);
        transitionMap.put(Pair.of(OrderStates.PENDING_NEW, OrderStateEvents.OMS_REJECT), OrderStates.REJECTED_BY_OMS);
        transitionMap.put(Pair.of(OrderStates.NEW_ACK, OrderStateEvents.DESK_APPROVE), OrderStates.ACCEPTED_BY_DESK);
        transitionMap.put(Pair.of(OrderStates.NEW_ACK, OrderStateEvents.DESK_REJECT), OrderStates.REJECTED_BY_DESK);
        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_DESK, OrderStateEvents.SUBMIT_TO_EXCHANGE), OrderStates.SENT_TO_EXCHANGE);
        transitionMap.put(Pair.of(OrderStates.SENT_TO_EXCHANGE, OrderStateEvents.EXCHANGE_ACKNOWLEDGE), OrderStates.ACKNOWLEDGED_BY_EXCHANGE);
        transitionMap.put(Pair.of(OrderStates.SENT_TO_EXCHANGE, OrderStateEvents.EXCHANGE_REJECT), OrderStates.REJECTED_BY_EXCHANGE);
    }

    public static Optional<OrderStates> getNextState(OrderStates currentState, OrderStateEvents event) {
        return Optional.ofNullable(transitionMap.get(Pair.of(currentState, event)));
    }
}
