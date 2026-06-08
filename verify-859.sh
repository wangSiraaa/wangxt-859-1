#!/bin/bash

BASE_URL="http://localhost:18080/api/fumigation"
AUDIT_URL="http://localhost:18080/api/audit"
PASS=0
FAIL=0
TOTAL=0

pass() { PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); echo "✅ PASS: $1"; }
fail() { FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); echo "❌ FAIL: $1"; echo "   Expected: $2"; echo "   Got: $3"; }
check() { if [ "$2" = "$3" ]; then pass "$1"; else fail "$1" "$2" "$3"; fi; }
check_contains() { if echo "$3" | grep -q "$2"; then pass "$1"; else fail "$1" "contains '$2'" "$(echo "$3" | head -c 100)"; fi; }
trim() { local var="$*"; var="${var#"${var%%[![:space:]]*}"}"; var="${var%"${var##*[![:space:]]}"}"; printf '%s' "$var"; }

echo "========================================"
echo "  粮库熏蒸作业审批 API 验证脚本 - 859"
echo "========================================"
echo ""

echo "[1/7] 检查服务..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/tickets")
check "服务正常运行" "200" "$HTTP_CODE"

echo "[0/7] 验证失败分支代码..."
if grep -q "人员未撤离不能投药" src/main/java/com/grain/fumigation/service/FumigationOperationService.java; then
    pass "代码包含：人员未撤离不能投药"
else
    fail "代码包含：人员未撤离不能投药" "存在" "不存在"
fi
echo ""

echo "[2/7] 创建作业票并订阅..."
CREATE=$(curl -s -X POST "$BASE_URL/ticket" -H "Content-Type: application/json" -d '{
    "warehouseCode": "WH001", "warehouseName": "1号仓", "grainType": "小麦", "grainQuantity": 100.5,
    "keeperId": "K001", "keeperName": "张三", "safetyOfficerId": "S001", "safetyOfficerName": "王安全员",
    "operationManagerId": "M001", "operationManagerName": "李经理"
}')
TICKET_ID=$(echo "$CREATE" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
TICKET_NO=$(echo "$CREATE" | grep -o '"ticketNo":"[^"]*"' | head -1 | cut -d'"' -f4)
check "创建成功" "200" "$(echo "$CREATE" | grep -o '"code":[0-9]*' | head -1 | cut -d: -f2)"
check "状态为草稿" "DRAFT" "$(echo "$CREATE" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)"

SUB=$(curl -s -X POST "$BASE_URL/subscribe" -H "Content-Type: application/json" -d "{
    \"operationId\": $TICKET_ID, \"subscriberRole\": \"SAFETY_OFFICER\",
    \"subscriberId\": \"S001\", \"subscriberName\": \"王安全员\"
}")
check "订阅成功" "订阅成功" "$(echo "$SUB" | grep -o '"message":"[^"]*"' | head -1 | cut -d'"' -f4)"
check_contains "订阅列表包含作业票" "$TICKET_NO" "$(curl -s "$BASE_URL/subscriptions/subscriber/S001")"
echo ""

echo "[3/7] 审批流程和状态事件..."
SUBMIT=$(curl -s -X POST "$BASE_URL/ticket/submit" -H "Content-Type: application/json" -d "{
    \"operationId\": $TICKET_ID, \"operatorId\": \"K001\", \"operatorName\": \"张三\"
}")
check "提交成功" "SUBMITTED" "$(echo "$SUBMIT" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)"

APPROVE=$(curl -s -X POST "$BASE_URL/ticket/approve" -H "Content-Type: application/json" -d "{
    \"operationId\": $TICKET_ID, \"approved\": true, \"operatorId\": \"M001\", \"operatorName\": \"李经理\", \"approveRemark\": \"同意\"
}")
check "审批通过" "APPROVED" "$(echo "$APPROVE" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)"

EVACUATION=$(curl -s -X POST "$BASE_URL/evacuation/confirm" -H "Content-Type: application/json" -d "{
    \"operationId\": $TICKET_ID, \"evacuationConfirmed\": true, \"operatorId\": \"S001\", \"operatorName\": \"王安全员\", \"evacuationRemark\": \"人员已撤离\"
}")
check "撤离确认成功" "EVACUATION_CONFIRMED" "$(echo "$EVACUATION" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)"

NOTIFY=$(curl -s "$BASE_URL/notifications")
check_contains "通知包含草稿" "DRAFT" "$NOTIFY"
check_contains "通知包含提交" "SUBMITTED" "$NOTIFY"
check_contains "通知包含审批" "APPROVED" "$NOTIFY"
check_contains "通知包含撤离" "EVACUATION_CONFIRMED" "$NOTIFY"
echo ""

echo "[4/7] 失败分支测试..."
TICKET_ID2=$(curl -s -X POST "$BASE_URL/ticket" -H "Content-Type: application/json" -d '{
    "warehouseCode": "WH002", "warehouseName": "2号仓", "grainType": "玉米", "grainQuantity": 80,
    "keeperId": "K002", "keeperName": "李四", "safetyOfficerId": "S002", "safetyOfficerName": "赵安全员",
    "operationManagerId": "M002", "operationManagerName": "王经理"
}' | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

curl -s -X POST "$BASE_URL/ticket/submit" -H "Content-Type: application/json" -d "{\"operationId\": $TICKET_ID2, \"operatorId\": \"K002\", \"operatorName\": \"李四\"}" > /dev/null
curl -s -X POST "$BASE_URL/ticket/approve" -H "Content-Type: application/json" -d "{\"operationId\": $TICKET_ID2, \"approved\": true, \"operatorId\": \"M002\", \"operatorName\": \"王经理\", \"approveRemark\": \"同意\"}" > /dev/null

EVAC_FAIL=$(curl -s -X POST "$BASE_URL/evacuation/confirm" -H "Content-Type: application/json" -d "{
    \"operationId\": $TICKET_ID2, \"evacuationConfirmed\": false, \"operatorId\": \"S002\", \"operatorName\": \"赵安全员\"
}")
check_contains "确认撤离失败返回正确消息" "人员未撤离" "$EVAC_FAIL"

PEST_FAIL=$(curl -s -X POST "$BASE_URL/pesticide/apply" -H "Content-Type: application/json" -d "{
    \"operationId\": $TICKET_ID2, \"pesticideType\": \"PH3\", \"pesticideDosage\": 20,
    \"operatorId\": \"M002\", \"operatorName\": \"王经理\"
}")
check_contains "未撤离投药失败" "不允许投药" "$PEST_FAIL"
check "失败状态码正确" "400" "$(echo "$PEST_FAIL" | grep -o '"code":[0-9]*' | head -1 | cut -d: -f2)"
echo ""

echo "[5/7] 待办、日志、查询状态一致..."
DETAIL=$(curl -s "$BASE_URL/ticket/$TICKET_ID")
DETAIL_STATUS=$(echo "$DETAIL" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)

TODO=$(curl -s "$BASE_URL/todos?operatorId=M001&role=OPERATION_MANAGER")

AUDIT=$(curl -s "$AUDIT_URL/logs/operation/$TICKET_ID")
AUDIT_STATUS=$(echo "$AUDIT" | grep -o '"afterStatus":"[^"]*"' | head -1 | cut -d'"' -f4)

check "查询状态" "EVACUATION_CONFIRMED" "$DETAIL_STATUS"
check "日志状态与查询一致" "$DETAIL_STATUS" "$AUDIT_STATUS"
check_contains "待办包含待投药" "待投药" "$TODO"
check_contains "待办状态与查询一致" "EVACUATION_CONFIRMED" "$TODO"
check_contains "订阅待办包含作业票" "$TICKET_NO" "$(curl -s "$BASE_URL/todos/subscribed/S001")"

curl -s -X DELETE "$BASE_URL/notifications" > /dev/null
echo ""

echo "[6/7] 完整投药通风流程..."
PEST=$(curl -s -X POST "$BASE_URL/pesticide/apply" -H "Content-Type: application/json" -d "{
    \"operationId\": $TICKET_ID, \"pesticideType\": \"PH3\", \"pesticideDosage\": 25,
    \"operatorId\": \"M001\", \"operatorName\": \"李经理\", \"pesticideRemark\": \"环流熏蒸\"
}")
check "投药成功" "PESTICIDE_APPLIED" "$(echo "$PEST" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)"

VENT=$(curl -s -X POST "$BASE_URL/ventilation/detect" -H "Content-Type: application/json" -d "{
    \"operationId\": $TICKET_ID, \"ventilationDurationHours\": 48, \"gasConcentration\": 0.3,
    \"ventilationPassed\": true, \"operatorId\": \"M001\", \"operatorName\": \"李经理\", \"ventilationRemark\": \"检测合格\"
}")
check "通风检测合格" "VENTILATION_COMPLETED" "$(echo "$VENT" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)"

LIFT=$(curl -s -X POST "$BASE_URL/alert/lift" -H "Content-Type: application/json" -d "{
    \"operationId\": $TICKET_ID, \"operatorId\": \"M001\", \"operatorName\": \"李经理\", \"alertLiftRemark\": \"解除警戒\"
}")
check "解除警戒成功" "ALERT_LIFTED" "$(echo "$LIFT" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)"
echo ""

echo "[7/7] 最终状态一致性..."
FINAL_DETAIL=$(curl -s "$BASE_URL/ticket/$TICKET_ID")
FINAL_STATUS=$(echo "$FINAL_DETAIL" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)

FINAL_AUDIT=$(curl -s "$AUDIT_URL/logs/operation/$TICKET_ID")
FINAL_AUDIT_STATUS=$(echo "$FINAL_AUDIT" | grep -o '"afterStatus":"[^"]*"' | head -1 | cut -d'"' -f4)

FINAL_NOTIFY=$(curl -s "$BASE_URL/notifications")

check "最终查询状态" "ALERT_LIFTED" "$FINAL_STATUS"
check "最终日志状态与查询一致" "$FINAL_STATUS" "$FINAL_AUDIT_STATUS"
check_contains "通知包含投药" "PESTICIDE_APPLIED" "$FINAL_NOTIFY"
check_contains "通知包含通风" "VENTILATION_COMPLETED" "$FINAL_NOTIFY"
check_contains "通知包含解除警戒" "ALERT_LIFTED" "$FINAL_NOTIFY"

FINAL_TODO=$(curl -s "$BASE_URL/todos?operatorId=M001&role=OPERATION_MANAGER")
if echo "$FINAL_TODO" | grep -q "\"operationId\": $TICKET_ID,"; then
    fail "流程完成后作业票不在待办中" "不包含 $TICKET_ID" "待办中仍包含 $TICKET_ID"
else
    pass "流程完成后作业票不在待办中"
fi

curl -s -X DELETE "$BASE_URL/notifications" > /dev/null
echo ""

echo "========================================"
echo "  验证结果"
echo "========================================"
echo "  总计: $TOTAL 项"
echo "  通过: $PASS 项"
echo "  失败: $FAIL 项"
echo "========================================"

if [ "$FAIL" -eq 0 ]; then
    echo ""
    echo "🎉 所有验证通过！"
    echo "   - 待办、日志、查询状态一致 ✓"
    echo "   - 失败分支：人员未撤离不能投药 ✓"
    echo "   - 状态订阅和通知正常 ✓"
    echo ""
    exit 0
else
    echo ""
    echo "❌ 有 $FAIL 项失败"
    echo ""
    exit 1
fi
