package com.itsm.ticket.service.ticket.impl;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.itsm.ticket.domain.ticket.ItTicketAttachment;
import com.itsm.ticket.mapper.ticket.ItTicketAttachmentMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItTicketAttachmentServiceImplTest
{
    @Mock
    private ItTicketAttachmentMapper attachmentMapper;

    @InjectMocks
    private ItTicketAttachmentServiceImpl attachmentService;

    private ItTicketAttachment attachment1;
    private ItTicketAttachment attachment2;

    @BeforeEach
    void setUp()
    {
        attachment1 = new ItTicketAttachment();
        attachment1.setAttachmentId(1L);
        attachment1.setTicketId(100L);
        attachment1.setFileName("测试文档.pdf");
        attachment1.setFilePath("/profile/upload/2026/05/16/test.pdf");
        attachment1.setFileSize(1024L);
        attachment1.setFileType("pdf");
        attachment1.setUploadBy("admin");

        attachment2 = new ItTicketAttachment();
        attachment2.setAttachmentId(2L);
        attachment2.setTicketId(100L);
        attachment2.setFileName("测试表格.xlsx");
        attachment2.setFilePath("/profile/upload/2026/05/16/test.xlsx");
        attachment2.setFileSize(2048L);
        attachment2.setFileType("xlsx");
        attachment2.setUploadBy("admin");
    }

    @Test
    @DisplayName("根据ID查询附件")
    void selectAttachmentById_ReturnsAttachment()
    {
        when(attachmentMapper.selectAttachmentById(1L)).thenReturn(attachment1);
        ItTicketAttachment result = attachmentService.selectAttachmentById(1L);
        assertNotNull(result);
        assertEquals("测试文档.pdf", result.getFileName());
        assertEquals("pdf", result.getFileType());
        verify(attachmentMapper).selectAttachmentById(1L);
    }

    @Test
    @DisplayName("根据工单ID查询附件列表")
    void selectAttachmentsByTicketId_ReturnsList()
    {
        List<ItTicketAttachment> attachments = Arrays.asList(attachment1, attachment2);
        when(attachmentMapper.selectAttachmentsByTicketId(100L)).thenReturn(attachments);
        List<ItTicketAttachment> result = attachmentService.selectAttachmentsByTicketId(100L);
        assertEquals(2, result.size());
        assertEquals("测试文档.pdf", result.get(0).getFileName());
        assertEquals("测试表格.xlsx", result.get(1).getFileName());
        verify(attachmentMapper).selectAttachmentsByTicketId(100L);
    }

    @Test
    @DisplayName("新增附件")
    void insertAttachment_Success()
    {
        when(attachmentMapper.insertAttachment(any())).thenReturn(1);
        int rows = attachmentService.insertAttachment(attachment1);
        assertEquals(1, rows);
        verify(attachmentMapper).insertAttachment(attachment1);
    }

    @Test
    @DisplayName("删除附件-逻辑删除")
    void deleteAttachmentById_Success()
    {
        when(attachmentMapper.deleteAttachmentById(1L)).thenReturn(1);
        int rows = attachmentService.deleteAttachmentById(1L);
        assertEquals(1, rows);
        verify(attachmentMapper).deleteAttachmentById(1L);
    }

    @Test
    @DisplayName("根据工单ID查询附件-无附件返回空列表")
    void selectAttachmentsByTicketId_NoAttachments_ReturnsEmptyList()
    {
        when(attachmentMapper.selectAttachmentsByTicketId(999L)).thenReturn(Arrays.asList());
        List<ItTicketAttachment> result = attachmentService.selectAttachmentsByTicketId(999L);
        assertTrue(result.isEmpty());
    }
}
