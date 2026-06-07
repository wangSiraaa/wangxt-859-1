package com.grain.fumigation;

import com.grain.fumigation.config.FumigationConfig;
import com.grain.fumigation.dto.*;
import com.grain.fumigation.entity.FumigationOperation;
import com.grain.fumigation.enums.OperationStatus;
import com.grain.fumigation.exception.BusinessException;
import com.grain.fumigation.repository.AuditLogRepository;
import com.grain.fumigation.repository.FumigationOperationRepository;
import com.grain.fumigation.service.AuditLogService;
import com.grain.fumigation.service.FumigationOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class FumigationOperationServiceTest {

    @Autowired
    private FumigationOperationService operationService;

    @Autowired
    private FumigationOperationRepository operationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private FumigationConfig fumigationConfig;

    private String testIp = "127.0.0.1";

    @Test
    @DisplayName("创建作业票成功")
    void testCreateTicket_Success() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setWarehouseCode("WH001");
        request.setWarehouseName("1号仓房");
        request.setGrainType("小麦");
        request.setGrainQuantity(new BigDecimal("1000"));
        request.setKeeperId("K001");
        request.setKeeperName("张三");

        FumigationOperation operation = operationService.createTicket(request, testIp);

        assertNotNull(operation);
        assertNotNull(operation.getId());
        assertNotNull(operation.getTicketNo());
        assertEquals("WH001", operation.getWarehouseCode());
        assertEquals("小麦", operation.getGrainType());
        assertEquals(OperationStatus.DRAFT, operation.getStatus());
        assertEquals("K001", operation.getKeeperId());
        assertFalse(operation.getEvacuationConfirmed());
        assertFalse(operation.getVentilationPassed());
        assertFalse(operation.getAlertLifted());
    }

    @Test
    @DisplayName("提交作业票成功")
    void testSubmitTicket_Success() {
        FumigationOperation operation = createDraftTicket();

        SubmitTicketRequest request = new SubmitTicketRequest();
        request.setOperationId(operation.getId());
        request.setOperatorId("K001");
        request.setOperatorName("张三");

        FumigationOperation result = operationService.submitTicket(request, testIp);

        assertEquals(OperationStatus.SUBMITTED, result.getStatus());
    }

    @Test
    @DisplayName("提交非草稿状态作业票失败")
    void testSubmitTicket_NotDraft_Failure() {
        FumigationOperation operation = createDraftTicket();
        operation.setStatus(OperationStatus.SUBMITTED);
        operationRepository.save(operation);

        SubmitTicketRequest request = new SubmitTicketRequest();
        request.setOperationId(operation.getId());
        request.setOperatorId("K001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            operationService.submitTicket(request, testIp);
        });

        assertTrue(exception.getMessage().contains("不允许提交"));
    }

    @Test
    @DisplayName("审批通过作业票成功")
    void testApproveTicket_Approved_Success() {
        FumigationOperation operation = createSubmittedTicket();

        ApproveTicketRequest request = new ApproveTicketRequest();
        request.setOperationId(operation.getId());
        request.setApproved(true);
        request.setOperatorId("M001");
        request.setOperatorName("作业负责人");
        request.setApproveRemark("同意作业");

        FumigationOperation result = operationService.approveTicket(request, testIp);

        assertEquals(OperationStatus.APPROVED, result.getStatus());
        assertEquals("同意作业", result.getApproveRemark());
        assertNotNull(result.getApproveTime());
    }

    @Test
    @DisplayName("审批驳回作业票成功")
    void testApproveTicket_Rejected_Success() {
        FumigationOperation operation = createSubmittedTicket();

        ApproveTicketRequest request = new ApproveTicketRequest();
        request.setOperationId(operation.getId());
        request.setApproved(false);
        request.setOperatorId("M001");
        request.setRejectReason("药剂用量需要重新评估");

        FumigationOperation result = operationService.approveTicket(request, testIp);

        assertEquals(OperationStatus.REJECTED, result.getStatus());
        assertEquals("药剂用量需要重新评估", result.getRejectReason());
    }

    @Test
    @DisplayName("确认人员撤离成功")
    void testConfirmEvacuation_Success() {
        FumigationOperation operation = createApprovedTicket();

        ConfirmEvacuationRequest request = new ConfirmEvacuationRequest();
        request.setOperationId(operation.getId());
        request.setEvacuationConfirmed(true);
        request.setOperatorId("S001");
        request.setOperatorName("安全员");
        request.setEvacuationRemark("人员已全部撤离");

        FumigationOperation result = operationService.confirmEvacuation(request, testIp);

        assertTrue(result.getEvacuationConfirmed());
        assertEquals(OperationStatus.EVACUATION_CONFIRMED, result.getStatus());
        assertNotNull(result.getEvacuationConfirmTime());
    }

    @Test
    @DisplayName("人员未撤离失败")
    void testConfirmEvacuation_NotConfirmed_Failure() {
        FumigationOperation operation = createApprovedTicket();

        ConfirmEvacuationRequest request = new ConfirmEvacuationRequest();
        request.setOperationId(operation.getId());
        request.setEvacuationConfirmed(false);
        request.setOperatorId("S001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            operationService.confirmEvacuation(request, testIp);
        });

        assertEquals("人员未撤离，不允许进行投药操作", exception.getMessage());
    }

    @Test
    @DisplayName("投药成功")
    void testApplyPesticide_Success() {
        FumigationOperation operation = createEvacuationConfirmedTicket();

        ApplyPesticideRequest request = new ApplyPesticideRequest();
        request.setOperationId(operation.getId());
        request.setPesticideType("磷化铝");
        request.setPesticideDosage(new BigDecimal("30"));
        request.setOperatorId("M001");
        request.setOperatorName("作业负责人");
        request.setPesticideRemark("按标准用量投药");

        FumigationOperation result = operationService.applyPesticide(request, testIp);

        assertEquals(OperationStatus.PESTICIDE_APPLIED, result.getStatus());
        assertEquals("磷化铝", result.getPesticideType());
        assertEquals(0, result.getPesticideDosage().compareTo(new BigDecimal("30")));
        assertNotNull(result.getPesticideApplyTime());
    }

    @Test
    @DisplayName("药剂超量失败 - 核心验证场景")
    void testApplyPesticide_OverDosage_Failure() {
        FumigationOperation operation = createEvacuationConfirmedTicket();

        BigDecimal maxDosage = fumigationConfig.getMaxPesticideDosage();
        BigDecimal overDosage = maxDosage.add(new BigDecimal("10"));

        ApplyPesticideRequest request = new ApplyPesticideRequest();
        request.setOperationId(operation.getId());
        request.setPesticideType("磷化铝");
        request.setPesticideDosage(overDosage);
        request.setOperatorId("M001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            operationService.applyPesticide(request, testIp);
        });

        assertEquals(406, exception.getCode());
        assertTrue(exception.getMessage().contains("超过最大限值"));
        assertTrue(exception.getMessage().contains("申请被驳回"));
    }

    @Test
    @DisplayName("人员未撤离投药失败")
    void testApplyPesticide_EvacuationNotConfirmed_Failure() {
        FumigationOperation operation = createApprovedTicket();
        operation.setEvacuationConfirmed(false);
        operation.setStatus(OperationStatus.EVACUATION_CONFIRMED);
        operationRepository.save(operation);

        ApplyPesticideRequest request = new ApplyPesticideRequest();
        request.setOperationId(operation.getId());
        request.setPesticideType("磷化铝");
        request.setPesticideDosage(new BigDecimal("30"));
        request.setOperatorId("M001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            operationService.applyPesticide(request, testIp);
        });

        assertEquals("人员未撤离，不能投药！", exception.getMessage());
    }

    @Test
    @DisplayName("通风检测合格成功")
    void testVentilationDetection_Passed_Success() {
        FumigationOperation operation = createPesticideAppliedTicket();

        VentilationRequest request = new VentilationRequest();
        request.setOperationId(operation.getId());
        request.setGasConcentration(new BigDecimal("0.2"));
        request.setVentilationPassed(true);
        request.setOperatorId("M001");
        request.setVentilationRemark("通风4小时，检测合格");

        FumigationOperation result = operationService.ventilationDetection(request, testIp);

        assertEquals(OperationStatus.VENTILATION_COMPLETED, result.getStatus());
        assertTrue(result.getVentilationPassed());
        assertEquals(0, result.getGasConcentration().compareTo(new BigDecimal("0.2")));
    }

    @Test
    @DisplayName("毒气浓度超标失败")
    void testVentilationDetection_OverConcentration_Failure() {
        FumigationOperation operation = createPesticideAppliedTicket();

        BigDecimal allowableConcentration = fumigationConfig.getAllowableGasConcentration();
        BigDecimal overConcentration = allowableConcentration.add(new BigDecimal("0.5"));

        VentilationRequest request = new VentilationRequest();
        request.setOperationId(operation.getId());
        request.setGasConcentration(overConcentration);
        request.setVentilationPassed(true);
        request.setOperatorId("M001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            operationService.ventilationDetection(request, testIp);
        });

        assertTrue(exception.getMessage().contains("超过允许值"));
        assertTrue(exception.getMessage().contains("通风检测不合格"));
    }

    @Test
    @DisplayName("解除警戒成功")
    void testLiftAlert_Success() {
        FumigationOperation operation = createVentilationCompletedTicket();

        LiftAlertRequest request = new LiftAlertRequest();
        request.setOperationId(operation.getId());
        request.setOperatorId("M001");
        request.setOperatorName("作业负责人");
        request.setAlertLiftRemark("安全，解除警戒");

        FumigationOperation result = operationService.liftAlert(request, testIp);

        assertEquals(OperationStatus.ALERT_LIFTED, result.getStatus());
        assertTrue(result.getAlertLifted());
        assertNotNull(result.getAlertLiftTime());
    }

    @Test
    @DisplayName("通风检测未合格解除警戒失败")
    void testLiftAlert_VentilationNotPassed_Failure() {
        FumigationOperation operation = createPesticideAppliedTicket();
        operation.setStatus(OperationStatus.VENTILATION_COMPLETED);
        operation.setVentilationPassed(false);
        operationRepository.save(operation);

        LiftAlertRequest request = new LiftAlertRequest();
        request.setOperationId(operation.getId());
        request.setOperatorId("M001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            operationService.liftAlert(request, testIp);
        });

        assertEquals("通风检测未合格，不能解除警戒！", exception.getMessage());
    }

    @Test
    @DisplayName("完整流程测试")
    void testFullWorkflow_Success() {
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setWarehouseCode("WH002");
        createRequest.setGrainType("玉米");
        createRequest.setKeeperId("K002");
        createRequest.setKeeperName("李四");
        FumigationOperation operation = operationService.createTicket(createRequest, testIp);
        Long operationId = operation.getId();

        SubmitTicketRequest submitRequest = new SubmitTicketRequest();
        submitRequest.setOperationId(operationId);
        submitRequest.setOperatorId("K002");
        operation = operationService.submitTicket(submitRequest, testIp);
        assertEquals(OperationStatus.SUBMITTED, operation.getStatus());

        ApproveTicketRequest approveRequest = new ApproveTicketRequest();
        approveRequest.setOperationId(operationId);
        approveRequest.setApproved(true);
        approveRequest.setOperatorId("M002");
        operation = operationService.approveTicket(approveRequest, testIp);
        assertEquals(OperationStatus.APPROVED, operation.getStatus());

        ConfirmEvacuationRequest evacuationRequest = new ConfirmEvacuationRequest();
        evacuationRequest.setOperationId(operationId);
        evacuationRequest.setEvacuationConfirmed(true);
        evacuationRequest.setOperatorId("S002");
        operation = operationService.confirmEvacuation(evacuationRequest, testIp);
        assertEquals(OperationStatus.EVACUATION_CONFIRMED, operation.getStatus());

        ApplyPesticideRequest pesticideRequest = new ApplyPesticideRequest();
        pesticideRequest.setOperationId(operationId);
        pesticideRequest.setPesticideType("磷化铝");
        pesticideRequest.setPesticideDosage(new BigDecimal("25"));
        pesticideRequest.setOperatorId("M002");
        operation = operationService.applyPesticide(pesticideRequest, testIp);
        assertEquals(OperationStatus.PESTICIDE_APPLIED, operation.getStatus());

        VentilationRequest ventilationRequest = new VentilationRequest();
        ventilationRequest.setOperationId(operationId);
        ventilationRequest.setGasConcentration(new BigDecimal("0.3"));
        ventilationRequest.setVentilationPassed(true);
        ventilationRequest.setOperatorId("M002");
        operation = operationService.ventilationDetection(ventilationRequest, testIp);
        assertEquals(OperationStatus.VENTILATION_COMPLETED, operation.getStatus());

        LiftAlertRequest liftRequest = new LiftAlertRequest();
        liftRequest.setOperationId(operationId);
        liftRequest.setOperatorId("M002");
        operation = operationService.liftAlert(liftRequest, testIp);
        assertEquals(OperationStatus.ALERT_LIFTED, operation.getStatus());

        assertEquals(7, auditLogRepository.findByOperationIdOrderByOperationTimeDesc(operationId).stream()
                .filter(log -> log.getSuccess()).count());
    }

    @Test
    @DisplayName("超药剂用量审批失败 - 容器启动后验证场景")
    void testContainerStartup_OverDosageRejection() {
        FumigationOperation operation = createEvacuationConfirmedTicket();
        Long operationId = operation.getId();

        BigDecimal maxDosage = fumigationConfig.getMaxPesticideDosage();
        System.out.println("当前最大药剂用量限值: " + maxDosage);

        BigDecimal dosageWithinLimit = maxDosage.subtract(new BigDecimal("5"));
        ApplyPesticideRequest validRequest = new ApplyPesticideRequest();
        validRequest.setOperationId(operationId);
        validRequest.setPesticideType("磷化铝");
        validRequest.setPesticideDosage(dosageWithinLimit);
        validRequest.setOperatorId("M001");

        FumigationOperation validResult = operationService.applyPesticide(validRequest, testIp);
        assertEquals(OperationStatus.PESTICIDE_APPLIED, validResult.getStatus());
        System.out.println("✅ 正常用量(" + dosageWithinLimit + ")投药成功");

        FumigationOperation operation2 = createAnotherEvacuationConfirmedTicket();
        Long operationId2 = operation2.getId();

        BigDecimal dosageOverLimit = maxDosage.add(new BigDecimal("15"));
        ApplyPesticideRequest overRequest = new ApplyPesticideRequest();
        overRequest.setOperationId(operationId2);
        overRequest.setPesticideType("磷化铝");
        overRequest.setPesticideDosage(dosageOverLimit);
        overRequest.setOperatorId("M001");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            operationService.applyPesticide(overRequest, testIp);
        });

        assertEquals(406, exception.getCode());
        assertTrue(exception.getMessage().contains(String.format("%.2f", dosageOverLimit)));
        assertTrue(exception.getMessage().contains(String.format("%.2f", maxDosage)));
        System.out.println("❌ 超量(" + dosageOverLimit + ")投药被正确驳回，错误码: " + exception.getCode());
        System.out.println("   错误信息: " + exception.getMessage());

        FumigationOperation checkedOperation = operationService.getOperationById(operationId2);
        assertEquals(OperationStatus.EVACUATION_CONFIRMED, checkedOperation.getStatus());
        assertNull(checkedOperation.getPesticideDosage());
        System.out.println("✅ 超量投药后状态保持不变，数据未被污染");
    }

    private FumigationOperation createDraftTicket() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setWarehouseCode("WH001");
        request.setGrainType("小麦");
        request.setKeeperId("K001");
        request.setKeeperName("张三");
        return operationService.createTicket(request, testIp);
    }

    private FumigationOperation createSubmittedTicket() {
        FumigationOperation operation = createDraftTicket();
        SubmitTicketRequest request = new SubmitTicketRequest();
        request.setOperationId(operation.getId());
        request.setOperatorId("K001");
        return operationService.submitTicket(request, testIp);
    }

    private FumigationOperation createApprovedTicket() {
        FumigationOperation operation = createSubmittedTicket();
        ApproveTicketRequest request = new ApproveTicketRequest();
        request.setOperationId(operation.getId());
        request.setApproved(true);
        request.setOperatorId("M001");
        return operationService.approveTicket(request, testIp);
    }

    private FumigationOperation createEvacuationConfirmedTicket() {
        FumigationOperation operation = createApprovedTicket();
        ConfirmEvacuationRequest request = new ConfirmEvacuationRequest();
        request.setOperationId(operation.getId());
        request.setEvacuationConfirmed(true);
        request.setOperatorId("S001");
        return operationService.confirmEvacuation(request, testIp);
    }

    private FumigationOperation createAnotherEvacuationConfirmedTicket() {
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setWarehouseCode("WH003");
        createRequest.setGrainType("大豆");
        createRequest.setKeeperId("K003");
        FumigationOperation operation = operationService.createTicket(createRequest, testIp);

        SubmitTicketRequest submitRequest = new SubmitTicketRequest();
        submitRequest.setOperationId(operation.getId());
        submitRequest.setOperatorId("K003");
        operation = operationService.submitTicket(submitRequest, testIp);

        ApproveTicketRequest approveRequest = new ApproveTicketRequest();
        approveRequest.setOperationId(operation.getId());
        approveRequest.setApproved(true);
        approveRequest.setOperatorId("M003");
        operation = operationService.approveTicket(approveRequest, testIp);

        ConfirmEvacuationRequest evacuationRequest = new ConfirmEvacuationRequest();
        evacuationRequest.setOperationId(operation.getId());
        evacuationRequest.setEvacuationConfirmed(true);
        evacuationRequest.setOperatorId("S003");
        return operationService.confirmEvacuation(evacuationRequest, testIp);
    }

    private FumigationOperation createPesticideAppliedTicket() {
        FumigationOperation operation = createEvacuationConfirmedTicket();
        ApplyPesticideRequest request = new ApplyPesticideRequest();
        request.setOperationId(operation.getId());
        request.setPesticideType("磷化铝");
        request.setPesticideDosage(new BigDecimal("20"));
        request.setOperatorId("M001");
        return operationService.applyPesticide(request, testIp);
    }

    private FumigationOperation createVentilationCompletedTicket() {
        FumigationOperation operation = createPesticideAppliedTicket();
        VentilationRequest request = new VentilationRequest();
        request.setOperationId(operation.getId());
        request.setGasConcentration(new BigDecimal("0.3"));
        request.setVentilationPassed(true);
        request.setOperatorId("M001");
        return operationService.ventilationDetection(request, testIp);
    }
}
