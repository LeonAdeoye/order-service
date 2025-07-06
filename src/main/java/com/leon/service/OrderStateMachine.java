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
        transitionMap.put(Pair.of(OrderStates.PENDING_NEW, OrderStateEvents.OMS_ACCEPT), OrderStates.ACCEPTED_BY_OMS);
        transitionMap.put(Pair.of(OrderStates.PENDING_NEW, OrderStateEvents.OMS_REJECT), OrderStates.REJECTED_BY_OMS);

        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_OMS, OrderStateEvents.DESK_APPROVE), OrderStates.ACCEPTED_BY_DESK);
        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_OMS, OrderStateEvents.DESK_REJECT), OrderStates.REJECTED_BY_DESK);
        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_OMS, OrderStateEvents.DESK_CANCEL), OrderStates.CANCELLED_BY_DESK);
        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_OMS, OrderStateEvents.DESK_REPLACE), OrderStates.REPLACED_BY_DESK);

        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_DESK, OrderStateEvents.SUBMIT_TO_EXCH), OrderStates.PENDING_EXCH);
        transitionMap.put(Pair.of(OrderStates.PENDING_EXCH, OrderStateEvents.EXCH_APPROVE), OrderStates.ACCEPTED_BY_EXCH);
        transitionMap.put(Pair.of(OrderStates.PENDING_EXCH, OrderStateEvents.EXCH_REJECT), OrderStates.REJECTED_BY_EXCH);

        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_EXCH, OrderStateEvents.DESK_CANCEL), OrderStates.PENDING_CANCEL);
        transitionMap.put(Pair.of(OrderStates.PENDING_CANCEL, OrderStateEvents.EXCH_APPROVE), OrderStates.CANCELLED_BY_EXCH);
        transitionMap.put(Pair.of(OrderStates.PENDING_CANCEL, OrderStateEvents.EXCH_REJECT), OrderStates.CANCEL_REJECTED_BY_EXCH);

        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_EXCH, OrderStateEvents.DESK_REPLACE), OrderStates.PENDING_REPLACE);
        transitionMap.put(Pair.of(OrderStates.PENDING_REPLACE, OrderStateEvents.EXCH_APPROVE), OrderStates.REPLACED_BY_EXCH);
        transitionMap.put(Pair.of(OrderStates.PENDING_REPLACE, OrderStateEvents.EXCH_REJECT), OrderStates.REPLACE_REJECTED_BY_EXCH);

        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_EXCH, OrderStateEvents.FULL_FILL), OrderStates.FULLY_FILLED);
        transitionMap.put(Pair.of(OrderStates.ACCEPTED_BY_EXCH, OrderStateEvents.PARTIAL_FILL), OrderStates.PARTIALLY_FILLED);
        transitionMap.put(Pair.of(OrderStates.PARTIALLY_FILLED, OrderStateEvents.FULL_FILL), OrderStates.FULLY_FILLED);

        transitionMap.put(Pair.of(OrderStates.PARTIALLY_FILLED, OrderStateEvents.DESK_CANCEL), OrderStates.PENDING_CANCEL);
        transitionMap.put(Pair.of(OrderStates.PENDING_CANCEL, OrderStateEvents.EXCH_APPROVE), OrderStates.CANCELLED_BY_EXCH);

        transitionMap.put(Pair.of(OrderStates.PARTIALLY_FILLED, OrderStateEvents.DESK_REPLACE), OrderStates.PENDING_REPLACE);
        transitionMap.put(Pair.of(OrderStates.PENDING_REPLACE, OrderStateEvents.EXCH_APPROVE), OrderStates.REPLACED_BY_EXCH);

        transitionMap.put(Pair.of(OrderStates.REPLACED_BY_EXCH, OrderStateEvents.FULL_FILL), OrderStates.FULLY_FILLED);
        transitionMap.put(Pair.of(OrderStates.REPLACED_BY_EXCH, OrderStateEvents.PARTIAL_FILL), OrderStates.PARTIALLY_FILLED);
    }

    public static Optional<OrderStates> getNextState(OrderStates currentState, OrderStateEvents event) {
        return Optional.ofNullable(transitionMap.get(Pair.of(currentState, event)));
    }
}
