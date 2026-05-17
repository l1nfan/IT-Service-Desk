package com.itsm.ticket.mapper.ticket;

import java.util.List;
import com.itsm.ticket.domain.ticket.ItTicketAttachment;

/**
 * 工单附件 数据层
 *
 * @author itsm
 */
public interface ItTicketAttachmentMapper
{
    ItTicketAttachment selectAttachmentById(Long attachmentId);

    List<ItTicketAttachment> selectAttachmentList(ItTicketAttachment attachment);

    List<ItTicketAttachment> selectAttachmentsByTicketId(Long ticketId);

    int insertAttachment(ItTicketAttachment attachment);

    int deleteAttachmentById(Long attachmentId);

    int deleteAttachmentByIds(Long[] attachmentIds);

    int deleteAttachmentByTicketId(Long ticketId);
}
