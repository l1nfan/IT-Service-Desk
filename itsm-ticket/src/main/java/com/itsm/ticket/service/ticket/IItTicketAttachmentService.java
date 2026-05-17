package com.itsm.ticket.service.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicketAttachment;

/**
 * 工单附件 服务层
 *
 * @author itsm
 */
public interface IItTicketAttachmentService
{
    ItTicketAttachment selectAttachmentById(Long attachmentId);

    List<ItTicketAttachment> selectAttachmentsByTicketId(Long ticketId);

    int insertAttachment(ItTicketAttachment attachment);

    int deleteAttachmentById(Long attachmentId);
}
