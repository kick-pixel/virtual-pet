<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
         <el-form-item :label="$t('web3.token.networkId')" prop="networkType">
            <el-select v-model="queryParams.networkType" :placeholder="$t('web3.token.networkPlaceholder')" clearable>
               <el-option
                  v-for="item in networkList"
                  :key="item.networkType"
                  :label="item.networkName"
                  :value="item.networkType"
               />
            </el-select>
         </el-form-item>
         <el-form-item :label="$t('web3.token.tokenSymbol')" prop="tokenSymbol">
            <el-input
               v-model="queryParams.tokenSymbol"
               :placeholder="$t('web3.token.tokenSymbolPlaceholder')"
               clearable
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item :label="$t('web3.token.isNative')" prop="isNative">
            <el-select v-model="queryParams.isNative" :placeholder="$t('common.pleaseSelect')" clearable>
               <el-option :label="$t('web3.token.nativeToken')" value="1" />
               <el-option :label="$t('web3.token.erc20Token')" value="0" />
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
               v-hasPermi="['web3:token:add']"
            >{{ $t('button.add') }}</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['web3:token:edit']"
            >{{ $t('button.edit') }}</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['web3:token:remove']"
            >{{ $t('button.remove') }}</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="tokenList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column :label="$t('web3.token.tokenId')" align="center" prop="tokenId" width="80" />
         <el-table-column :label="$t('web3.token.networkId')" align="center" prop="networkName" width="140">
            <template #default="scope">
               <el-tag>{{ scope.row.networkName || getNetworkName(scope.row.networkType) }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.token.tokenSymbol')" align="center" prop="tokenSymbol" width="100">
            <template #default="scope">
               <strong>{{ scope.row.tokenSymbol }}</strong>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.token.tokenName')" align="center" prop="tokenName" :show-overflow-tooltip="true" />
         <el-table-column :label="$t('web3.token.contractAddress')" align="center" prop="contractAddress" width="200">
            <template #default="scope">
               <template v-if="scope.row.isNative === '1'">
                  <el-tag type="success">{{ $t('web3.token.nativeToken') }}</el-tag>
               </template>
               <template v-else>
                  <el-tooltip :content="scope.row.contractAddress" placement="top">
                     <span class="address-text">{{ formatAddress(scope.row.contractAddress) }}</span>
                  </el-tooltip>
                  <el-button link type="primary" size="small" @click="copyToClipboard(scope.row.contractAddress)">
                     <el-icon><CopyDocument /></el-icon>
                  </el-button>
               </template>
            </template>
         </el-table-column>
         <el-table-column :label="$t('web3.token.decimals')" align="center" prop="decimals" width="80" />
         <el-table-column :label="$t('web3.token.isNative')" align="center" prop="isNative" width="100">
            <template #default="scope">
               <el-tag :type="scope.row.isNative === '1' ? 'success' : 'info'">
                  {{ scope.row.isNative === '1' ? $t('common.yes') : $t('common.no') }}
               </el-tag>
            </template>
         </el-table-column>
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
         <el-table-column :label="$t('common.operation')" align="center" width="150" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['web3:token:edit']">{{ $t('button.edit') }}</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['web3:token:remove']">{{ $t('button.remove') }}</el-button>
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

      <!-- 添加或修改代币配置对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="tokenRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item :label="$t('web3.token.networkId')" prop="networkType">
               <el-select v-model="form.networkType" :placeholder="$t('web3.token.networkPlaceholder')" style="width: 100%">
                  <el-option
                     v-for="item in enabledNetworkList"
                     :key="item.networkType"
                     :label="item.networkName"
                     :value="item.networkType"
                  />
               </el-select>
            </el-form-item>
            <el-form-item :label="$t('web3.token.tokenSymbol')" prop="tokenSymbol">
               <el-input v-model="form.tokenSymbol" :placeholder="$t('web3.token.tokenSymbolPlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('web3.token.tokenName')" prop="tokenName">
               <el-input v-model="form.tokenName" :placeholder="$t('web3.token.tokenNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('web3.token.isNative')" prop="isNative">
               <el-radio-group v-model="form.isNative" @change="handleNativeChange">
                  <el-radio value="1">{{ $t('web3.token.nativeToken') }}</el-radio>
                  <el-radio value="0">{{ $t('web3.token.erc20Token') }}</el-radio>
               </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('web3.token.contractAddress')" prop="contractAddress" v-show="form.isNative !== '1'">
               <el-input v-model="form.contractAddress" :placeholder="$t('web3.token.contractAddressPlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('web3.token.decimals')" prop="decimals">
               <el-input-number v-model="form.decimals" :min="0" :max="36" :placeholder="$t('web3.token.decimalsPlaceholder')" style="width: 100%" />
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

<script setup name="Token">
import { listToken, getToken, delToken, addToken, updateToken } from "@/api/web3/token"
import { listBlockchain } from "@/api/web3/blockchain"
import { useI18n } from 'vue-i18n'
import { CopyDocument } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { t } = useI18n()

const tokenList = ref([])
const networkList = ref([])
const enabledNetworkList = ref([])
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
    networkType: undefined,
    tokenSymbol: undefined,
    isNative: undefined,
    status: undefined
  },
  rules: {
    networkType: [{ required: true, message: computed(() => t('web3.token.networkRequired')), trigger: "change" }],
    tokenSymbol: [{ required: true, message: computed(() => t('web3.token.tokenSymbolRequired')), trigger: "blur" }],
    tokenName: [{ required: true, message: computed(() => t('web3.token.tokenNameRequired')), trigger: "blur" }],
    decimals: [{ required: true, message: computed(() => t('web3.token.decimalsRequired')), trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

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

/** 根据网络类型获取网络名称 */
function getNetworkName(networkType) {
  const network = networkList.value.find(item => item.networkType === networkType)
  return network ? network.networkName : networkType
}

/** 获取网络列表 */
function getNetworks() {
  listBlockchain({ pageNum: 1, pageSize: 100 }).then(response => {
    networkList.value = response.rows
    // 过滤出启用的网络用于新增/编辑时选择
    enabledNetworkList.value = response.rows.filter(item => item.status === '0')
  })
}

/** 查询代币列表 */
function getList() {
  loading.value = true
  listToken(queryParams.value).then(response => {
    tokenList.value = response.rows
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
    tokenId: undefined,
    networkType: undefined,
    tokenSymbol: undefined,
    tokenName: undefined,
    contractAddress: undefined,
    decimals: 18,
    isNative: "0",
    status: "0",
    remark: undefined
  }
  proxy.resetForm("tokenRef")
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
  ids.value = selection.map(item => item.tokenId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = t('web3.token.addToken')
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const tokenId = row.tokenId || ids.value
  getToken(tokenId).then(response => {
    form.value = response.data
    open.value = true
    title.value = t('web3.token.editToken')
  })
}

/** 原生代币切换 */
function handleNativeChange(val) {
  if (val === '1') {
    form.value.contractAddress = undefined
  }
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["tokenRef"].validate(valid => {
    if (valid) {
      // 如果是非原生代币，验证合约地址
      if (form.value.isNative !== '1' && !form.value.contractAddress) {
        proxy.$modal.msgError(t('web3.token.contractAddressRequired'))
        return
      }

      if (form.value.tokenId != undefined) {
        updateToken(form.value).then(response => {
          proxy.$modal.msgSuccess(t('message.updateSuccess'))
          open.value = false
          getList()
        })
      } else {
        addToken(form.value).then(response => {
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
  const tokenIds = row.tokenId || ids.value
  const tokenSymbol = row.tokenSymbol || ''
  proxy.$modal.confirm(t('web3.token.confirmDelete', { symbol: tokenSymbol })).then(function () {
    return delToken(tokenIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess(t('message.deleteSuccess'))
  }).catch(() => {})
}

/** 状态变更 */
function handleStatusChange(row) {
  let text = row.status === "0" ? t('common.normal') : t('common.disable')
  updateToken(row).then(() => {
    proxy.$modal.msgSuccess(text + t('common.success'))
  }).catch(() => {
    row.status = row.status === "0" ? "1" : "0"
  })
}

getNetworks()
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
