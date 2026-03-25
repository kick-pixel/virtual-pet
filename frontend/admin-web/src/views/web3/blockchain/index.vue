<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
         <el-form-item :label="$t('web3.blockchain.networkName')" prop="networkName">
            <el-input
               v-model="queryParams.networkName"
               :placeholder="$t('web3.blockchain.networkNamePlaceholder')"
               clearable
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item :label="$t('web3.blockchain.networkType')" prop="networkType">
            <el-select v-model="queryParams.networkType" :placeholder="$t('web3.blockchain.networkTypePlaceholder')" clearable>
               <el-option label="Ethereum" value="ethereum" />
               <el-option label="Ethereum Testnet" value="ethereum_testnet" />
               <el-option label="BSC" value="bsc" />
               <el-option label="BSC Testnet" value="bsc_testnet" />
               <el-option label="Polygon" value="polygon" />
            </el-select>
         </el-form-item>
         <el-form-item :label="$t('common.status')" prop="status">
            <el-select v-model="queryParams.status" :placeholder="$t('common.pleaseSelect')" clearable>
               <el-option :label="$t('common.normal')" value="0" />
               <el-option :label="$t('common.disable')" value="1" />
            </el-select>
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
               icon="Plus"
               @click="handleAdd"
               v-hasPermi="['web3:blockchain:add']"
            >{{ $t('button.add') }}</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['web3:blockchain:edit']"
            >{{ $t('button.edit') }}</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['web3:blockchain:remove']"
            >{{ $t('button.remove') }}</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="blockchainList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column :label="$t('web3.blockchain.configId')" align="center" prop="configId" width="80" />
         <el-table-column :label="$t('web3.blockchain.networkName')" align="center" prop="networkName" :show-overflow-tooltip="true" />
         <el-table-column :label="$t('web3.blockchain.networkType')" align="center" prop="networkType" width="140">
            <template #default="scope">
               <el-tag :type="getNetworkTypeTag(scope.row.networkType)">{{ getNetworkTypeLabel(scope.row.networkType) }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.blockchain.chainId')" align="center" prop="chainId" width="100" />
         <el-table-column :label="$t('web3.blockchain.rpcUrl')" align="center" prop="rpcUrl" :show-overflow-tooltip="true" />
         <el-table-column :label="$t('web3.blockchain.walletAddress')" align="center" prop="walletAddress" width="180">
            <template #default="scope">
               <el-tooltip :content="scope.row.walletAddress" placement="top">
                  <span class="address-text">{{ formatAddress(scope.row.walletAddress) }}</span>
               </el-tooltip>
               <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.walletAddress)">
                  <el-icon><CopyDocument /></el-icon>
               </el-button>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.blockchain.scanStartBlock')" align="center" prop="scanStartBlock" width="120" />
         <el-table-column :label="$t('common.status')" align="center" prop="status" width="100">
            <template #default="scope">
               <el-switch
                  v-model="scope.row.status"
                  active-value="0"
                  inactive-value="1"
                  @change="handleStatusChange(scope.row)"
               />
            </template>
         </el-table-column>
         <el-table-column :label="$t('common.operation')" align="center" width="200" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Connection" @click="handleTestConnection(scope.row)">{{ $t('web3.blockchain.testConnection') }}</el-button>
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['web3:blockchain:edit']">{{ $t('button.edit') }}</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['web3:blockchain:remove']">{{ $t('button.remove') }}</el-button>
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

      <!-- 添加或修改区块链网络对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="blockchainRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item :label="$t('web3.blockchain.networkName')" prop="networkName">
               <el-input v-model="form.networkName" :placeholder="$t('web3.blockchain.networkNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('web3.blockchain.networkType')" prop="networkType">
               <el-select v-model="form.networkType" :placeholder="$t('web3.blockchain.networkTypePlaceholder')" style="width: 100%">
                  <el-option label="Ethereum Mainnet" value="ethereum" />
                  <el-option label="Ethereum Testnet (Sepolia)" value="ethereum_testnet" />
                  <el-option label="BSC Mainnet" value="bsc" />
                  <el-option label="BSC Testnet" value="bsc_testnet" />
                  <el-option label="Polygon Mainnet" value="polygon" />
                  <el-option label="Polygon Testnet (Mumbai)" value="polygon_testnet" />
               </el-select>
            </el-form-item>
            <el-form-item :label="$t('web3.blockchain.chainId')" prop="chainId">
               <el-input-number v-model="form.chainId" :min="1" :placeholder="$t('web3.blockchain.chainIdPlaceholder')" style="width: 100%" />
            </el-form-item>
            <el-form-item :label="$t('web3.blockchain.rpcUrl')" prop="rpcUrl">
               <el-input v-model="form.rpcUrl" :placeholder="$t('web3.blockchain.rpcUrlPlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('web3.blockchain.explorerUrl')" prop="explorerUrl">
               <el-input v-model="form.explorerUrl" :placeholder="$t('web3.blockchain.explorerUrlPlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('web3.blockchain.walletAddress')" prop="walletAddress">
               <el-input v-model="form.walletAddress" :placeholder="$t('web3.blockchain.walletAddressPlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('web3.blockchain.scanStartBlock')" prop="scanStartBlock">
               <el-input-number v-model="form.scanStartBlock" :min="0" :placeholder="$t('web3.blockchain.scanStartBlockPlaceholder')" style="width: 100%" />
            </el-form-item>
            <el-form-item :label="$t('common.status')" prop="status">
               <el-radio-group v-model="form.status">
                  <el-radio value="0">{{ $t('common.normal') }}</el-radio>
                  <el-radio value="1">{{ $t('common.disable') }}</el-radio>
               </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('common.remarks')" prop="remark">
               <el-input v-model="form.remark" type="textarea" :placeholder="$t('common.pleaseInput')" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">{{ $t('button.submit') }}</el-button>
               <el-button @click="cancel">{{ $t('button.cancel') }}</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Blockchain">
import { listBlockchain, getBlockchain, delBlockchain, addBlockchain, updateBlockchain, testRpcConnection } from "@/api/web3/blockchain"
import { useI18n } from 'vue-i18n'
import { CopyDocument } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { t } = useI18n()

const blockchainList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    networkName: undefined,
    networkType: undefined,
    status: undefined
  },
  rules: {
    networkName: [{ required: true, message: computed(() => t('web3.blockchain.networkNameRequired')), trigger: "blur" }],
    networkType: [{ required: true, message: computed(() => t('web3.blockchain.networkTypeRequired')), trigger: "change" }],
    chainId: [{ required: true, message: computed(() => t('web3.blockchain.chainIdRequired')), trigger: "blur" }],
    rpcUrl: [{ required: true, message: computed(() => t('web3.blockchain.rpcUrlRequired')), trigger: "blur" }],
    walletAddress: [{ required: true, message: computed(() => t('web3.blockchain.walletAddressRequired')), trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 获取网络类型标签颜色 */
function getNetworkTypeTag(type) {
  const typeMap = {
    'ethereum': '',
    'ethereum_testnet': 'warning',
    'bsc': 'success',
    'bsc_testnet': 'warning',
    'polygon': 'danger',
    'polygon_testnet': 'warning'
  }
  return typeMap[type] || 'info'
}

/** 获取网络类型显示文本 */
function getNetworkTypeLabel(type) {
  const labelMap = {
    'ethereum': 'Ethereum',
    'ethereum_testnet': 'ETH Testnet',
    'bsc': 'BSC',
    'bsc_testnet': 'BSC Testnet',
    'polygon': 'Polygon',
    'polygon_testnet': 'Polygon Testnet'
  }
  return labelMap[type] || type
}

/** 格式化地址显示 */
function formatAddress(address) {
  if (!address) return ''
  if (address.length <= 12) return address
  return address.substring(0, 6) + '...' + address.substring(address.length - 4)
}

/** 复制到剪贴板 */
function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(() => {
    proxy.$modal.msgSuccess(t('web3.transaction.copySuccess'))
  })
}

/** 查询区块链网络列表 */
function getList() {
  loading.value = true
  listBlockchain(queryParams.value).then(response => {
    blockchainList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    configId: undefined,
    networkName: undefined,
    networkType: undefined,
    chainId: undefined,
    rpcUrl: undefined,
    explorerUrl: undefined,
    walletAddress: undefined,
    scanStartBlock: 0,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("blockchainRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = t('web3.blockchain.addBlockchain')
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const configId = row.configId || ids.value
  getBlockchain(configId).then(response => {
    form.value = response.data
    open.value = true
    title.value = t('web3.blockchain.editBlockchain')
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["blockchainRef"].validate(valid => {
    if (valid) {
      if (form.value.configId != undefined) {
        updateBlockchain(form.value).then(response => {
          proxy.$modal.msgSuccess(t('message.updateSuccess'))
          open.value = false
          getList()
        })
      } else {
        addBlockchain(form.value).then(response => {
          proxy.$modal.msgSuccess(t('message.addSuccess'))
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const configIds = row.configId || ids.value
  const networkName = row.networkName || ''
  proxy.$modal.confirm(t('web3.blockchain.confirmDelete', { name: networkName })).then(function () {
    return delBlockchain(configIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess(t('message.deleteSuccess'))
  }).catch(() => {})
}

/** 状态变更 */
function handleStatusChange(row) {
  let text = row.status === "0" ? t('common.normal') : t('common.disable')
  updateBlockchain(row).then(() => {
    proxy.$modal.msgSuccess(text + t('common.success'))
  }).catch(() => {
    row.status = row.status === "0" ? "1" : "0"
  })
}

/** 测试RPC连接 */
function handleTestConnection(row) {
  testRpcConnection(row.configId).then(response => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess(t('web3.blockchain.connectionSuccess') + ': ' + t('web3.blockchain.currentBlock') + ' ' + response.data)
    } else {
      proxy.$modal.msgError(t('web3.blockchain.connectionFailed'))
    }
  }).catch(() => {
    proxy.$modal.msgError(t('web3.blockchain.connectionFailed'))
  })
}

getList()
</script>

<style scoped>
.address-text {
  font-family: monospace;
  font-size: 12px;
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

/* 响应式调整 */
@media screen and (max-width: 1366px) {
  :deep(.el-form--inline .el-form-item__content) {
    min-width: 180px;
  }

  :deep(.el-form--inline .el-input),
  :deep(.el-form--inline .el-select) {
    width: 180px;
  }
}
</style>
