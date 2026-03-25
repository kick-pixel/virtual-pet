<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="auto">
      <el-form-item :label="$t('virtual.stats.dateRange')">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          :start-placeholder="$t('common.startDate')"
          :end-placeholder="$t('common.endDate')"
          :shortcuts="dateShortcuts"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ $t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <!-- 汇总卡片 -->
    <el-row :gutter="16" class="stat-cards mb16" v-if="summary.totalCount !== undefined">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ summary.totalCount }}</div>
          <div class="stat-label">{{ $t('virtual.stats.totalCount') }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value success-val">{{ summary.successCount }}</div>
          <div class="stat-label">{{ $t('virtual.stats.successCount') }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value danger-val">{{ summary.failCount }}</div>
          <div class="stat-label">{{ $t('virtual.stats.failCount') }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value warning-val">{{ summary.timeoutCount }}</div>
          <div class="stat-label">{{ $t('virtual.stats.timeoutCount') }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value primary-val">{{ summary.successRate }}%</div>
          <div class="stat-label">{{ $t('virtual.stats.successRate') }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ summary.avgDurationS }}s</div>
          <div class="stat-label">{{ $t('virtual.stats.avgDuration') }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="statsList" stripe>
      <el-table-column :label="$t('virtual.stats.statDate')" align="center" prop="statDate" width="120" />
      <el-table-column :label="$t('virtual.stats.totalCount')" align="right" prop="totalCount" width="120" />
      <el-table-column :label="$t('virtual.stats.successCount')" align="right" prop="successCount" width="110">
        <template #default="scope">
          <span class="success-val">{{ scope.row.successCount }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.stats.failCount')" align="right" prop="failCount" width="110">
        <template #default="scope">
          <span class="danger-val">{{ scope.row.failCount }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.stats.timeoutCount')" align="right" prop="timeoutCount" width="110">
        <template #default="scope">
          <span class="warning-val">{{ scope.row.timeoutCount }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.stats.successRate')" align="right" prop="successRate" width="100">
        <template #default="scope">
          <el-progress
            :percentage="Number(scope.row.successRate) || 0"
            :color="getProgressColor(scope.row.successRate)"
            :stroke-width="10"
            style="width:120px"
          />
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.stats.avgDuration')" align="right" prop="avgDurationS" width="130">
        <template #default="scope">{{ scope.row.avgDurationS || '-' }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup name="VirtualStats">
import { getApiStats } from '@/api/virtual/stats'
import { useI18n } from 'vue-i18n'

const { proxy } = getCurrentInstance()
const { t } = useI18n()

const statsList = ref([])
const loading = ref(true)
const summary = ref({})

// 默认查近 7 天
const today = new Date()
const sevenDaysAgo = new Date(today)
sevenDaysAgo.setDate(today.getDate() - 6)
const fmt = (d) => d.toISOString().slice(0, 10)
const dateRange = ref([fmt(sevenDaysAgo), fmt(today)])

const queryParams = reactive({})

const dateShortcuts = [
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 6)
      return [fmt(start), fmt(end)]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 29)
      return [fmt(start), fmt(end)]
    }
  }
]

function getProgressColor(rate) {
  const v = Number(rate) || 0
  if (v >= 90) return '#67C23A'
  if (v >= 70) return '#E6A23C'
  return '#F56C6C'
}

function calcSummary(list) {
  if (!list || list.length === 0) { summary.value = {}; return }
  let total = 0, success = 0, fail = 0, timeout = 0, durSum = 0, durCnt = 0
  list.forEach(item => {
    total += item.totalCount || 0
    success += item.successCount || 0
    fail += item.failCount || 0
    timeout += item.timeoutCount || 0
    if (item.avgDurationS) { durSum += Number(item.avgDurationS); durCnt++ }
  })
  summary.value = {
    totalCount: total,
    successCount: success,
    failCount: fail,
    timeoutCount: timeout,
    successRate: total > 0 ? ((success / total) * 100).toFixed(1) : '0.0',
    avgDurationS: durCnt > 0 ? (durSum / durCnt).toFixed(1) : '-'
  }
}

function getList() {
  loading.value = true
  const params = {}
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  getApiStats(params).then(response => {
    statsList.value = response.data || []
    calcSummary(statsList.value)
    loading.value = false
  })
}

function handleQuery() {
  getList()
}

function resetQuery() {
  dateRange.value = [fmt(sevenDaysAgo), fmt(today)]
  getList()
}

getList()
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.stat-cards .stat-card {
  text-align: center;
  padding: 8px 0;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.4;
}
.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.success-val { color: #67C23A; }
.danger-val  { color: #F56C6C; }
.warning-val { color: #E6A23C; }
.primary-val { color: #409EFF; }
</style>
