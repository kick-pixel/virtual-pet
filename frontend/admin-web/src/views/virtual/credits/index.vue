<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
      <el-form-item :label="$t('virtual.credits.userId')" prop="userId">
        <el-input v-model="queryParams.userId" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleQuery" style="width:140px" />
      </el-form-item>
      <el-form-item :label="$t('virtual.credits.direction')" prop="direction">
        <el-select v-model="queryParams.direction" :placeholder="$t('common.pleaseSelect')" clearable style="width:120px">
          <el-option :label="$t('virtual.credits.directionIn')" :value="1" />
          <el-option :label="$t('virtual.credits.directionOut')" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('virtual.credits.createTime')">
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

    <el-table v-loading="loading" :data="creditsLogList">
      <el-table-column :label="$t('virtual.credits.txId')" align="center" prop="txId" width="100" />
      <el-table-column :label="$t('virtual.credits.userId')" align="center" prop="userId" width="100" />
      <el-table-column :label="$t('virtual.credits.direction')" align="center" prop="direction" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.direction === 1 ? 'success' : 'danger'">
            {{ scope.row.direction === 1 ? $t('virtual.credits.directionIn') : (scope.row.direction === 2 ? $t('virtual.credits.directionOut') : $t('common.unknown')) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.credits.amount')" align="right" prop="amount" width="120">
        <template #default="scope">
          <span :style="{ color: scope.row.direction === 1 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
            {{ scope.row.direction === 1 ? '+' : '-' }}{{ scope.row.amount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.credits.txType')" align="left" prop="txType" min-width="120" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.credits.relatedId')" align="center" prop="businessId" width="120" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.credits.balanceBefore')" align="right" prop="balanceBefore" width="110" />
      <el-table-column :label="$t('virtual.credits.balanceAfter')" align="right" prop="balanceAfter" width="110" />
      <el-table-column :label="$t('virtual.credits.remark')" align="left" prop="remark" min-width="160" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.credits.createTime')" align="center" prop="createTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
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

<script setup name="VirtualCredits">
import { listAdminCredits } from '@/api/virtual/credits'
import { useI18n } from 'vue-i18n'

const { proxy } = getCurrentInstance()
const { t } = useI18n()

const creditsLogList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: undefined,
    direction: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listAdminCredits(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    creditsLogList.value = response.rows
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
.credits-in {
  color: #67C23A;
  font-weight: bold;
  font-family: monospace;
}
.credits-out {
  color: #F56C6C;
  font-weight: bold;
  font-family: monospace;
}
</style>
