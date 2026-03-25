<template>
   <el-form ref="pwdRef" :model="user" :rules="rules" label-width="80px">
      <el-form-item :label="$t('profile.oldPassword')" prop="oldPassword">
         <el-input v-model="user.oldPassword" :placeholder="$t('profile.oldPasswordPlaceholder')" type="password" show-password />
      </el-form-item>
      <el-form-item :label="$t('profile.newPassword')" prop="newPassword">
         <el-input v-model="user.newPassword" :placeholder="$t('profile.newPasswordPlaceholder')" type="password" show-password />
      </el-form-item>
      <el-form-item :label="$t('profile.confirmPassword')" prop="confirmPassword">
         <el-input v-model="user.confirmPassword" :placeholder="$t('profile.confirmPasswordPlaceholder')" type="password" show-password/>
      </el-form-item>
      <el-form-item>
      <el-button type="primary" @click="submit">{{ $t('button.save') }}</el-button>
      <el-button type="danger" @click="close">{{ $t('button.close') }}</el-button>
      </el-form-item>
   </el-form>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { updateUserPwd } from "@/api/system/user"

const { t } = useI18n()
const { proxy } = getCurrentInstance()

const user = reactive({
  oldPassword: undefined,
  newPassword: undefined,
  confirmPassword: undefined
})

const equalToPassword = (rule, value, callback) => {
  if (user.newPassword !== value) {
    callback(new Error(t('profile.passwordNotMatch')))
  } else {
    callback()
  }
}

const rules = ref({
  oldPassword: [{ required: true, message: t('user.inputPassword'), trigger: "blur" }],
  newPassword: [
    { required: true, message: t('user.inputPassword'), trigger: "blur" },
    { min: 6, max: 20, message: t('user.passwordLengthBetween'), trigger: "blur" },
    { pattern: /^[^<>"'|\\]+$/, message: t('user.passwordIllegalChar'), trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: t('user.inputPassword'), trigger: "blur" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ]
})

/** 提交按钮 */
function submit() {
  proxy.$refs.pwdRef.validate(valid => {
    if (valid) {
      updateUserPwd(user.oldPassword, user.newPassword).then(() => {
        proxy.$modal.msgSuccess(t('profile.resetSuccess'))
      })
    }
  })
}

/** 关闭按钮 */
function close() {
  proxy.$tab.closePage()
}
</script>
