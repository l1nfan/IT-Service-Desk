package com.itsm.ticket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "itsm.module")
public class ItsmModuleConfig
{
    private boolean enabled = true;
    private int maxAttachmentsPerTicket = 5;
    private long maxAttachmentSizeMb = 50;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public int getMaxAttachmentsPerTicket()
    {
        return maxAttachmentsPerTicket;
    }

    public void setMaxAttachmentsPerTicket(int maxAttachmentsPerTicket)
    {
        this.maxAttachmentsPerTicket = maxAttachmentsPerTicket;
    }

    public long getMaxAttachmentSizeMb()
    {
        return maxAttachmentSizeMb;
    }

    public void setMaxAttachmentSizeMb(long maxAttachmentSizeMb)
    {
        this.maxAttachmentSizeMb = maxAttachmentSizeMb;
    }
}
