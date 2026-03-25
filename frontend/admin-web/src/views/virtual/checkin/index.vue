<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
      <el-form-item :label="$t('virtual.checkin.userId')" prop="userId">
        <el-input v-model="queryParams.userId" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleQuery" style="width:140px" />
      </el-form-item>
      <el-form-item :label="$t('virtual.checkin.checkinTime')">
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

    <el-table v-loading="loading" :data="checkinList">
      <el-table-column :label="$t('virtual.checkin.userId')" align="center" prop="userId" width="80" />
      <el-table-column :label="$t('virtual.checkin.email')" align="center" prop="email" min-width="160" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.checkin.checkinTime')" align="center" prop="checkinTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.checkinTime) }}</template>
      </el-table-column>
      <el-table-column :label="$t('virtual.checkin.lastCheckinDate')" align="center" prop="lastCheckinDate" width="120" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.checkin.currentStreak')" align="right" prop="currentStreak" width="140">
        <template #default="scope">
          <el-tag type="success">{{ scope.row.currentStreak }} {{ $t('virtual.checkin.days') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.checkin.maxStreakDays')" align="right" prop="maxStreakDays" width="150">
        <template #default="scope">
          <el-tag type="warning">{{ scope.row.maxStreakDays }} {{ $t('virtual.checkin.days') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.checkin.totalCheckinDays')" align="right" prop="totalCheckinDays" width="140">
        <template #default="scope">
          <strong>{{ scope.row.totalCheckinDays }} {{ $t('virtual.checkin.days') }}</strong>
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

<script setup name="VirtualCheckin">
import { listAdminCheckins } from '@/api/virtual/checkin'
import { useI18n } from 'vue-i18n'

const { proxy } = getCurrentInstance()
const { t } = useI18n()

const checkinList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    queryUserId: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  listAdminCheckins(params).then(response => {
    checkinList.value = response.rows
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
