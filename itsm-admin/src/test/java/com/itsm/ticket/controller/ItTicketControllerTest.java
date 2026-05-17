package com.itsm.ticket.controller;

import java.util.Arrays;
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
import com.itsm.common.constant.HttpStatus;
import com.itsm.common.core.domain.AjaxResult;
import com.itsm.common.core.page.TableDataInfo;
import com.itsm.common.utils.PageUtils;
import com.itsm.ticket.domain.ticket.ItTicket;
import com.itsm.ticket.domain.ticket.ItTicketLog;
import com.itsm.ticket.domain.ticket.TicketTransitDTO;
import com.itsm.ticket.enums.TicketStatus;
import com.itsm.ticket.service.ticket.IItTicketLogService;
import com.itsm.ticket.service.ticket.IItTicketService;
import com.itsm.ticket.service.ticket.IItSlaConfigService;
import com.itsm.web.controller.itsm.ItTicketController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItTicketControllerTest
{
    @Mock
    private IItTicketService ticketService;

    @Mock
    private IItTicketLogService ticketLogService;

    @Mock
    private IItSlaConfigService slaConfigService;

    @InjectMocks
    private ItTicketController controller;

    private ItTicket testTicket;

    @BeforeEach
    void setUp()
    {
        testTicket = new ItTicket();
        testTicket.setTicketId(1L);
        testTicket.setTicketNo("TK-20260517-0001");
        testTicket.setTitle("测试工单");
        testTicket.setStatus(TicketStatus.DRAFT.getCode());
        testTicket.setCategoryId(1L);
        testTicket.setPriority(3);
    }

    @Test
    @DisplayName("list返回TableDataInfo")
    void list_ReturnsTableDataInfo()
    {
        List<ItTicket> ticketList = Collections.singletonList(testTicket);
        try (MockedStatic<PageUtils> pageUtilsMock = mockStatic(PageUtils.class))
        {
            pageUtilsMock.when(PageUtils::startPage).thenReturn(null);
            pageUtilsMock.when(PageUtils::getPage).thenReturn(null);
            pageUtilsMock.when(PageUtils::clearPage).thenAnswer(invocation -> null);

            when(ticketService.selectTicketList(any(ItTicket.class))).thenReturn(ticketList);

            TableDataInfo result = controller.list(testTicket);

            assertNotNull(result);
            assertEquals(HttpStatus.SUCCESS, result.getCode());
            assertEquals(ticketList, result.getRows());
            assertEquals(1, result.getTotal());
            verify(ticketService).selectTicketList(any(ItTicket.class));
        }
    }

    @Test
    @DisplayName("getInfo返回工单数据")
    void getInfo_ReturnsTicketData()
    {
        when(ticketService.selectTicketById(1L)).thenReturn(testTicket);

        AjaxResult result = controller.getInfo(1L);

        assertTrue(result.isSuccess());
        assertEquals(testTicket, result.get(AjaxResult.DATA_TAG));
        verify(ticketService).selectTicketById(1L);
    }

    @Test
    @DisplayName("add创建工单")
    void add_CreatesTicket()
    {
        when(ticketService.insertTicket(any(ItTicket.class))).thenReturn(1);

        AjaxResult result = controller.add(testTicket);

        assertTrue(result.isSuccess());
        verify(ticketService).insertTicket(any(ItTicket.class));
    }

    @Test
    @DisplayName("edit更新工单")
    void edit_UpdatesTicket()
    {
        when(ticketService.updateTicket(any(ItTicket.class))).thenReturn(1);

        AjaxResult result = controller.edit(testTicket);

        assertTrue(result.isSuccess());
        verify(ticketService).updateTicket(any(ItTicket.class));
    }

    @Test
    @DisplayName("remove删除工单并调用deleteTicketByIds返回toAjax1")
    void remove_DeletesTickets()
    {
        Long[] ticketIds = new Long[]{1L, 2L};

        AjaxResult result = controller.remove(ticketIds);

        verify(ticketService).deleteTicketByIds(ticketIds);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("assign分配工单")
    void assign_AssignsTicket()
    {
        when(ticketService.assignTicket(any(ItTicket.class))).thenReturn(1);

        AjaxResult result = controller.assign(testTicket);

        assertTrue(result.isSuccess());
        verify(ticketService).assignTicket(any(ItTicket.class));
    }

    @Test
    @DisplayName("approve审批工单")
    void approve_ApprovesTicket()
    {
        when(ticketService.approveTicket(any(ItTicket.class))).thenReturn(1);

        AjaxResult result = controller.approve(testTicket);

        assertTrue(result.isSuccess());
        verify(ticketService).approveTicket(any(ItTicket.class));
    }

    @Test
    @DisplayName("transit使用TicketTransitDTO流转工单")
    void transit_UsesTicketTransitDTO()
    {
        TicketTransitDTO dto = new TicketTransitDTO();
        dto.setTicketId(1L);
        dto.setTargetStatus(TicketStatus.SUBMITTED.getCode());
        dto.setRemark("提交工单");

        when(ticketService.transitTicket(any(TicketTransitDTO.class))).thenReturn(1);

        AjaxResult result = controller.transit(dto);

        assertTrue(result.isSuccess());
        verify(ticketService).transitTicket(any(TicketTransitDTO.class));
    }

    @Test
    @DisplayName("getAvailableTransitions返回可用流转")
    void getAvailableTransitions_ReturnsTransitions()
    {
        List<TicketStatus> transitions = Arrays.asList(TicketStatus.SUBMITTED, TicketStatus.CANCELLED);
        when(ticketService.getAvailableTransitions(1L)).thenReturn(transitions);

        AjaxResult result = controller.getAvailableTransitions(1L);

        assertTrue(result.isSuccess());
        assertEquals(transitions, result.get(AjaxResult.DATA_TAG));
        verify(ticketService).getAvailableTransitions(1L);
    }

    @Test
    @DisplayName("getTicketLogs返回工单日志")
    void getTicketLogs_ReturnsLogs()
    {
        ItTicketLog log = new ItTicketLog();
        log.setLogId(1L);
        log.setTicketId(1L);
        log.setAction("CREATE");
        List<ItTicketLog> logs = Collections.singletonList(log);

        when(ticketLogService.selectLogsByTicketId(1L)).thenReturn(logs);

        AjaxResult result = controller.getTicketLogs(1L);

        assertTrue(result.isSuccess());
        assertEquals(logs, result.get(AjaxResult.DATA_TAG));
        verify(ticketLogService).selectLogsByTicketId(1L);
    }
}
