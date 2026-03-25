<template>
  <!-- 创建表 -->
  <el-dialog :title="$t('gen.createTable')" v-model="visible" width="800px" top="5vh" append-to-body>
    <span>{{ $t('gen.createTableDesc') }}</span>
    <el-input type="textarea" :rows="10" :placeholder="$t('gen.createTablePlaceholder')" v-model="content"></el-input>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleImportTable">{{ $t('button.confirm') }}</el-button>
        <el-button @click="visible = false">{{ $t('button.cancel') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { createTable } from "@/api/tool/gen"

const { t } = useI18n()
const visible = ref(false)
const content = ref("")
const { proxy } = getCurrentInstance()
const emit = defineEmits(["ok"])

/** 显示弹框 */
function show() {
  visible.value = true
}

/** 导入按钮操作 */
function handleImportTable() {
  if (content.value === "") {
    proxy.$modal.msgError(t('gen.createTableError'))
    return
  }
  createTable({ sql: content.value }).then(res => {
    proxy.$modal.msgSuccess(res.msg)
    if (res.code === 200) {
      visible.value = false
      emit("ok")
    }
  })
}

defineExpose({
  show,
})
</script>
