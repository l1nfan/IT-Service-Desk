package com.itsm.ticket.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import com.itsm.common.config.ItsmConfig;
import com.itsm.common.core.domain.AjaxResult;
import com.itsm.common.core.domain.model.LoginUser;
import com.itsm.common.utils.SecurityUtils;
import com.itsm.common.utils.file.FileUploadUtils;
import com.itsm.framework.config.ServerConfig;
import com.itsm.ticket.config.ItsmModuleConfig;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.domain.ticket.ItTicketAttachment;
import com.itsm.ticket.enums.TicketStatus;
import com.itsm.ticket.service.ticket.IItTicketAttachmentService;
import com.itsm.ticket.service.ticket.IItTicketService;
import com.itsm.web.controller.itsm.ItTicketAttachmentController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItTicketAttachmentControllerTest
{
    @Mock
    private IItTicketAttachmentService attachmentService;

    @Mock
    private IItTicketService ticketService;

    @Mock
    private ServerConfig serverConfig;

    @Mock
    private ItsmModuleConfig itsmModuleConfig;

    @InjectMocks
    private ItTicketAttachmentController controller;

    private ItTicket testTicket;
    private ItTicketAttachment testAttachment;
    private MultipartFile testFile;

    @BeforeEach
    void setUp()
    {
        testTicket = new ItTicket();
        testTicket.setTicketId(1L);
        testTicket.setTicketNo("TK-20260517-0001");
        testTicket.setTitle("测试工单");
        testTicket.setStatus(TicketStatus.PROCESSING.getCode());

        testAttachment = new ItTicketAttachment();
        testAttachment.setAttachmentId(1L);
        testAttachment.setTicketId(1L);
        testAttachment.setFileName("测试文档.pdf");
        testAttachment.setFilePath("/profile/upload/2026/05/17/test.pdf");
        testAttachment.setFileSize(1024L);
        testAttachment.setFileType("pdf");

        testFile = mock(MultipartFile.class);
        lenient().when(testFile.isEmpty()).thenReturn(false);
        lenient().when(testFile.getSize()).thenReturn(1024L);
        lenient().when(testFile.getOriginalFilename()).thenReturn("测试文档.pdf");
    }

    @Test
    @DisplayName("list返回附件列表")
    void list_ReturnsAttachments()
    {
        List<ItTicketAttachment> attachments = Collections.singletonList(testAttachment);
        when(attachmentService.selectAttachmentsByTicketId(1L)).thenReturn(attachments);

        AjaxResult result = controller.list(1L);

        assertTrue(result.isSuccess());
        assertEquals(attachments, result.get(AjaxResult.DATA_TAG));
        verify(attachmentService).selectAttachmentsByTicketId(1L);
    }

    @Test
    @DisplayName("upload失败-工单ID为空")
    void upload_Fails_WhenTicketIdNull()
    {
        AjaxResult result = controller.upload(null, testFile);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("工单ID不能为空"));
        verify(ticketService, never()).selectTicketById(anyLong());
    }

    @Test
    @DisplayName("upload失败-文件为空")
    void upload_Fails_WhenFileEmpty()
    {
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        AjaxResult result = controller.upload(1L, emptyFile);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("上传文件不能为空"));
        verify(ticketService, never()).selectTicketById(anyLong());
    }

    @Test
    @DisplayName("upload失败-文件为null")
    void upload_Fails_WhenFileNull()
    {
        AjaxResult result = controller.upload(1L, null);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("上传文件不能为空"));
        verify(ticketService, never()).selectTicketById(anyLong());
    }

    @Test
    @DisplayName("upload失败-工单不存在")
    void upload_Fails_WhenTicketNotExists()
    {
        when(ticketService.selectTicketById(999L)).thenReturn(null);

        AjaxResult result = controller.upload(999L, testFile);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("工单不存在"));
        verify(ticketService).selectTicketById(999L);
    }

    @Test
    @DisplayName("upload失败-工单已关闭")
    void upload_Fails_WhenTicketClosed()
    {
        ItTicket closedTicket = new ItTicket();
        closedTicket.setTicketId(2L);
        closedTicket.setStatus(TicketStatus.CLOSED.getCode());
        when(ticketService.selectTicketById(2L)).thenReturn(closedTicket);

        AjaxResult result = controller.upload(2L, testFile);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("已关闭或已取消"));
    }

    @Test
    @DisplayName("upload失败-工单已取消")
    void upload_Fails_WhenTicketCancelled()
    {
        ItTicket cancelledTicket = new ItTicket();
        cancelledTicket.setTicketId(3L);
        cancelledTicket.setStatus(TicketStatus.CANCELLED.getCode());
        when(ticketService.selectTicketById(3L)).thenReturn(cancelledTicket);

        AjaxResult result = controller.upload(3L, testFile);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("已关闭或已取消"));
    }

    @Test
    @DisplayName("upload失败-文件大小超限")
    void upload_Fails_WhenFileSizeExceedsLimit()
    {
        when(ticketService.selectTicketById(1L)).thenReturn(testTicket);
        when(itsmModuleConfig.getMaxAttachmentSizeMb()).thenReturn(50L);

        MultipartFile largeFile = mock(MultipartFile.class);
        when(largeFile.isEmpty()).thenReturn(false);
        when(largeFile.getSize()).thenReturn(51L * 1024 * 1024);

        AjaxResult result = controller.upload(1L, largeFile);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("50"));
        verify(itsmModuleConfig, atLeastOnce()).getMaxAttachmentSizeMb();
    }

    @Test
    @DisplayName("upload失败-附件数量超限")
    void upload_Fails_WhenAttachmentCountExceedsLimit()
    {
        when(ticketService.selectTicketById(1L)).thenReturn(testTicket);
        when(itsmModuleConfig.getMaxAttachmentSizeMb()).thenReturn(50L);
        when(itsmModuleConfig.getMaxAttachmentsPerTicket()).thenReturn(20);

        List<ItTicketAttachment> existingAttachments = new ArrayList<>();
        for (int i = 0; i < 20; i++)
        {
            existingAttachments.add(new ItTicketAttachment());
        }
        when(attachmentService.selectAttachmentsByTicketId(1L)).thenReturn(existingAttachments);

        AjaxResult result = controller.upload(1L, testFile);

        assertTrue(result.isError());
        assertTrue(result.get(AjaxResult.MSG_TAG).toString().contains("20"));
        verify(itsmModuleConfig, atLeastOnce()).getMaxAttachmentsPerTicket();
    }

    @Test
    @DisplayName("upload成功上传附件")
    void upload_Success()
    {
        when(ticketService.selectTicketById(1L)).thenReturn(testTicket);
        when(itsmModuleConfig.getMaxAttachmentSizeMb()).thenReturn(50L);
        when(itsmModuleConfig.getMaxAttachmentsPerTicket()).thenReturn(20);
        when(attachmentService.selectAttachmentsByTicketId(1L)).thenReturn(Collections.emptyList());
        when(attachmentService.insertAttachment(any(ItTicketAttachment.class))).thenReturn(1);
        when(serverConfig.getUrl()).thenReturn("http://localhost:8080");

        try (MockedStatic<ItsmConfig> itsmConfigMock = mockStatic(ItsmConfig.class);
             MockedStatic<FileUploadUtils> fileUploadUtilsMock = mockStatic(FileUploadUtils.class);
             MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class))
        {
            itsmConfigMock.when(ItsmConfig::getUploadPath).thenReturn("/upload/path");
            fileUploadUtilsMock.when(() -> FileUploadUtils.upload(anyString(), any(MultipartFile.class)))
                    .thenReturn("/profile/upload/2026/05/17/test.pdf");

            LoginUser loginUser = mock(LoginUser.class);
            when(loginUser.getUsername()).thenReturn("admin");
            securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);

            AjaxResult result = controller.upload(1L, testFile);

            assertTrue(result.isSuccess());
            assertNotNull(result.get("url"));
            assertNotNull(result.get("fileName"));
            verify(attachmentService).insertAttachment(any(ItTicketAttachment.class));
        }
    }

    @Test
    @DisplayName("remove删除附件")
    void remove_DeletesAttachment()
    {
        when(attachmentService.deleteAttachmentById(1L)).thenReturn(1);

        AjaxResult result = controller.remove(1L);

        assertTrue(result.isSuccess());
        verify(attachmentService).deleteAttachmentById(1L);
    }
}
