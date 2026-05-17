package com.itsm.ticket.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TicketStatusTest
{
    @Test
    @DisplayName("fromCode-有效编码返回对应枚举")
    void fromCode_ValidCode_ReturnsEnum()
    {
        assertEquals(TicketStatus.DRAFT, TicketStatus.fromCode("DRAFT"));
        assertEquals(TicketStatus.SUBMITTED, TicketStatus.fromCode("SUBMITTED"));
        assertEquals(TicketStatus.APPROVED, TicketStatus.fromCode("APPROVED"));
        assertEquals(TicketStatus.ASSIGNED, TicketStatus.fromCode("ASSIGNED"));
        assertEquals(TicketStatus.PROCESSING, TicketStatus.fromCode("PROCESSING"));
        assertEquals(TicketStatus.RESOLVED, TicketStatus.fromCode("RESOLVED"));
        assertEquals(TicketStatus.VERIFIED, TicketStatus.fromCode("VERIFIED"));
        assertEquals(TicketStatus.CLOSED, TicketStatus.fromCode("CLOSED"));
        assertEquals(TicketStatus.REJECTED, TicketStatus.fromCode("REJECTED"));
        assertEquals(TicketStatus.CANCELLED, TicketStatus.fromCode("CANCELLED"));
    }

    @Test
    @DisplayName("fromCode-无效编码返回null")
    void fromCode_InvalidCode_ReturnsNull()
    {
        assertNull(TicketStatus.fromCode("INVALID"));
        assertNull(TicketStatus.fromCode(null));
        assertNull(TicketStatus.fromCode(""));
    }

    @Test
    @DisplayName("getCode返回正确的编码")
    void getCode_ReturnsCorrectCode()
    {
        assertEquals("DRAFT", TicketStatus.DRAFT.getCode());
        assertEquals("SUBMITTED", TicketStatus.SUBMITTED.getCode());
        assertEquals("CLOSED", TicketStatus.CLOSED.getCode());
    }

    @Test
    @DisplayName("getDesc返回正确的描述")
    void getDesc_ReturnsCorrectDesc()
    {
        assertEquals("草稿", TicketStatus.DRAFT.getDesc());
        assertEquals("已提交", TicketStatus.SUBMITTED.getDesc());
        assertEquals("已关闭", TicketStatus.CLOSED.getDesc());
    }

    @Test
    @DisplayName("枚举值总数应为10")
    void values_CountIsTen()
    {
        assertEquals(10, TicketStatus.values().length);
    }
}
