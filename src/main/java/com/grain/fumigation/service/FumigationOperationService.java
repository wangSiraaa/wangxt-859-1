package com.grain.fumigation.service;

import com.grain.fumigation.config.FumigationConfig;
import com.grain.fumigation.dto.*;
import com.grain.fumigation.entity.FumigationOperation;
import com.grain.fumigation.enums.AuditOperationType;
import com.grain.fumigation.enums.OperationRole;
import com.grain.fumigation.enums.OperationStatus;
import com.grain.fumigation.exception.BusinessException;
import com.grain.fumigation.repository.FumigationOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FumigationOperationService {

    private static final Logger log = LoggerFactory.getLogger(FumigationOperationService.class);

    @Autowired
    private FumigationOperationRepository operationRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private FumigationConfig fumigationConfig;

    private final AtomicLong ticketCounter = new AtomicLong(0);

    private String generateTicketNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = LocalDateTime.now().format(formatter);
        long seq = ticketCounter.incrementAndGet();
        return String.format("FUM%s%04d", dateStr, seq);
    }

    @Transactional
    public FumigationOperation createTicket(CreateTicketRequest request, String ipAddress) {
        FumigationOperation operation = new FumigationOperation();
        operation.setTicketNo(generateTicketNo());
        operation.setWarehouseCode(request.getWarehouseCode());
        operation.setWarehouseName(request.getWarehouseName());
        operation.setGrainType(request.getGrainType());
        operation.setGrainQuantity(request.getGrainQuantity());
        operation.setKeeperId(request.getKeeperId());
        operation.setKeeperName(request.getKeeperName());
        operation.setSafetyOfficerId(request.getSafetyOfficerId());
        operation.setSafetyOfficerName(request.getSafetyOfficerName());
        operation.setOperationManagerId(request.getOperationManagerId());
        operation.setOperationManagerName(request.getOperationManagerName());
        operation.setStatus(OperationStatus.DRAFT);
        operation.setCreateBy(request.getKeeperId());
        operation.setEvacuationConfirmed(false);
        operation.setVentilationPassed(false);
        operation.setAlertLifted(false);

        operation = operationRepository.save(operation);

        String detail = String.format("创建熏蒸作业票，仓房:%s, 粮食品种:%s", 
                operation.getWarehouseCode(), operation.getGrainType());
        auditLogService.createAuditLog(
                operation,
                AuditOperationType.CREATE_TICKET,
                OperationRole.KEEPER,
                request.getKeeperId(),
                request.getKeeperName(),
                detail,
                null,
                OperationStatus.DRAFT,
                true,
                null,
                ipAddress
        );

        return operation;
    }

    @Transactional
    public FumigationOperation submitTicket(SubmitTicketRequest request, String ipAddress) {
        FumigationOperation operation = getOperationById(request.getOperationId());

        if (operation.getStatus() != OperationStatus.DRAFT) {
            String errorMsg = String.format("当前状态[%s]不允许提交，仅草稿状态可提交", operation.getStatus().getDescription());
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.SUBMIT_TICKET,
                    OperationRole.KEEPER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            throw new BusinessException(errorMsg);
        }

        OperationStatus beforeStatus = operation.getStatus();
        operation.setStatus(OperationStatus.SUBMITTED);
        operation.setUpdateBy(request.getOperatorId());
        operation = operationRepository.save(operation);

        String detail = String.format("提交熏蒸作业票[%s]审批", operation.getTicketNo());
        auditLogService.createAuditLog(
                operation,
                AuditOperationType.SUBMIT_TICKET,
                OperationRole.KEEPER,
                request.getOperatorId(),
                request.getOperatorName(),
                detail,
                beforeStatus,
                OperationStatus.SUBMITTED,
                true,
                null,
                ipAddress
        );

        return operation;
    }

    @Transactional
    public FumigationOperation approveTicket(ApproveTicketRequest request, String ipAddress) {
        FumigationOperation operation = getOperationById(request.getOperationId());

        if (operation.getStatus() != OperationStatus.SUBMITTED) {
            String errorMsg = String.format("当前状态[%s]不允许审批", operation.getStatus().getDescription());
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.APPROVE_TICKET,
                    OperationRole.OPERATION_MANAGER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            throw new BusinessException(errorMsg);
        }

        OperationStatus beforeStatus = operation.getStatus();
        AuditOperationType auditType;

        if (request.getApproved()) {
            operation.setStatus(OperationStatus.APPROVED);
            operation.setApproveRemark(request.getApproveRemark());
            operation.setApproveTime(LocalDateTime.now());
            auditType = AuditOperationType.APPROVE_TICKET;
        } else {
            operation.setStatus(OperationStatus.REJECTED);
            operation.setRejectReason(request.getRejectReason());
            operation.setApproveTime(LocalDateTime.now());
            auditType = AuditOperationType.REJECT_TICKET;
        }
        operation.setUpdateBy(request.getOperatorId());
        operation = operationRepository.save(operation);

        String result = request.getApproved() ? "通过" : "驳回";
        String detail = String.format("审批作业票[%s]，结果:%s，备注:%s", 
                operation.getTicketNo(), result, request.getApproved() ? request.getApproveRemark() : request.getRejectReason());
        auditLogService.createAuditLog(
                operation,
                auditType,
                OperationRole.OPERATION_MANAGER,
                request.getOperatorId(),
                request.getOperatorName(),
                detail,
                beforeStatus,
                operation.getStatus(),
                true,
                null,
                ipAddress
        );

        return operation;
    }

    @Transactional
    public FumigationOperation confirmEvacuation(ConfirmEvacuationRequest request, String ipAddress) {
        FumigationOperation operation = getOperationById(request.getOperationId());

        if (operation.getStatus() != OperationStatus.APPROVED) {
            String errorMsg = String.format("当前状态[%s]不允许确认人员撤离，需审批通过后操作", operation.getStatus().getDescription());
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.CONFIRM_EVACUATION,
                    OperationRole.SAFETY_OFFICER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            throw new BusinessException(errorMsg);
        }

        if (!request.getEvacuationConfirmed()) {
            String errorMsg = "人员未撤离，不允许进行投药操作";
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.CONFIRM_EVACUATION,
                    OperationRole.SAFETY_OFFICER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            throw new BusinessException(errorMsg);
        }

        OperationStatus beforeStatus = operation.getStatus();
        operation.setEvacuationConfirmed(true);
        operation.setEvacuationConfirmTime(LocalDateTime.now());
        operation.setEvacuationRemark(request.getEvacuationRemark());
        operation.setStatus(OperationStatus.EVACUATION_CONFIRMED);
        operation.setUpdateBy(request.getOperatorId());
        operation = operationRepository.save(operation);

        String detail = String.format("确认人员已撤离，仓房:%s，备注:%s", 
                operation.getWarehouseCode(), request.getEvacuationRemark());
        auditLogService.createAuditLog(
                operation,
                AuditOperationType.CONFIRM_EVACUATION,
                OperationRole.SAFETY_OFFICER,
                request.getOperatorId(),
                request.getOperatorName(),
                detail,
                beforeStatus,
                OperationStatus.EVACUATION_CONFIRMED,
                true,
                null,
                ipAddress
        );

        return operation;
    }

    @Transactional
    public FumigationOperation applyPesticide(ApplyPesticideRequest request, String ipAddress) {
        FumigationOperation operation = getOperationById(request.getOperationId());

        if (operation.getStatus() != OperationStatus.EVACUATION_CONFIRMED) {
            String errorMsg = String.format("当前状态[%s]不允许投药，需人员撤离确认后操作", operation.getStatus().getDescription());
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.APPLY_PESTICIDE,
                    OperationRole.OPERATION_MANAGER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            throw new BusinessException(errorMsg);
        }

        if (!Boolean.TRUE.equals(operation.getEvacuationConfirmed())) {
            String errorMsg = "人员未撤离，不能投药！";
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.APPLY_PESTICIDE,
                    OperationRole.OPERATION_MANAGER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            BusinessException ex = new BusinessException(errorMsg);
            ex.setOperationId(operation.getId());
            ex.setTicketNo(operation.getTicketNo());
            ex.setOperationType(AuditOperationType.APPLY_PESTICIDE);
            ex.setOperatorRole(OperationRole.OPERATION_MANAGER);
            ex.setOperatorId(request.getOperatorId());
            ex.setOperatorName(request.getOperatorName());
            ex.setBeforeStatus(operation.getStatus());
            ex.setAfterStatus(operation.getStatus());
            ex.setIpAddress(ipAddress);
            ex.setAuditLogged(true);
            throw ex;
        }

        BigDecimal maxDosage = fumigationConfig.getMaxPesticideDosage();
        if (request.getPesticideDosage().compareTo(maxDosage) > 0) {
            String errorMsg = String.format("药剂用量[%.2f]超过最大限值[%.2f]，申请被驳回！", 
                    request.getPesticideDosage(), maxDosage);
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.APPLY_PESTICIDE,
                    OperationRole.OPERATION_MANAGER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            BusinessException ex = new BusinessException(406, errorMsg);
            ex.setOperationId(operation.getId());
            ex.setTicketNo(operation.getTicketNo());
            ex.setOperationType(AuditOperationType.APPLY_PESTICIDE);
            ex.setOperatorRole(OperationRole.OPERATION_MANAGER);
            ex.setOperatorId(request.getOperatorId());
            ex.setOperatorName(request.getOperatorName());
            ex.setBeforeStatus(operation.getStatus());
            ex.setAfterStatus(operation.getStatus());
            ex.setIpAddress(ipAddress);
            ex.setAuditLogged(true);
            throw ex;
        }

        OperationStatus beforeStatus = operation.getStatus();
        operation.setPesticideType(request.getPesticideType());
        operation.setPesticideDosage(request.getPesticideDosage());
        operation.setPesticideApplyTime(LocalDateTime.now());
        operation.setPesticideRemark(request.getPesticideRemark());
        operation.setStatus(OperationStatus.PESTICIDE_APPLIED);
        operation.setUpdateBy(request.getOperatorId());
        operation = operationRepository.save(operation);

        String detail = String.format("投药完成，药剂类型:%s，用量:%.2f，备注:%s", 
                request.getPesticideType(), request.getPesticideDosage(), request.getPesticideRemark());
        auditLogService.createAuditLog(
                operation,
                AuditOperationType.APPLY_PESTICIDE,
                OperationRole.OPERATION_MANAGER,
                request.getOperatorId(),
                request.getOperatorName(),
                detail,
                beforeStatus,
                OperationStatus.PESTICIDE_APPLIED,
                true,
                null,
                ipAddress
        );

        return operation;
    }

    @Transactional
    public FumigationOperation ventilationDetection(VentilationRequest request, String ipAddress) {
        FumigationOperation operation = getOperationById(request.getOperationId());

        if (operation.getStatus() != OperationStatus.PESTICIDE_APPLIED) {
            String errorMsg = String.format("当前状态[%s]不允许通风检测，需投药完成后操作", operation.getStatus().getDescription());
            auditLogService.createAuditLog(
                    operation,
                    request.getVentilationPassed() ? AuditOperationType.COMPLETE_VENTILATION : AuditOperationType.START_VENTILATION,
                    OperationRole.OPERATION_MANAGER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            throw new BusinessException(errorMsg);
        }

        LocalDateTime startTime = request.getVentilationStartTime() != null ? request.getVentilationStartTime() : LocalDateTime.now();
        LocalDateTime endTime = request.getVentilationEndTime() != null ? request.getVentilationEndTime() : LocalDateTime.now();
        BigDecimal duration = request.getVentilationDurationHours();

        if (duration == null) {
            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
            duration = new BigDecimal(minutes).divide(new BigDecimal("60"), 1, BigDecimal.ROUND_HALF_UP);
        }

        BigDecimal allowableConcentration = fumigationConfig.getAllowableGasConcentration();
        if (request.getGasConcentration().compareTo(allowableConcentration) > 0) {
            String errorMsg = String.format("毒气浓度[%.2f]超过允许值[%.2f]，通风检测不合格！", 
                    request.getGasConcentration(), allowableConcentration);
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.COMPLETE_VENTILATION,
                    OperationRole.OPERATION_MANAGER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            throw new BusinessException(errorMsg);
        }

        OperationStatus beforeStatus = operation.getStatus();
        operation.setVentilationStartTime(startTime);
        operation.setVentilationEndTime(endTime);
        operation.setVentilationDurationHours(duration);
        operation.setGasConcentration(request.getGasConcentration());
        operation.setVentilationPassed(request.getVentilationPassed());
        operation.setVentilationRemark(request.getVentilationRemark());

        if (request.getVentilationPassed()) {
            operation.setStatus(OperationStatus.VENTILATION_COMPLETED);
        } else {
            operation.setStatus(OperationStatus.VENTILATION_IN_PROGRESS);
        }
        operation.setUpdateBy(request.getOperatorId());
        operation = operationRepository.save(operation);

        String result = request.getVentilationPassed() ? "合格" : "进行中";
        String detail = String.format("通风检测，时长:%.1f小时，毒气浓度:%.2f，结果:%s，备注:%s", 
                duration, request.getGasConcentration(), result, request.getVentilationRemark());
        auditLogService.createAuditLog(
                operation,
                request.getVentilationPassed() ? AuditOperationType.COMPLETE_VENTILATION : AuditOperationType.START_VENTILATION,
                OperationRole.OPERATION_MANAGER,
                request.getOperatorId(),
                request.getOperatorName(),
                detail,
                beforeStatus,
                operation.getStatus(),
                true,
                null,
                ipAddress
        );

        return operation;
    }

    @Transactional
    public FumigationOperation liftAlert(LiftAlertRequest request, String ipAddress) {
        FumigationOperation operation = getOperationById(request.getOperationId());

        if (operation.getStatus() != OperationStatus.VENTILATION_COMPLETED) {
            String errorMsg = String.format("当前状态[%s]不允许解除警戒，需通风检测合格后操作", operation.getStatus().getDescription());
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.LIFT_ALERT,
                    OperationRole.OPERATION_MANAGER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            throw new BusinessException(errorMsg);
        }

        if (!Boolean.TRUE.equals(operation.getVentilationPassed())) {
            String errorMsg = "通风检测未合格，不能解除警戒！";
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.LIFT_ALERT,
                    OperationRole.OPERATION_MANAGER,
                    request.getOperatorId(),
                    request.getOperatorName(),
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            BusinessException ex = new BusinessException(errorMsg);
            ex.setOperationId(operation.getId());
            ex.setTicketNo(operation.getTicketNo());
            ex.setOperationType(AuditOperationType.LIFT_ALERT);
            ex.setOperatorRole(OperationRole.OPERATION_MANAGER);
            ex.setOperatorId(request.getOperatorId());
            ex.setOperatorName(request.getOperatorName());
            ex.setBeforeStatus(operation.getStatus());
            ex.setAfterStatus(operation.getStatus());
            ex.setIpAddress(ipAddress);
            ex.setAuditLogged(true);
            throw ex;
        }

        OperationStatus beforeStatus = operation.getStatus();
        operation.setAlertLifted(true);
        operation.setAlertLiftTime(LocalDateTime.now());
        operation.setAlertLiftRemark(request.getAlertLiftRemark());
        operation.setStatus(OperationStatus.ALERT_LIFTED);
        operation.setUpdateBy(request.getOperatorId());
        operation = operationRepository.save(operation);

        String detail = String.format("解除警戒完成，备注:%s", request.getAlertLiftRemark());
        auditLogService.createAuditLog(
                operation,
                AuditOperationType.LIFT_ALERT,
                OperationRole.OPERATION_MANAGER,
                request.getOperatorId(),
                request.getOperatorName(),
                detail,
                beforeStatus,
                OperationStatus.ALERT_LIFTED,
                true,
                null,
                ipAddress
        );

        return operation;
    }

    public FumigationOperation getOperationById(Long id) {
        Optional<FumigationOperation> opt = operationRepository.findById(id);
        return opt.orElseThrow(() -> new BusinessException("作业票不存在，ID:" + id));
    }

    public FumigationOperation getOperationByTicketNo(String ticketNo) {
        Optional<FumigationOperation> opt = operationRepository.findByTicketNo(ticketNo);
        return opt.orElseThrow(() -> new BusinessException("作业票不存在，编号:" + ticketNo));
    }

    public List<FumigationOperation> getAllOperations() {
        return operationRepository.findAll();
    }

    public List<FumigationOperation> getOperationsByStatus(OperationStatus status) {
        return operationRepository.findByStatus(status);
    }

    public List<FumigationOperation> getOperationsByWarehouse(String warehouseCode) {
        return operationRepository.findByWarehouseCode(warehouseCode);
    }

    @Transactional
    public FumigationOperation cancelTicket(Long operationId, String operatorId, String operatorName, String ipAddress) {
        FumigationOperation operation = getOperationById(operationId);

        if (operation.getStatus() == OperationStatus.ALERT_LIFTED || 
            operation.getStatus() == OperationStatus.CANCELLED) {
            String errorMsg = String.format("当前状态[%s]不允许取消", operation.getStatus().getDescription());
            auditLogService.createAuditLog(
                    operation,
                    AuditOperationType.CANCEL_TICKET,
                    OperationRole.OPERATION_MANAGER,
                    operatorId,
                    operatorName,
                    errorMsg,
                    operation.getStatus(),
                    operation.getStatus(),
                    false,
                    errorMsg,
                    ipAddress
            );
            throw new BusinessException(errorMsg);
        }

        OperationStatus beforeStatus = operation.getStatus();
        operation.setStatus(OperationStatus.CANCELLED);
        operation.setUpdateBy(operatorId);
        operation = operationRepository.save(operation);

        String detail = String.format("取消作业票[%s]", operation.getTicketNo());
        auditLogService.createAuditLog(
                operation,
                AuditOperationType.CANCEL_TICKET,
                OperationRole.OPERATION_MANAGER,
                operatorId,
                operatorName,
                detail,
                beforeStatus,
                OperationStatus.CANCELLED,
                true,
                null,
                ipAddress
        );

        return operation;
    }
}
