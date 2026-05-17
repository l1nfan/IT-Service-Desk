package com.itsm.ticket.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TicketTransitionTest
{
    @Test
    @DisplayName("findTransition-查找存在的转换")
    void findTransition_ExistingTransition_ReturnsTransition()
    {
        TicketTransition transition = TicketTransition.findTransition(TicketStatus.DRAFT, TicketStatus.SUBMITTED);
        assertNotNull(transition);
        assertEquals(TicketTransition.SUBMIT, transition);
        assertEquals("提交工单", transition.getAction());
    }

    @Test
    @DisplayName("findTransition-查找不存在的转换返回null")
    void findTransition_NonExistingTransition_ReturnsNull()
    {
        TicketTransition transition = TicketTransition.findTransition(TicketStatus.DRAFT, TicketStatus.APPROVED);
        assertNull(transition);
    }

    @Test
    @DisplayName("isValidTransition-合法转换返回true")
    void isValidTransition_ValidTransition_ReturnsTrue()
    {
        assertTrue(TicketTransition.isValidTransition(TicketStatus.DRAFT, TicketStatus.SUBMITTED));
        assertTrue(TicketTransition.isValidTransition(TicketStatus.SUBMITTED, TicketStatus.APPROVED));
        assertTrue(TicketTransition.isValidTransition(TicketStatus.SUBMITTED, TicketStatus.REJECTED));
    }

    @Test
    @DisplayName("isValidTransition-非法转换返回false")
    void isValidTransition_InvalidTransition_ReturnsFalse()
    {
        assertFalse(TicketTransition.isValidTransition(TicketStatus.CLOSED, TicketStatus.DRAFT));
        assertFalse(TicketTransition.isValidTransition(TicketStatus.CANCELLED, TicketStatus.DRAFT));
        assertFalse(TicketTransition.isValidTransition(TicketStatus.DRAFT, TicketStatus.CLOSED));
    }

    @Test
    @DisplayName("枚举值总数应为17")
    void values_CountIsSeventeen()
    {
        assertEquals(17, TicketTransition.values().length);
    }

    @Test
    @DisplayName("每个转换的from和to不同(转派除外)")
    void eachTransition_FromAndToDifferent()
    {
        for (TicketTransition transition : TicketTransition.values())
        {
            if (transition == TicketTransition.REASSIGN_ASSIGNED)
            {
                assertEquals(transition.getFrom(), transition.getTo(),
                    "转派转换 " + transition.name() + " 的from和to应相同");
                continue;
            }
            assertNotEquals(transition.getFrom(), transition.getTo(),
                "转换 " + transition.name() + " 的from和to不应相同");
        }
    }

    @Test
    @DisplayName("每个转换都有权限标识")
    void eachTransition_HasPermission()
    {
        for (TicketTransition transition : TicketTransition.values())
        {
            assertNotNull(transition.getPermission());
            assertTrue(transition.getPermission().startsWith("itsm:"),
                "转换 " + transition.name() + " 的权限应以itsm:开头");
        }
    }

    @Test
    @DisplayName("BUG-014: REOPEN转换使用approve权限而非edit权限")
    void reopen_UsesApprovePermission()
    {
        assertEquals("itsm:ticket:approve", TicketTransition.REOPEN.getPermission());
    }

    @Test
    @DisplayName("BUG-014: VERIFY转换使用approve权限")
    void verify_UsesApprovePermission()
    {
        assertEquals("itsm:ticket:approve", TicketTransition.VERIFY.getPermission());
    }

    @Test
    @DisplayName("BUG-014: CLOSE转换使用approve权限")
    void close_UsesApprovePermission()
    {
        assertEquals("itsm:ticket:approve", TicketTransition.CLOSE.getPermission());
    }

    @Test
    @DisplayName("SUBMIT转换使用edit权限")
    void submit_UsesEditPermission()
    {
        assertEquals("itsm:ticket:edit", TicketTransition.SUBMIT.getPermission());
    }

    @Test
    @DisplayName("RESOLVE转换使用edit权限")
    void resolve_UsesEditPermission()
    {
        assertEquals("itsm:ticket:edit", TicketTransition.RESOLVE.getPermission());
    }
}
