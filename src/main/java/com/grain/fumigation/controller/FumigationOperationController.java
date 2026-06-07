package com.grain.fumigation.controller;

import com.grain.fumigation.dto.*;
import com.grain.fumigation.entity.FumigationOperation;
import com.grain.fumigation.enums.OperationStatus;
import com.grain.fumigation.service.FumigationOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/fumigation")
public class FumigationOperationController {

    @Autowired
    private FumigationOperationService operationService;

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @PostMapping("/ticket")
    public ApiResponse<FumigationOperation> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            HttpServletRequest httpRequest) {
        FumigationOperation operation = operationService.createTicket(request, getClientIp(httpRequest));
        return ApiResponse.success("作业票创建成功", operation);
    }

    @PostMapping("/ticket/submit")
    public ApiResponse<FumigationOperation> submitTicket(
            @Valid @RequestBody SubmitTicketRequest request,
            HttpServletRequest httpRequest) {
        FumigationOperation operation = operationService.submitTicket(request, getClientIp(httpRequest));
        return ApiResponse.success("作业票提交成功", operation);
    }

    @PostMapping("/ticket/approve")
    public ApiResponse<FumigationOperation> approveTicket(
            @Valid @RequestBody ApproveTicketRequest request,
            HttpServletRequest httpRequest) {
        FumigationOperation operation = operationService.approveTicket(request, getClientIp(httpRequest));
        String msg = request.getApproved() ? "作业票审批通过" : "作业票审批驳回";
        return ApiResponse.success(msg, operation);
    }

    @PostMapping("/evacuation/confirm")
    public ApiResponse<FumigationOperation> confirmEvacuation(
            @Valid @RequestBody ConfirmEvacuationRequest request,
            HttpServletRequest httpRequest) {
        FumigationOperation operation = operationService.confirmEvacuation(request, getClientIp(httpRequest));
        return ApiResponse.success("人员撤离确认完成", operation);
    }

    @PostMapping("/pesticide/apply")
    public ApiResponse<FumigationOperation> applyPesticide(
            @Valid @RequestBody ApplyPesticideRequest request,
            HttpServletRequest httpRequest) {
        FumigationOperation operation = operationService.applyPesticide(request, getClientIp(httpRequest));
        return ApiResponse.success("投药完成", operation);
    }

    @PostMapping("/ventilation/detect")
    public ApiResponse<FumigationOperation> ventilationDetection(
            @Valid @RequestBody VentilationRequest request,
            HttpServletRequest httpRequest) {
        FumigationOperation operation = operationService.ventilationDetection(request, getClientIp(httpRequest));
        String msg = request.getVentilationPassed() ? "通风检测合格" : "通风进行中";
        return ApiResponse.success(msg, operation);
    }

    @PostMapping("/alert/lift")
    public ApiResponse<FumigationOperation> liftAlert(
            @Valid @RequestBody LiftAlertRequest request,
            HttpServletRequest httpRequest) {
        FumigationOperation operation = operationService.liftAlert(request, getClientIp(httpRequest));
        return ApiResponse.success("警戒已解除", operation);
    }

    @GetMapping("/ticket/{id}")
    public ApiResponse<FumigationOperation> getTicketById(
            @PathVariable Long id) {
        FumigationOperation operation = operationService.getOperationById(id);
        return ApiResponse.success(operation);
    }

    @GetMapping("/ticket/no/{ticketNo}")
    public ApiResponse<FumigationOperation> getTicketByNo(
            @PathVariable String ticketNo) {
        FumigationOperation operation = operationService.getOperationByTicketNo(ticketNo);
        return ApiResponse.success(operation);
    }

    @GetMapping("/tickets")
    public ApiResponse<List<FumigationOperation>> getAllTickets() {
        List<FumigationOperation> operations = operationService.getAllOperations();
        return ApiResponse.success(operations);
    }

    @GetMapping("/tickets/status/{status}")
    public ApiResponse<List<FumigationOperation>> getTicketsByStatus(
            @PathVariable OperationStatus status) {
        List<FumigationOperation> operations = operationService.getOperationsByStatus(status);
        return ApiResponse.success(operations);
    }

    @GetMapping("/tickets/warehouse/{warehouseCode}")
    public ApiResponse<List<FumigationOperation>> getTicketsByWarehouse(
            @PathVariable String warehouseCode) {
        List<FumigationOperation> operations = operationService.getOperationsByWarehouse(warehouseCode);
        return ApiResponse.success(operations);
    }

    @PostMapping("/ticket/cancel/{id}")
    public ApiResponse<FumigationOperation> cancelTicket(
            @PathVariable Long id,
            @RequestParam String operatorId,
            @RequestParam(required = false) String operatorName,
            HttpServletRequest httpRequest) {
        FumigationOperation operation = operationService.cancelTicket(id, operatorId, operatorName, getClientIp(httpRequest));
        return ApiResponse.success("作业票已取消", operation);
    }
}
