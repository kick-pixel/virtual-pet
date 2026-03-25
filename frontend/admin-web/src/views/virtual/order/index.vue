<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
      <el-form-item :label="$t('virtual.order.orderNo')" prop="orderNo">
        <el-input v-model="queryParams.orderNo" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleQuery" style="width:180px" />
      </el-form-item>
      <el-form-item :label="$t('virtual.order.userId')" prop="userId">
        <el-input v-model="queryParams.userId" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleQuery" style="width:140px" />
      </el-form-item>
      <el-form-item :label="$t('virtual.order.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="$t('common.pleaseSelect')" clearable style="width:120px">
          <el-option :label="$t('virtual.order.statusPending')" :value="0" />
          <el-option :label="$t('virtual.order.statusPaid')" :value="1" />
          <el-option :label="$t('virtual.order.statusCompleted')" :value="2" />
          <el-option :label="$t('virtual.order.statusClosed')" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('virtual.order.createTime')">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          :start-placeholder="$t('common.startDate')"
          :end-placeholder="$t('common.endDate')"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ $t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="orderList">
      <el-table-column :label="$t('virtual.order.orderId')" align="center" prop="orderId" width="90" />
      <el-table-column :label="$t('virtual.order.orderNo')" align="center" prop="orderNo" width="180" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.order.userId')" align="center" prop="userId" width="80" />
      <el-table-column :label="$t('virtual.order.packageName')" align="center" prop="packageName" width="120" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.order.payAmount')" align="right" prop="payAmountDisplay" width="130">
        <template #default="scope">
          <strong>{{ scope.row.payAmountDisplay }}</strong>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.order.payToken')" align="center" prop="payToken" width="80" />
      <el-table-column :label="$t('virtual.order.payNetwork')" align="center" prop="payNetwork" width="100" />
      <el-table-column :label="$t('virtual.order.creditsAmount')" align="right" prop="creditsAmount" width="100" />
      <el-table-column :label="$t('virtual.order.bonusCredits')" align="right" prop="bonusCredits" width="90" />
      <el-table-column :label="$t('virtual.order.status')" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ scope.row.status === 'pending' ? $t('virtual.order.statusPending') : (scope.row.status === 'paid' ? $t('virtual.order.statusPaid') : (scope.row.status === 'completed' ? $t('virtual.order.statusCompleted') : (scope.row.status === 'closed' ? $t('virtual.order.statusClosed') : $t('common.unknown')))) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.order.txHash')" align="center" prop="txHash" width="140">
        <template #default="scope">
          <el-tooltip v-if="scope.row.txHash" :content="scope.row.txHash" placement="top">
            <span class="mono-text">{{ formatHash(scope.row.txHash) }}</span>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.order.createTime')" align="center" prop="createTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="$t('virtual.order.paidAt')" align="center" prop="paidAt" width="160">
        <template #default="scope">{{ parseTime(scope.row.paidAt) }}</template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="VirtualOrder">
import { listAdminOrders } from '@/api/virtual/order'
import { useI18n } from 'vue-i18n'

const { proxy } = getCurrentInstance()
const { t } = useI18n()

const orderList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: undefined,
    orderNo: undefined,
    txHash: undefined,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

function formatHash(hash) {
  if (!hash) return ''
  if (hash.length <= 14) return hash
  return hash.substring(0, 8) + '...' + hash.substring(hash.length - 6)
}

function getStatusType(status) {
  const map = { pending: 'warning', paid: 'primary', completed: 'success', closed: 'info' }
  return map[status] || 'info'
}

function getStatusLabel(status) {
  const map = { pending: '未支付', paid: '已支付', completed: '已完成', closed: '已关闭' }
  return map[status] || status
}

function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  listAdminOrders(params).then(response => {
    orderList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

getList()
</script>

<style scoped>
.mono-text {
  font-family: monospace;
  font-size: 12px;
}
</style>
