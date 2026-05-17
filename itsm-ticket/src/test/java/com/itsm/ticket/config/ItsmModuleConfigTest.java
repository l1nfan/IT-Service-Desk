package com.itsm.ticket.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItsmModuleConfigTest
{
    @Test
    @DisplayName("BUG-013: ItsmModuleConfig默认值正确")
    void defaultValues_Correct()
    {
        ItsmModuleConfig config = new ItsmModuleConfig();
        assertTrue(config.isEnabled());
        assertEquals(5, config.getMaxAttachmentsPerTicket());
        assertEquals(50, config.getMaxAttachmentSizeMb());
    }

    @Test
    @DisplayName("BUG-013: ItsmModuleConfig setter和getter正常工作")
    void setterAndGetter_WorkCorrectly()
    {
        ItsmModuleConfig config = new ItsmModuleConfig();
        config.setEnabled(false);
        config.setMaxAttachmentsPerTicket(10);
        config.setMaxAttachmentSizeMb(100);
        assertFalse(config.isEnabled());
        assertEquals(10, config.getMaxAttachmentsPerTicket());
        assertEquals(100, config.getMaxAttachmentSizeMb());
    }
}
