<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
         <el-form-item :label="$t('web3.transaction.networkName')" prop="networkType">
            <el-select v-model="queryParams.networkType" :placeholder="$t('web3.transaction.networkPlaceholder')" clearable>
               <el-option
                  v-for="item in networkList"
                  :key="item.networkType"
                  :label="item.networkName"
                  :value="item.networkType"
               />
            </el-select>
         </el-form-item>
         <el-form-item :label="$t('web3.transaction.txHash')" prop="txHash">
            <el-input
               v-model="queryParams.txHash"
               :placeholder="$t('web3.transaction.txHashPlaceholder')"
               clearable
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item :label="$t('web3.transaction.fromAddress')" prop="fromAddress">
            <el-input
               v-model="queryParams.fromAddress"
               :placeholder="$t('web3.transaction.fromAddressPlaceholder')"
               clearable
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item :label="$t('web3.transaction.status')" prop="status">
            <el-select v-model="queryParams.status" :placeholder="$t('web3.transaction.statusPlaceholder')" clearable>
               <el-option :label="$t('web3.transaction.statusPending')" value="pending" />
               <el-option :label="$t('web3.transaction.statusConfirmed')" value="confirmed" />
               <el-option :label="$t('web3.transaction.statusFailed')" value="failed" />
            </el-select>
         </el-form-item>
         <el-form-item :label="$t('common.createTime')">
            <el-date-picker
               v-model="dateRange"
               value-format="YYYY-MM-DD"
               type="daterange"
               range-separator="-"
               :start-placeholder="$t('common.startDate')"
               :end-placeholder="$t('common.endDate')"
            ></el-date-picker>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('button.query') }}</el-button>
            <el-button icon="Refresh" @click="resetQuery">{{ $t('button.reset') }}</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Refresh"
               @click="handleScan"
               v-hasPermi="['web3:transaction:scan']"
            >{{ $t('web3.transaction.scanNow') }}</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['web3:transaction:export']"
            >{{ $t('button.export') }}</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="transactionList">
         <el-table-column :label="$t('web3.transaction.transactionId')" align="center" prop="txId" width="80" />
         <el-table-column :label="$t('web3.transaction.networkName')" align="center" prop="networkName" width="120">
            <template #default="scope">
               <el-tag size="small">{{ scope.row.networkName || getNetworkName(scope.row.networkType) }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.transaction.txHash')" align="center" prop="txHash" width="180">
            <template #default="scope">
               <el-tooltip :content="scope.row.txHash" placement="top">
                  <span class="hash-text">{{ formatHash(scope.row.txHash) }}</span>
               </el-tooltip>
               <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.txHash)">
                  <el-icon><CopyDocument /></el-icon>
               </el-button>
               <el-button link type="primary" size="small" @click="viewOnExplorer(scope.row)">
                  <el-icon><Link /></el-icon>
               </el-button>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.transaction.blockNumber')" align="center" prop="blockNumber" width="100" />
         <el-table-column :label="$t('web3.transaction.fromAddress')" align="center" prop="fromAddress" width="160">
            <template #default="scope">
               <el-tooltip :content="scope.row.fromAddress" placement="top">
                  <span class="address-text">{{ formatAddress(scope.row.fromAddress) }}</span>
               </el-tooltip>
               <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.fromAddress)">
                  <el-icon><CopyDocument /></el-icon>
               </el-button>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.transaction.tokenSymbol')" align="center" prop="tokenSymbol" width="100">
            <template #default="scope">
               <strong>{{ scope.row.tokenSymbol }}</strong>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.transaction.amount')" align="right" prop="amountDisplay" width="140">
            <template #default="scope">
               <span class="amount-text">{{ scope.row.amountDisplay || formatAmount(scope.row.amount, scope.row.decimals) }}</span>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.transaction.status')" align="center" prop="status" width="100">
            <template #default="scope">
               <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusLabel(scope.row.status) }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.transaction.confirmations')" align="center" prop="confirmations" width="80" />
         <el-table-column :label="$t('web3.transaction.transactionTime')" align="center" prop="transactionTime" width="160">
            <template #default="scope">
               <span>{{ parseTime(scope.row.transactionTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column :label="$t('common.createTime')" align="center" prop="createTime" width="160">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
      </el-table>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />

      <!-- 交易详情对话框 -->
      <el-dialog :title="$t('common.detail')" v-model="detailOpen" width="700px" append-to-body>
         <el-descriptions :column="2" border>
            <el-descriptions-item :label="$t('web3.transaction.transactionId')">{{ detailData.txId }}</el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.networkName')">{{ detailData.networkName }}</el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.txHash')" :span="2">
               <span class="hash-text-full">{{ detailData.txHash }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.blockNumber')">{{ detailData.blockNumber }}</el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.confirmations')">{{ detailData.confirmations }}</el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.fromAddress')" :span="2">
               <span class="address-text-full">{{ detailData.fromAddress }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.toAddress')" :span="2">
               <span class="address-text-full">{{ detailData.toAddress }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.tokenSymbol')">{{ detailData.tokenSymbol }}</el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.amount')">{{ detailData.amountDisplay }}</el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.status')">
               <el-tag :type="getStatusType(detailData.status)">{{ getStatusLabel(detailData.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('web3.transaction.transactionTime')">{{ parseTime(detailData.transactionTime) }}</el-descriptions-item>
         </el-descriptions>
         <template #footer>
            <div class="dialog-footer">
               <el-button @click="detailOpen = false">{{ $t('button.close') }}</el-button>
               <el-button type="primary" @click="viewOnExplorer(detailData)">{{ $t('web3.transaction.viewOnExplorer') }}</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Transaction">
import { listTransaction, getTransaction, triggerScan, exportTransaction } from "@/api/web3/transaction"
import { listBlockchain } from "@/api/web3/blockchain"
import { useI18n } from 'vue-i18n'
import { CopyDocument, Link } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { t } = useI18n()

const transactionList = ref([])
const networkList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])
const detailOpen = ref(false)
const detailData = ref({})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    networkType: undefined,
    txHash: undefined,
    fromAddress: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

/** 格式化交易哈希显示 */
function formatHash(hash) {
  if (!hash) return ''
  if (hash.length <= 16) return hash
  return hash.substring(0, 10) + '...' + hash.substring(hash.length - 6)
}

/** 格式化地址显示 */
function formatAddress(address) {
  if (!address) return ''
  if (address.length <= 12) return address
  return address.substring(0, 6) + '...' + address.substring(address.length - 4)
}

/** 格式化金额 */
function formatAmount(amount, decimals) {
  if (!amount) return '0'
  const dec = decimals || 18
  const value = parseFloat(amount) / Math.pow(10, dec)
  return value.toFixed(6)
}

/** 复制到剪贴板 */
function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(() => {
    proxy.$modal.msgSuccess(t('web3.transaction.copySuccess'))
  })
}

/** 根据网络类型获取网络名称 */
function getNetworkName(networkType) {
  const network = networkList.value.find(item => item.networkType === networkType)
  return network ? network.networkName : networkType
}

/** 根据网络类型获取区块浏览器URL */
function getExplorerUrl(networkType) {
  const network = networkList.value.find(item => item.networkType === networkType)
  return network ? network.explorerUrl : null
}

/** 在区块浏览器查看 */
function viewOnExplorer(row) {
  const explorerUrl = row.explorerUrl || getExplorerUrl(row.networkType)
  if (explorerUrl) {
    const url = explorerUrl.endsWith('/') ? explorerUrl : explorerUrl + '/'
    window.open(url + 'tx/' + row.txHash, '_blank')
  } else {
    proxy.$modal.msgWarning(t('web3.transaction.noExplorer'))
  }
}

/** 获取状态类型 */
function getStatusType(status) {
  const typeMap = {
    'pending': 'warning',
    'confirmed': 'success',
    'failed': 'danger'
  }
  return typeMap[status] || 'info'
}

/** 获取状态标签 */
function getStatusLabel(status) {
  const labelMap = {
    'pending': t('web3.transaction.statusPending'),
    'confirmed': t('web3.transaction.statusConfirmed'),
    'failed': t('web3.transaction.statusFailed')
  }
  return labelMap[status] || status
}

/** 获取网络列表 */
function getNetworks() {
  listBlockchain({ pageNum: 1, pageSize: 100 }).then(response => {
    networkList.value = response.rows
  })
}

/** 查询交易记录列表 */
function getList() {
  loading.value = true
  listTransaction(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    transactionList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 手动触发扫描 */
function handleScan() {
  triggerScan().then(response => {
    proxy.$modal.msgSuccess(t('web3.transaction.scanTriggered'))
    // 延迟刷新列表
    setTimeout(() => {
      getList()
    }, 2000)
  })
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("web3/transaction/export", {
    ...queryParams.value
  }, `transactions_${new Date().getTime()}.xlsx`)
}

/** 查看详情 */
function handleDetail(row) {
  getTransaction(row.txId).then(response => {
    detailData.value = response.data
    detailOpen.value = true
  })
}

getNetworks()
getList()
</script>

<style scoped>
.hash-text, .address-text {
  font-family: monospace;
  font-size: 12px;
}
.hash-text-full, .address-text-full {
  font-family: monospace;
  font-size: 12px;
  word-break: break-all;
}
.amount-text {
  font-family: monospace;
  font-weight: bold;
  color: #67C23A;
}

/* 搜索表单响应式布局 */
:deep(.el-form--inline .el-form-item) {
  margin-right: 10px;
}

:deep(.el-form--inline .el-form-item__content) {
  min-width: 200px;
}

:deep(.el-form--inline .el-input),
:deep(.el-form--inline .el-select) {
  width: 200px;
}

/* 日期范围选择器需要更宽 */
:deep(.el-form--inline .el-date-editor.el-input__wrapper) {
  width: 260px;
}

/* 响应式调整 */
@media screen and (max-width: 1366px) {
  :deep(.el-form--inline .el-form-item__content) {
    min-width: 180px;
  }

  :deep(.el-form--inline .el-input),
  :deep(.el-form--inline .el-select) {
    width: 180px;
  }

  :deep(.el-form--inline .el-date-editor.el-input__wrapper) {
    width: 240px;
  }
}
</style>
