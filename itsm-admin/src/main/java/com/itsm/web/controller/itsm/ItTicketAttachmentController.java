package com.itsm.web.controller.itsm;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.itsm.common.annotation.Log;
import com.itsm.common.config.ItsmConfig;
import com.itsm.common.core.controller.BaseController;
import com.itsm.common.core.domain.AjaxResult;
import com.itsm.common.enums.BusinessType;
import com.itsm.common.utils.file.FileUploadUtils;
import com.itsm.common.utils.file.FileUtils;
import com.itsm.framework.config.ServerConfig;
import com.itsm.ticket.config.ItsmModuleConfig;
import com.itsm.ticket.domain.ticket.ItTicketAttachment;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.service.ticket.IItTicketAttachmentService;
import com.itsm.ticket.service.ticket.IItTicketService;

@RestController
@RequestMapping("/itsm/ticket/attachment")
public class ItTicketAttachmentController extends BaseController
{
    @Autowired
    private IItTicketAttachmentService attachmentService;

    @Autowired
    private IItTicketService ticketService;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private ItsmModuleConfig itsmModuleConfig;

    @PreAuthorize("@ss.hasPermi('itsm:ticket:query')")
    @GetMapping("/list/{ticketId}")
    public AjaxResult list(@PathVariable Long ticketId)
    {
        List<ItTicketAttachment> list = attachmentService.selectAttachmentsByTicketId(ticketId);
        return success(list);
    }

    @Log(title = "工单附件", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:edit')")
    @PostMapping("/upload")
    public AjaxResult upload(Long ticketId, MultipartFile file)
    {
        if (ticketId == null)
        {
            return error("工单ID不能为空");
        }
        if (file == null || file.isEmpty())
        {
            return error("上传文件不能为空");
        }
        ItTicket ticket = ticketService.selectTicketById(ticketId);
        if (ticket == null)
        {
            return error("工单不存在");
        }
        if ("CLOSED".equals(ticket.getStatus()) || "CANCELLED".equals(ticket.getStatus()))
        {
            return error("已关闭或已取消的工单不能上传附件");
        }
        long maxFileSize = itsmModuleConfig.getMaxAttachmentSizeMb() * 1024 * 1024;
        if (file.getSize() > maxFileSize)
        {
            return error("上传文件大小不能超过" + itsmModuleConfig.getMaxAttachmentSizeMb() + "MB");
        }
        List<ItTicketAttachment> existingAttachments = attachmentService.selectAttachmentsByTicketId(ticketId);
        if (existingAttachments.size() >= itsmModuleConfig.getMaxAttachmentsPerTicket())
        {
            return error("每个工单最多上传" + itsmModuleConfig.getMaxAttachmentsPerTicket() + "个附件");
        }
        try
        {
            String filePath = ItsmConfig.getUploadPath();
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;

            ItTicketAttachment attachment = new ItTicketAttachment();
            attachment.setTicketId(ticketId);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(fileName);
            attachment.setFileSize(file.getSize());
            String originalName = file.getOriginalFilename();
            attachment.setFileType(originalName != null && originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".") + 1) : "");
            attachment.setUploadBy(getUsername());
            attachmentService.insertAttachment(attachment);

            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("attachmentId", attachment.getAttachmentId());
            return ajax;
        }
        catch (Exception e)
        {
            return error(e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('itsm:ticket:query')")
    @GetMapping("/download/{attachmentId}")
    public void download(@PathVariable Long attachmentId, HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            ItTicketAttachment attachment = attachmentService.selectAttachmentById(attachmentId);
            if (attachment == null)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String resource = attachment.getFilePath();
            if (!FileUtils.checkAllowDownload(resource))
            {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            String localPath = ItsmConfig.getProfile();
            String downloadPath = localPath + FileUtils.stripPrefix(resource);
            String downloadName = attachment.getFileName();
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        }
        catch (Exception e)
        {
            logger.error("下载附件失败", e);
            try
            {
                response.reset();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            catch (Exception ignored)
            {
            }
        }
    }

    @Log(title = "工单附件", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('itsm:ticket:edit')")
    @DeleteMapping("/{attachmentId}")
    public AjaxResult remove(@PathVariable Long attachmentId)
    {
        return toAjax(attachmentService.deleteAttachmentById(attachmentId));
    }
}
