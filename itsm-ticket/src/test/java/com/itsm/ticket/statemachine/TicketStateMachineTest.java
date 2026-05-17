package com.itsm.ticket.statemachine;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.itsm.ticket.enums.TicketStatus;
import com.itsm.ticket.enums.TicketTransition;
import static org.junit.jupiter.api.Assertions.*;

class TicketStateMachineTest
{
    private TicketStateMachine stateMachine;

    @BeforeEach
    void setUp()
    {
        stateMachine = new TicketStateMachine();
    }

    @Test
    @DisplayName("DRAFT -> SUBMITTED 转换合法")
    void canTransit_DraftToSubmitted_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.DRAFT, TicketStatus.SUBMITTED));
    }

    @Test
    @DisplayName("DRAFT -> APPROVED 转换非法")
    void canTransit_DraftToApproved_ReturnsFalse()
    {
        assertFalse(stateMachine.canTransit(TicketStatus.DRAFT, TicketStatus.APPROVED));
    }

    @Test
    @DisplayName("SUBMITTED -> APPROVED 转换合法")
    void canTransit_SubmittedToApproved_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.SUBMITTED, TicketStatus.APPROVED));
    }

    @Test
    @DisplayName("SUBMITTED -> REJECTED 转换合法")
    void canTransit_SubmittedToRejected_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.SUBMITTED, TicketStatus.REJECTED));
    }

    @Test
    @DisplayName("APPROVED -> ASSIGNED 转换合法")
    void canTransit_ApprovedToAssigned_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.APPROVED, TicketStatus.ASSIGNED));
    }

    @Test
    @DisplayName("ASSIGNED -> PROCESSING 转换合法")
    void canTransit_AssignedToProcessing_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.ASSIGNED, TicketStatus.PROCESSING));
    }

    @Test
    @DisplayName("PROCESSING -> RESOLVED 转换合法")
    void canTransit_ProcessingToResolved_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.PROCESSING, TicketStatus.RESOLVED));
    }

    @Test
    @DisplayName("RESOLVED -> VERIFIED 转换合法")
    void canTransit_ResolvedToVerified_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.RESOLVED, TicketStatus.VERIFIED));
    }

    @Test
    @DisplayName("RESOLVED -> PROCESSING 重新处理转换合法")
    void canTransit_ResolvedToProcessing_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.RESOLVED, TicketStatus.PROCESSING));
    }

    @Test
    @DisplayName("VERIFIED -> CLOSED 转换合法")
    void canTransit_VerifiedToClosed_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.VERIFIED, TicketStatus.CLOSED));
    }

    @Test
    @DisplayName("REJECTED -> DRAFT 重新编辑转换合法")
    void canTransit_RejectedToDraft_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.REJECTED, TicketStatus.DRAFT));
    }

    @Test
    @DisplayName("DRAFT -> CANCELLED 取消转换合法")
    void canTransit_DraftToCancelled_ReturnsTrue()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.DRAFT, TicketStatus.CANCELLED));
    }

    @Test
    @DisplayName("CLOSED -> DRAFT 已关闭不可转换")
    void canTransit_ClosedToDraft_ReturnsFalse()
    {
        assertFalse(stateMachine.canTransit(TicketStatus.CLOSED, TicketStatus.DRAFT));
    }

    @Test
    @DisplayName("CANCELLED -> DRAFT 已取消不可转换")
    void canTransit_CancelledToDraft_ReturnsFalse()
    {
        assertFalse(stateMachine.canTransit(TicketStatus.CANCELLED, TicketStatus.DRAFT));
    }

    @Test
    @DisplayName("validateTransition 合法转换返回转换枚举")
    void validateTransition_ValidTransition_ReturnsTransition()
    {
        TicketTransition transition = stateMachine.validateTransition(TicketStatus.DRAFT, TicketStatus.SUBMITTED);
        assertNotNull(transition);
        assertEquals(TicketTransition.SUBMIT, transition);
        assertEquals("提交工单", transition.getAction());
    }

    @Test
    @DisplayName("validateTransition 非法转换抛出异常")
    void validateTransition_InvalidTransition_ThrowsException()
    {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            stateMachine.validateTransition(TicketStatus.DRAFT, TicketStatus.APPROVED));
        assertTrue(exception.getMessage().contains("不允许从状态"));
    }

    @Test
    @DisplayName("DRAFT状态的可用目标状态包含SUBMITTED和CANCELLED")
    void getAvailableTargetStatuses_Draft_ReturnsSubmittedAndCancelled()
    {
        List<TicketStatus> targets = stateMachine.getAvailableTargetStatuses(TicketStatus.DRAFT);
        assertEquals(2, targets.size());
        assertTrue(targets.contains(TicketStatus.SUBMITTED));
        assertTrue(targets.contains(TicketStatus.CANCELLED));
    }

    @Test
    @DisplayName("SUBMITTED状态的可用目标状态包含APPROVED/REJECTED/CANCELLED")
    void getAvailableTargetStatuses_Submitted_ReturnsThreeTargets()
    {
        List<TicketStatus> targets = stateMachine.getAvailableTargetStatuses(TicketStatus.SUBMITTED);
        assertEquals(3, targets.size());
        assertTrue(targets.contains(TicketStatus.APPROVED));
        assertTrue(targets.contains(TicketStatus.REJECTED));
        assertTrue(targets.contains(TicketStatus.CANCELLED));
    }

    @Test
    @DisplayName("CLOSED状态无可用转换")
    void getAvailableTargetStatuses_Closed_ReturnsEmpty()
    {
        List<TicketStatus> targets = stateMachine.getAvailableTargetStatuses(TicketStatus.CLOSED);
        assertTrue(targets.isEmpty());
    }

    @Test
    @DisplayName("CANCELLED状态无可用转换")
    void getAvailableTargetStatuses_Cancelled_ReturnsEmpty()
    {
        List<TicketStatus> targets = stateMachine.getAvailableTargetStatuses(TicketStatus.CANCELLED);
        assertTrue(targets.isEmpty());
    }

    @Test
    @DisplayName("PROCESSING状态的可用目标状态包含RESOLVED、ASSIGNED和CANCELLED")
    void getAvailableTargetStatuses_Processing_ReturnsResolvedAndCancelled()
    {
        List<TicketStatus> targets = stateMachine.getAvailableTargetStatuses(TicketStatus.PROCESSING);
        assertEquals(3, targets.size());
        assertTrue(targets.contains(TicketStatus.RESOLVED));
        assertTrue(targets.contains(TicketStatus.CANCELLED));
        assertTrue(targets.contains(TicketStatus.ASSIGNED));
    }

    @Test
    @DisplayName("完整生命周期 DRAFT->SUBMITTED->APPROVED->ASSIGNED->PROCESSING->RESOLVED->VERIFIED->CLOSED")
    void fullLifecycle_AllTransitionsValid()
    {
        assertTrue(stateMachine.canTransit(TicketStatus.DRAFT, TicketStatus.SUBMITTED));
        assertTrue(stateMachine.canTransit(TicketStatus.SUBMITTED, TicketStatus.APPROVED));
        assertTrue(stateMachine.canTransit(TicketStatus.APPROVED, TicketStatus.ASSIGNED));
        assertTrue(stateMachine.canTransit(TicketStatus.ASSIGNED, TicketStatus.PROCESSING));
        assertTrue(stateMachine.canTransit(TicketStatus.PROCESSING, TicketStatus.RESOLVED));
        assertTrue(stateMachine.canTransit(TicketStatus.RESOLVED, TicketStatus.VERIFIED));
        assertTrue(stateMachine.canTransit(TicketStatus.VERIFIED, TicketStatus.CLOSED));
    }
}
