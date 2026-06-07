#!/bin/bash

echo "=========================================="
echo "粮库熏蒸作业审批API - 超药剂用量审批失败验证"
echo "=========================================="
echo ""

BASE_URL="http://localhost:18080"
MAX_DOSAGE=50

echo "当前最大药剂用量限值: ${MAX_DOSAGE}"
echo ""

# Step 1: 创建作业票
echo "步骤1: 创建作业票（保管员提交仓房和粮食品种）"
CREATE_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/fumigation/ticket \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseCode": "WH001",
    "warehouseName": "1号仓",
    "grainType": "小麦",
    "grainQuantity": 1000.00,
    "keeperId": "K001",
    "keeperName": "张三",
    "remark": "2026年夏粮熏蒸作业"
  }')
echo "响应: ${CREATE_RESPONSE}"
TICKET_ID=$(echo ${CREATE_RESPONSE} | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
TICKET_NO=$(echo ${CREATE_RESPONSE} | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['ticketNo'])")
echo "作业票ID: ${TICKET_ID}, 作业票号: ${TICKET_NO}"
echo ""
sleep 1

# Step 2: 提交审批
echo "步骤2: 提交作业票审批"
SUBMIT_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/fumigation/ticket/submit \
  -H "Content-Type: application/json" \
  -d "{\"operationId\": ${TICKET_ID}, \"operatorId\": \"K001\", \"operatorName\": \"张三\"}")
echo "响应: ${SUBMIT_RESPONSE}"
echo ""
sleep 1

# Step 3: 审批通过
echo "步骤3: 作业负责人审批通过"
APPROVE_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/fumigation/ticket/approve \
  -H "Content-Type: application/json" \
  -d "{\"operationId\": ${TICKET_ID}, \"approved\": true, \"approveRemark\": \"同意开展熏蒸作业，请严格遵守操作规程\", \"operatorId\": \"M001\", \"operatorName\": \"李经理\", \"operationManagerId\": \"M001\", \"operationManagerName\": \"李经理\"}")
echo "响应: ${APPROVE_RESPONSE}"
echo ""
sleep 1

# Step 4: 确认人员撤离
echo "步骤4: 安全员核查人员撤离"
EVAC_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/fumigation/evacuation/confirm \
  -H "Content-Type: application/json" \
  -d "{\"operationId\": ${TICKET_ID}, \"evacuationConfirmed\": true, \"evacuationRemark\": \"仓内人员已全部撤离，周边已设置警戒线\", \"operatorId\": \"S001\", \"operatorName\": \"王安全员\"}")
echo "响应: ${EVAC_RESPONSE}"
echo ""
sleep 1

# Step 5: 正常剂量投药 - 应该成功
echo "步骤5: 正常剂量投药 - 期望值: 成功"
NORMAL_DOSAGE=30
echo "使用药剂用量: ${NORMAL_DOSAGE} (限值: ${MAX_DOSAGE})"
PESTICIDE_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/fumigation/pesticide/apply \
  -H "Content-Type: application/json" \
  -d "{\"operationId\": ${TICKET_ID}, \"pesticideType\": \"磷化铝\", \"pesticideDosage\": ${NORMAL_DOSAGE}, \"pesticideRemark\": \"按标准剂量投药\", \"operatorId\": \"M001\", \"operatorName\": \"李经理\"}")
PESTICIDE_CODE=$(echo ${PESTICIDE_RESPONSE} | python3 -c "import sys,json; print(json.load(sys.stdin)['code'])")
echo "响应: ${PESTICIDE_RESPONSE}"
if [ "${PESTICIDE_CODE}" = "200" ]; then
  echo "✅ 正常剂量投药成功 - 验证通过"
else
  echo "❌ 正常剂量投药失败 - 验证不通过"
fi
echo ""
sleep 1

# Step 6: 超剂量投药 - 应该失败 (错误码406)
echo "=========================================="
echo "步骤6: 超剂量投药验证 - 期望值: 失败 (错误码406)"
echo "=========================================="
OVER_DOSAGE=60
echo "使用药剂用量: ${OVER_DOSAGE} (限值: ${MAX_DOSAGE})"
echo ""

# Step 6.1: 创建新作业票
echo "步骤6.1: 创建新作业票用于超剂量测试"
CREATE2_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/fumigation/ticket \
  -H "Content-Type: application/json" \
  -d '{"warehouseCode": "WH002", "warehouseName": "2号仓", "grainType": "玉米", "grainQuantity": 800.00, "keeperId": "K002", "keeperName": "李四", "remark": "2026年夏粮熏蒸作业 - 超剂量测试"}')
TICKET2_ID=$(echo ${CREATE2_RESPONSE} | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
echo "新作业票ID: ${TICKET2_ID}"
echo ""
sleep 1

# Step 6.2: 提交审批
echo "步骤6.2: 提交作业票审批"
curl -s -X POST ${BASE_URL}/api/fumigation/ticket/submit \
  -H "Content-Type: application/json" \
  -d "{\"operationId\": ${TICKET2_ID}, \"operatorId\": \"K002\", \"operatorName\": \"李四\"}" > /dev/null
sleep 1

# Step 6.3: 审批通过
echo "步骤6.3: 作业负责人审批通过"
curl -s -X POST ${BASE_URL}/api/fumigation/ticket/approve \
  -H "Content-Type: application/json" \
  -d "{\"operationId\": ${TICKET2_ID}, \"approved\": true, \"approveRemark\": \"同意开展\", \"operatorId\": \"M001\", \"operatorName\": \"李经理\", \"operationManagerId\": \"M001\", \"operationManagerName\": \"李经理\"}" > /dev/null
sleep 1

# Step 6.4: 确认人员撤离
echo "步骤6.4: 安全员核查人员撤离"
curl -s -X POST ${BASE_URL}/api/fumigation/evacuation/confirm \
  -H "Content-Type: application/json" \
  -d "{\"operationId\": ${TICKET2_ID}, \"evacuationConfirmed\": true, \"evacuationRemark\": \"人员已撤离\", \"operatorId\": \"S001\", \"operatorName\": \"王安全员\"}" > /dev/null
sleep 1

# Step 6.5: 超剂量投药 - 核心验证
echo "步骤6.5: 超剂量投药验证"
echo "投药剂量: ${OVER_DOSAGE}, 最大限值: ${MAX_DOSAGE}"
echo "期望结果: 被驳回，返回错误码406"
echo ""
OVER_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/fumigation/pesticide/apply \
  -H "Content-Type: application/json" \
  -d "{\"operationId\": ${TICKET2_ID}, \"pesticideType\": \"磷化铝\", \"pesticideDosage\": ${OVER_DOSAGE}, \"pesticideRemark\": \"超剂量投药测试\", \"operatorId\": \"M001\", \"operatorName\": \"李经理\"}")
echo "实际响应: ${OVER_RESPONSE}"
echo ""

OVER_CODE=$(echo ${OVER_RESPONSE} | python3 -c "import sys,json; data=json.load(sys.stdin); print(data.get('code', 'N/A'))")
OVER_MSG=$(echo ${OVER_RESPONSE} | python3 -c "import sys,json; data=json.load(sys.stdin); print(data.get('message', 'N/A'))")

echo "响应代码: ${OVER_CODE}"
echo "响应消息: ${OVER_MSG}"
echo ""

if [ "${OVER_CODE}" = "406" ]; then
  echo "✅ 超药剂用量审批失败验证通过！"
  echo "   错误码406正确返回，药剂用量[${OVER_DOSAGE}]超过最大限值[${MAX_DOSAGE}]被正确驳回"
else
  echo "❌ 超药剂用量审批失败验证不通过！"
  echo "   期望错误码406，实际返回${OVER_CODE}"
fi
echo ""

# 验证超剂量投药后状态保持不变
echo "验证超剂量投药后作业状态保持不变:"
TICKET_STATUS=$(curl -s ${BASE_URL}/api/fumigation/ticket/${TICKET2_ID} | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['status'])")
echo "作业票状态: ${TICKET_STATUS}"
if [ "${TICKET_STATUS}" = "EVACUATION_CONFIRMED" ]; then
  echo "✅ 状态保持EVACUATION_CONFIRMED不变 - 验证通过"
else
  echo "❌ 状态不正确 - 验证不通过"
fi
echo ""

# 查询审计日志
echo "=========================================="
echo "查询审计记录:"
echo "=========================================="
curl -s ${BASE_URL}/api/audit/logs/operation/${TICKET2_ID} | python3 -m json.tool
echo ""

echo "=========================================="
echo "验证完成"
echo "=========================================="
