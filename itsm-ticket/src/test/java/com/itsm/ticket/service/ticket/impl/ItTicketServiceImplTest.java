package com.itsm.ticket.service.ticket.impl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import com.itsm.common.core.redis.RedisCache;
import com.itsm.common.exception.ServiceException;
import com.itsm.common.utils.SecurityUtils;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.enums.TicketStatus;
import com.itsm.ticket.enums.TicketTransition;
import com.itsm.ticket.mapper.ticket.ItTicketMapper;
import com.itsm.ticket.service.ticket.IItTicketLogService;
import com.itsm.ticket.service.ticket.IItTicketProcessLogService;
import com.itsm.ticket.service.workflow.IWorkflowEngine;
import com.itsm.ticket.statemachine.TicketStateMachine;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItTicketServiceImplTest
{
    @Mock
    private ItTicketMapper ticketMapper;

    @Mock
    private RedisCache redisCache;

    @Mock
    private TicketStateMachine stateMachine;

    @Mock
    private IItTicketLogService logService;

    @Mock
    private IWorkflowEngine workflowEngine;

    @Mock
    private IItTicketProcessLogService processLogService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ItTicketServiceImpl ticketService;

    private ItTicket draftTicket;
    private ItTicket submittedTicket;
    private ItTicket approvedTicket;
    private ItTicket processingTicket;
    private ItTicket assignedTicket;
    private ItTicket rejectedTicket;
    private ItTicket cancelledTicket;
    private ItTicket closedTicket;

    @BeforeEach
    void setUp()
    {
        draftTicket = new ItTicket();
        draftTicket.setTicketId(1L);
        draftTicket.setTicketNo("TK-20260516-0001");
        draftTicket.setTitle("测试工单");
        draftTicket.setStatus(TicketStatus.DRAFT.getCode());
        draftTicket.setCategoryId(1L);
        draftTicket.setPriority(3);

        submittedTicket = new ItTicket();
        submittedTicket.setTicketId(2L);
        submittedTicket.setTicketNo("TK-20260516-0002");
        submittedTicket.setTitle("已提交工单");
        submittedTicket.setStatus(TicketStatus.SUBMITTED.getCode());

        approvedTicket = new ItTicket();
        approvedTicket.setTicketId(3L);
        approvedTicket.setTicketNo("TK-20260516-0003");
        approvedTicket.setTitle("已审批工单");
        approvedTicket.setStatus(TicketStatus.APPROVED.getCode());

        processingTicket = new ItTicket();
        processingTicket.setTicketId(4L);
        processingTicket.setTicketNo("TK-20260516-0004");
        processingTicket.setTitle("处理中工单");
        processingTicket.setStatus(TicketStatus.PROCESSING.getCode());
        processingTicket.setAssigneeId(10L);

        assignedTicket = new ItTicket();
        assignedTicket.setTicketId(8L);
        assignedTicket.setTicketNo("TK-20260516-0008");
        assignedTicket.setTitle("已分配工单");
        assignedTicket.setStatus(TicketStatus.ASSIGNED.getCode());
        assignedTicket.setAssigneeId(10L);

        rejectedTicket = new ItTicket();
        rejectedTicket.setTicketId(5L);
        rejectedTicket.setTicketNo("TK-20260516-0005");
        rejectedTicket.setTitle("已驳回工单");
        rejectedTicket.setStatus(TicketStatus.REJECTED.getCode());

        cancelledTicket = new ItTicket();
        cancelledTicket.setTicketId(6L);
        cancelledTicket.setTicketNo("TK-20260516-0006");
        cancelledTicket.setTitle("已取消工单");
        cancelledTicket.setStatus(TicketStatus.CANCELLED.getCode());

        closedTicket = new ItTicket();
        closedTicket.setTicketId(7L);
        closedTicket.setTicketNo("TK-20260516-0007");
        closedTicket.setTitle("已关闭工单");
        closedTicket.setStatus(TicketStatus.CLOSED.getCode());
    }

    @Test
    @DisplayName("根据ID查询工单")
    void selectTicketById_ReturnsTicket()
    {
        when(ticketMapper.selectTicketById(1L)).thenReturn(draftTicket);
        ItTicket result = ticketService.selectTicketById(1L);
        assertNotNull(result);
        assertEquals("TK-20260516-0001", result.getTicketNo());
        verify(ticketMapper).selectTicketById(1L);
    }

    @Test
    @DisplayName("查询工单列表")
    void selectTicketList_ReturnsList()
    {
        List<ItTicket> tickets = Arrays.asList(draftTicket, submittedTicket);
        when(ticketMapper.selectTicketList(any())).thenReturn(tickets);
        List<ItTicket> result = ticketService.selectTicketList(new ItTicket());
        assertEquals(2, result.size());
        verify(ticketMapper).selectTicketList(any());
    }

    @Test
    @DisplayName("编辑工单-草稿状态允许编辑")
    void updateTicket_DraftStatus_Allowed()
    {
        when(ticketMapper.selectTicketById(1L)).thenReturn(draftTicket);
        when(ticketMapper.updateTicket(any())).thenReturn(1);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            int rows = ticketService.updateTicket(draftTicket);
            assertEquals(1, rows);
        }
    }

    @Test
    @DisplayName("编辑工单-已提交状态不允许编辑")
    void updateTicket_SubmittedStatus_ThrowsException()
    {
        when(ticketMapper.selectTicketById(2L)).thenReturn(submittedTicket);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ServiceException exception = assertThrows(ServiceException.class, () ->
                ticketService.updateTicket(submittedTicket));
            assertTrue(exception.getMessage().contains("只有草稿或已驳回的工单才能编辑"));
        }
    }

    @Test
    @DisplayName("编辑工单-工单不存在抛出异常")
    void updateTicket_NotFound_ThrowsException()
    {
        when(ticketMapper.selectTicketById(999L)).thenReturn(null);
        ItTicket ticket = new ItTicket();
        ticket.setTicketId(999L);
        assertThrows(ServiceException.class, () -> ticketService.updateTicket(ticket));
    }

    @Test
    @DisplayName("BUG-004: 删除工单-草稿状态允许删除")
    void deleteTicketByIds_DraftStatus_Allowed()
    {
        when(ticketMapper.selectTicketById(1L)).thenReturn(draftTicket);
        when(ticketMapper.deleteTicketById(1L)).thenReturn(1);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ticketService.deleteTicketByIds(new Long[]{1L});
            verify(ticketMapper).deleteTicketById(1L);
            verify(logService).insertLog(any());
        }
    }

    @Test
    @DisplayName("BUG-004: 删除工单-已驳回状态允许删除")
    void deleteTicketByIds_RejectedStatus_Allowed()
    {
        when(ticketMapper.selectTicketById(5L)).thenReturn(rejectedTicket);
        when(ticketMapper.deleteTicketById(5L)).thenReturn(1);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ticketService.deleteTicketByIds(new Long[]{5L});
            verify(ticketMapper).deleteTicketById(5L);
        }
    }

    @Test
    @DisplayName("BUG-004: 删除工单-已取消状态允许删除")
    void deleteTicketByIds_CancelledStatus_Allowed()
    {
        when(ticketMapper.selectTicketById(6L)).thenReturn(cancelledTicket);
        when(ticketMapper.deleteTicketById(6L)).thenReturn(1);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ticketService.deleteTicketByIds(new Long[]{6L});
            verify(ticketMapper).deleteTicketById(6L);
        }
    }

    @Test
    @DisplayName("BUG-004: 删除工单-已提交状态不允许删除")
    void deleteTicketByIds_SubmittedStatus_ThrowsException()
    {
        when(ticketMapper.selectTicketById(2L)).thenReturn(submittedTicket);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ServiceException exception = assertThrows(ServiceException.class, () ->
                ticketService.deleteTicketByIds(new Long[]{2L}));
            assertTrue(exception.getMessage().contains("只有草稿、已驳回或已取消的工单才能删除"));
        }
    }

    @Test
    @DisplayName("BUG-004: 删除工单-处理中状态不允许删除")
    void deleteTicketByIds_ProcessingStatus_ThrowsException()
    {
        when(ticketMapper.selectTicketById(4L)).thenReturn(processingTicket);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ServiceException exception = assertThrows(ServiceException.class, () ->
                ticketService.deleteTicketByIds(new Long[]{4L}));
            assertTrue(exception.getMessage().contains("只有草稿、已驳回或已取消的工单才能删除"));
        }
    }

    @Test
    @DisplayName("BUG-004: 删除工单-已关闭状态不允许删除")
    void deleteTicketByIds_ClosedStatus_ThrowsException()
    {
        when(ticketMapper.selectTicketById(7L)).thenReturn(closedTicket);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ServiceException exception = assertThrows(ServiceException.class, () ->
                ticketService.deleteTicketByIds(new Long[]{7L}));
            assertTrue(exception.getMessage().contains("只有草稿、已驳回或已取消的工单才能删除"));
        }
    }

    @Test
    @DisplayName("BUG-004: 删除工单-不存在的工单跳过")
    void deleteTicketByIds_NotFound_Skipped()
    {
        when(ticketMapper.selectTicketById(999L)).thenReturn(null);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ticketService.deleteTicketByIds(new Long[]{999L});
            verify(ticketMapper, never()).deleteTicketById(999L);
        }
    }

    @Test
    @DisplayName("审批工单-合法转换")
    void approveTicket_ValidTransition_Success()
    {
        when(ticketMapper.selectTicketById(2L)).thenReturn(submittedTicket);
        when(ticketMapper.approveTicket(any())).thenReturn(1);
        TicketTransition transition = TicketTransition.APPROVE;
        when(stateMachine.validateTransition(TicketStatus.SUBMITTED, TicketStatus.APPROVED))
            .thenReturn(transition);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ItTicket approveParam = new ItTicket();
            approveParam.setTicketId(2L);
            approveParam.setStatus("APPROVED");
            approveParam.setRemark("同意");
            int rows = ticketService.approveTicket(approveParam);
            assertEquals(1, rows);
            verify(logService).insertLog(any());
        }
    }

    @Test
    @DisplayName("审批工单-工单不存在抛出异常")
    void approveTicket_NotFound_ThrowsException()
    {
        when(ticketMapper.selectTicketById(999L)).thenReturn(null);
        ItTicket ticket = new ItTicket();
        ticket.setTicketId(999L);
        ticket.setStatus("APPROVED");
        assertThrows(ServiceException.class, () -> ticketService.approveTicket(ticket));
    }

    @Test
    @DisplayName("分配工单-合法转换")
    void assignTicket_ValidTransition_Success()
    {
        when(ticketMapper.selectTicketById(3L)).thenReturn(approvedTicket);
        when(ticketMapper.assignTicket(any())).thenReturn(1);
        TicketTransition transition = TicketTransition.ASSIGN;
        when(stateMachine.validateTransition(TicketStatus.APPROVED, TicketStatus.ASSIGNED))
            .thenReturn(transition);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ItTicket assignParam = new ItTicket();
            assignParam.setTicketId(3L);
            assignParam.setAssigneeId(10L);
            assignParam.setRemark("分配给张三");
            int rows = ticketService.assignTicket(assignParam);
            assertEquals(1, rows);
            verify(logService).insertLog(any());
        }
    }

    @Test
    @DisplayName("获取可用转换状态")
    void getAvailableTransitions_ReturnsStatuses()
    {
        when(ticketMapper.selectTicketById(1L)).thenReturn(draftTicket);
        List<TicketStatus> targets = Arrays.asList(TicketStatus.SUBMITTED, TicketStatus.CANCELLED);
        when(stateMachine.getAvailableTargetStatuses(TicketStatus.DRAFT)).thenReturn(targets);
        List<TicketStatus> result = ticketService.getAvailableTransitions(1L);
        assertEquals(2, result.size());
        assertTrue(result.contains(TicketStatus.SUBMITTED));
    }

    @Test
    @DisplayName("生成工单编号格式正确")
    void generateTicketNo_FormatCorrect()
    {
        when(redisCache.increment(anyString(), eq(1L), anyLong(), any())).thenReturn(1L);
        String ticketNo = ticketService.generateTicketNo();
        assertTrue(ticketNo.startsWith("TK-"));
        assertTrue(ticketNo.matches("TK-\\d{8}-\\d{4}"));
    }

    @Test
    @DisplayName("BUG-009: 首次生成编号时设置TTL")
    void generateTicketNo_FirstSeq_SetsExpire()
    {
        when(redisCache.increment(anyString(), eq(1L), anyLong(), any())).thenReturn(1L);
        ticketService.generateTicketNo();
        verify(redisCache).increment(anyString(), eq(1L), eq(25L), any());
    }

    @Test
    @DisplayName("每次生成编号时都刷新TTL")
    void generateTicketNo_SubsequentSeq_RefreshesExpire()
    {
        when(redisCache.increment(anyString(), eq(1L), anyLong(), any())).thenReturn(2L);
        ticketService.generateTicketNo();
        verify(redisCache).increment(anyString(), eq(1L), eq(25L), any());
    }

    @Test
    @DisplayName("BUG-009: 序列号超过9999时抛出异常")
    void generateTicketNo_SeqOverflow_ThrowsException()
    {
        when(redisCache.increment(anyString(), eq(1L), anyLong(), any())).thenReturn(10000L);
        assertThrows(ServiceException.class, () -> ticketService.generateTicketNo());
    }

    @Test
    @DisplayName("BUG-005: transitTicket使用updateTicketStatus而非updateTicket")
    void transitTicket_UsesUpdateTicketStatus()
    {
        ItTicket resolvedTicket = new ItTicket();
        resolvedTicket.setTicketId(8L);
        resolvedTicket.setTicketNo("TK-20260516-0008");
        resolvedTicket.setStatus(TicketStatus.RESOLVED.getCode());

        when(ticketMapper.selectTicketById(8L)).thenReturn(resolvedTicket);
        when(ticketMapper.updateTicketStatus(any())).thenReturn(1);
        TicketTransition transition = TicketTransition.VERIFY;
        when(stateMachine.validateTransition(TicketStatus.RESOLVED, TicketStatus.VERIFIED))
            .thenReturn(transition);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ItTicket ticket = new ItTicket();
            ticket.setTicketId(8L);
            int rows = ticketService.transitTicket(ticket, TicketStatus.VERIFIED);
            assertEquals(1, rows);
            verify(ticketMapper).updateTicketStatus(any());
            verify(ticketMapper, never()).updateTicket(any());
        }
    }

    @Test
    @DisplayName("转派工单-从ASSIGNED状态转派成功")
    void reassignTicket_FromAssigned_Success()
    {
        when(ticketMapper.selectTicketById(8L)).thenReturn(assignedTicket);
        when(ticketMapper.assignTicket(any())).thenReturn(1);
        TicketTransition transition = TicketTransition.REASSIGN_ASSIGNED;
        when(stateMachine.validateTransition(TicketStatus.ASSIGNED, TicketStatus.ASSIGNED))
            .thenReturn(transition);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ItTicket reassignParam = new ItTicket();
            reassignParam.setTicketId(8L);
            reassignParam.setAssigneeId(20L);
            reassignParam.setRemark("转派给李四");
            int rows = ticketService.reassignTicket(reassignParam);
            assertEquals(1, rows);
            verify(ticketMapper).assignTicket(any());
            verify(logService).insertLog(any());
        }
    }

    @Test
    @DisplayName("转派工单-从PROCESSING状态转派成功")
    void reassignTicket_FromProcessing_Success()
    {
        when(ticketMapper.selectTicketById(4L)).thenReturn(processingTicket);
        when(ticketMapper.updateTicketStatus(any())).thenReturn(1);
        TicketTransition transition = TicketTransition.REASSIGN_PROCESSING;
        when(stateMachine.validateTransition(TicketStatus.PROCESSING, TicketStatus.ASSIGNED))
            .thenReturn(transition);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ItTicket reassignParam = new ItTicket();
            reassignParam.setTicketId(4L);
            reassignParam.setAssigneeId(20L);
            reassignParam.setRemark("转派给李四");
            int rows = ticketService.reassignTicket(reassignParam);
            assertEquals(1, rows);
            verify(ticketMapper).updateTicketStatus(any());
            verify(logService).insertLog(any());
        }
    }

    @Test
    @DisplayName("转派工单-无效状态抛出异常")
    void reassignTicket_InvalidStatus_ThrowsException()
    {
        when(ticketMapper.selectTicketById(1L)).thenReturn(draftTicket);
        try (var mocked = mockStatic(SecurityUtils.class))
        {
            mocked.when(SecurityUtils::getUsername).thenReturn("admin");
            ItTicket reassignParam = new ItTicket();
            reassignParam.setTicketId(1L);
            reassignParam.setAssigneeId(20L);
            ServiceException exception = assertThrows(ServiceException.class,
                () -> ticketService.reassignTicket(reassignParam));
            assertTrue(exception.getMessage().contains("只有已分配或处理中的工单才能转派"));
        }
    }
}
