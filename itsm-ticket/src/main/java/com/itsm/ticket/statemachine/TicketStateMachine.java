package com.itsm.ticket.statemachine;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.itsm.ticket.enums.TicketStatus;
import com.itsm.ticket.enums.TicketTransition;

@Component
public class TicketStateMachine
{
    private final Map<TicketStatus, List<TicketTransition>> transitionMap = new EnumMap<>(TicketStatus.class);

    public TicketStateMachine()
    {
        for (TicketTransition transition : TicketTransition.values())
        {
            transitionMap.computeIfAbsent(transition.getFrom(), k -> new ArrayList<>()).add(transition);
        }
    }

    public boolean canTransit(TicketStatus from, TicketStatus to)
    {
        List<TicketTransition> transitions = transitionMap.get(from);
        if (transitions == null) return false;
        return transitions.stream().anyMatch(t -> t.getTo() == to);
    }

    public TicketTransition validateTransition(TicketStatus from, TicketStatus to)
    {
        List<TicketTransition> transitions = transitionMap.get(from);
        if (transitions != null)
        {
            for (TicketTransition t : transitions)
            {
                if (t.getTo() == to) return t;
            }
        }
        throw new IllegalStateException(
            String.format("不允许从状态[%s]转换到[%s]", from.getDesc(), to.getDesc()));
    }

    public List<TicketTransition> getAvailableTransitions(TicketStatus currentStatus)
    {
        return transitionMap.getOrDefault(currentStatus, new ArrayList<>());
    }

    public List<TicketStatus> getAvailableTargetStatuses(TicketStatus currentStatus)
    {
        List<TicketTransition> transitions = getAvailableTransitions(currentStatus);
        List<TicketStatus> targets = new ArrayList<>();
        for (TicketTransition transition : transitions)
        {
            targets.add(transition.getTo());
        }
        return targets;
    }
}
