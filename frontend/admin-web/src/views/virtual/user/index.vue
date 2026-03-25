<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
      <el-form-item :label="$t('virtual.user.userId')" prop="userId">
        <el-input v-model="queryParams.userId" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleQuery" style="width:140px" />
      </el-form-item>
      <el-form-item :label="$t('virtual.user.email')" prop="email">
        <el-input v-model="queryParams.email" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item :label="$t('virtual.user.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="$t('common.pleaseSelect')" clearable style="width:120px">
          <el-option :label="$t('virtual.user.normal')" value="1" />
          <el-option :label="$t('virtual.user.disabled')" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('virtual.user.createTime')">
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

    <el-table v-loading="loading" :data="userList">
      <el-table-column :label="$t('virtual.user.userId')" align="center" prop="userId" width="80" />
      <el-table-column :label="$t('virtual.user.email')" align="center" prop="email" min-width="160" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.user.walletAddress')" align="center" prop="walletAddress" width="140">
        <template #default="scope">
          <el-tooltip v-if="scope.row.walletAddress" :content="scope.row.walletAddress" placement="top">
            <span class="mono-text">{{ formatAddress(scope.row.walletAddress) }}</span>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.user.telegramId')" align="center" prop="telegramId" width="120" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.user.createTime')" align="center" prop="createTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="$t('virtual.user.lastLoginTime')" align="center" prop="lastLoginTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.lastLoginTime) }}</template>
      </el-table-column>
      <el-table-column :label="$t('virtual.user.registerIp')" align="center" prop="registerIp" width="120" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.user.countryRegion')" align="center" prop="countryRegion" width="100" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.user.status')" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? $t('virtual.user.normal') : $t('virtual.user.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.user.genDisabled')" align="center" prop="genDisabled" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.genDisabled === 1" type="warning">{{ $t('common.disable') }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.user.totalGenCount')" align="right" prop="totalGenCount" width="90" />
      <el-table-column :label="$t('virtual.user.failGenCount')" align="right" prop="failGenCount" width="90" />
      <el-table-column :label="$t('virtual.user.totalEarned')" align="right" prop="totalEarned" width="110" />
      <el-table-column :label="$t('virtual.user.totalSpent')" align="right" prop="totalSpent" width="110" />
      <el-table-column :label="$t('virtual.user.balance')" align="right" prop="balance" width="90">
        <template #default="scope">
          <strong>{{ scope.row.balance }}</strong>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" align="center" width="180" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="warning"
            size="small"
            @click="handleGenDisabled(scope.row)"
            v-hasPermi="['virtual:user:edit']"
          >
            {{ scope.row.genDisabled === 1 ? $t('virtual.user.actionUnbanGen') : $t('virtual.user.actionBanGen') }}
          </el-button>
          <el-button
            link
            :type="scope.row.status === 1 ? 'danger' : 'success'"
            size="small"
            @click="handleStatus(scope.row)"
            v-hasPermi="['virtual:user:edit']"
          >
            {{ scope.row.status === 1 ? $t('virtual.user.actionDisableAccount') : $t('virtual.user.actionEnableAccount') }}
          </el-button>
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
  </div>
</template>

<script setup name="VirtualUser">
import { listVirtualUsers, updateGenDisabled, updateUserStatus } from '@/api/virtual/user'
import { useI18n } from 'vue-i18n'

const { proxy } = getCurrentInstance()
const { t } = useI18n()

const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: undefined,
    email: undefined,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

function formatAddress(address) {
  if (!address) return ''
  if (address.length <= 12) return address
  return address.substring(0, 6) + '...' + address.substring(address.length - 4)
}

function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  listVirtualUsers(params).then(response => {
    userList.value = response.rows
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

function handleGenDisabled(row) {
  const newVal = row.genDisabled === 1 ? 0 : 1
  const label = newVal === 1 ? t('virtual.user.actionBanGen') : t('virtual.user.actionUnbanGen')
  proxy.$modal.confirm(t('virtual.user.confirmAction', { label: label, user: row.email || row.userId })).then(() => {
    updateGenDisabled(row.userId, newVal).then(() => {
      proxy.$modal.msgSuccess(t('virtual.user.actionSuccess'))
      getList()
    })
  }).catch(() => {})
}

function handleStatus(row) {
  const newVal = row.status === 1 ? 0 : 1
  const label = newVal === 1 ? t('virtual.user.actionEnableAccount') : t('virtual.user.actionDisableAccount')
  proxy.$modal.confirm(t('virtual.user.confirmAction', { label: label, user: row.email || row.userId })).then(() => {
    updateUserStatus(row.userId, newVal).then(() => {
      proxy.$modal.msgSuccess(t('virtual.user.actionSuccess'))
      getList()
    })
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.mono-text {
  font-family: monospace;
  font-size: 12px;
}
</style>
