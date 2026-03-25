```
<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
      <el-form-item :label="$t('virtual.video.taskId')" prop="taskId">
        <el-input v-model="queryParams.taskId" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleQuery" style="width:140px" />
      </el-form-item>
      <el-form-item :label="$t('virtual.video.virtualUserId')" prop="virtualUserId">
        <el-input v-model="queryParams.virtualUserId" :placeholder="$t('common.pleaseInput')" clearable @keyup.enter="handleQuery" style="width:140px" />
      </el-form-item>
      <el-form-item :label="$t('virtual.video.adminStatus')" prop="adminStatus">
        <el-select v-model="queryParams.adminStatus" :placeholder="$t('common.pleaseSelect')" clearable style="width:120px">
          <el-option :label="$t('virtual.video.normal')" value="1" />
          <el-option :label="$t('virtual.video.banned')" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('virtual.video.createTime')">
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

    <el-table v-loading="loading" :data="videoList">
      <el-table-column :label="$t('virtual.video.taskId')" align="center" prop="taskId" width="120" />
      <el-table-column :label="$t('virtual.video.virtualUserId')" align="center" prop="virtualUserId" width="100" />
      <el-table-column :label="$t('virtual.video.videoPicUrl')" align="center" prop="videoPicUrl" width="100">
        <template #default="scope">
          <el-image
            v-if="scope.row.videoPicUrl"
            style="width: 60px; height: 80px"
            :src="scope.row.videoPicUrl"
            :preview-src-list="[scope.row.videoPicUrl]"
            preview-teleported
            fit="cover"
          />
          <span v-else>{{ $t('virtual.video.noCover') }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.video.ossVideoUrl')" align="center" prop="ossVideoUrl" width="120">
        <template #default="scope">
          <el-button
            v-if="scope.row.ossVideoUrl"
            link
            type="primary"
            @click="handlePreview(scope.row)"
          >{{ $t('virtual.video.preview') }}</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('virtual.video.promptText')" align="left" prop="promptText" min-width="200" show-overflow-tooltip />
      <el-table-column :label="$t('virtual.video.videoDuration')" align="center" prop="videoDuration" width="80" />
      <el-table-column :label="$t('virtual.video.viewCount')" align="right" prop="viewCount" width="90" />
      <el-table-column :label="$t('virtual.video.likeCount')" align="right" prop="likeCount" width="90" />
      <el-table-column :label="$t('virtual.video.createTime')" align="center" prop="createTime" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column :label="$t('virtual.video.adminStatus')" align="center" prop="adminStatus" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.adminStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.adminStatus === 1 ? $t('virtual.video.normal') : $t('virtual.video.banned') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" align="center" width="120" fixed="right">
        <template #default="scope">
          <el-button
            link
            :type="scope.row.adminStatus === 1 ? 'danger' : 'success'"
            size="small"
            @click="handleStatus(scope.row)"
            v-hasPermi="['virtual:video:edit']"
          >
            {{ scope.row.adminStatus === 1 ? $t('virtual.video.actionBan') : $t('virtual.video.actionUnban') }}
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

    <!-- 视频预览对话框 -->
    <el-dialog title="preview" v-model="previewOpen" width="640px" append-to-body destroy-on-close>
      <video v-if="previewUrl" :src="previewUrl" controls style="width:100%;max-height:400px;" autoplay />
    </el-dialog>
  </div>
</template>

<script setup name="VirtualVideo">
import { listAdminVideos, updateVideoAdminStatus } from '@/api/virtual/video'
import { useI18n } from 'vue-i18n'

const { proxy } = getCurrentInstance()
const { t } = useI18n()

const videoList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])
const previewOpen = ref(false)
const previewUrl = ref('')

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    virtualUserId: undefined,
    adminStatus: undefined,
    taskId: undefined // Added taskId to queryParams based on template change
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listAdminVideos(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    videoList.value = response.rows
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

function handlePreview(row) {
  previewUrl.value = row.ossVideoUrl
  previewOpen.value = true
}

function handleStatus(row) {
  const newVal = row.adminStatus === 1 ? 0 : 1
  const label = newVal === 1 ? t('virtual.video.actionUnban') : t('virtual.video.actionBan')
  const extraMsg = newVal === 0 ? t('virtual.video.extraBanMsg') : ''
  proxy.$modal.confirm(t('virtual.video.confirmAction', { label: label, id: row.taskId, extra: extraMsg })).then(() => {
    updateVideoAdminStatus(row.id, newVal).then(() => { // Changed row.taskId to row.id based on instruction
      proxy.$modal.msgSuccess(t('virtual.user.actionSuccess'))
      getList()
    })
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.no-cover {
  color: #ccc;
  font-size: 12px;
}
</style>
