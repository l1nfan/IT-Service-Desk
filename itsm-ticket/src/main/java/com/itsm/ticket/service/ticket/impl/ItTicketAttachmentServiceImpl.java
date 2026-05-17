package com.itsm.ticket.service.ticket.impl;

import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsm.common.config.ItsmConfig;
import com.itsm.common.utils.file.FileUtils;
import com.itsm.ticket.domain.ticket.ItTicketAttachment;
import com.itsm.ticket.mapper.ticket.ItTicketAttachmentMapper;
import com.itsm.ticket.service.ticket.IItTicketAttachmentService;

@Service
public class ItTicketAttachmentServiceImpl implements IItTicketAttachmentService
{
    private static final Logger log = LoggerFactory.getLogger(ItTicketAttachmentServiceImpl.class);

    @Autowired
    private ItTicketAttachmentMapper attachmentMapper;

    @Override
    public ItTicketAttachment selectAttachmentById(Long attachmentId)
    {
        return attachmentMapper.selectAttachmentById(attachmentId);
    }

    @Override
    public List<ItTicketAttachment> selectAttachmentsByTicketId(Long ticketId)
    {
        return attachmentMapper.selectAttachmentsByTicketId(ticketId);
    }

    @Override
    public int insertAttachment(ItTicketAttachment attachment)
    {
        return attachmentMapper.insertAttachment(attachment);
    }

    @Override
    public int deleteAttachmentById(Long attachmentId)
    {
        ItTicketAttachment attachment = attachmentMapper.selectAttachmentById(attachmentId);
        if (attachment != null)
        {
            deletePhysicalFile(attachment.getFilePath());
        }
        return attachmentMapper.deleteAttachmentById(attachmentId);
    }

    private void deletePhysicalFile(String filePath)
    {
        try
        {
            String localPath = ItsmConfig.getProfile();
            String fullPath = localPath + filePath;
            File file = new File(fullPath);
            if (file.exists())
            {
                file.delete();
                log.info("已删除物理文件: {}", fullPath);
            }
        }
        catch (Exception e)
        {
            log.warn("删除物理文件失败: {}", filePath, e);
        }
    }
}
