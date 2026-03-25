<template>
   <el-form ref="userRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item :label="$t('profile.nickName')" prop="nickName">
         <el-input v-model="form.nickName" maxlength="30" />
      </el-form-item>
      <el-form-item :label="$t('profile.phoneNumber')" prop="phonenumber">
         <el-input v-model="form.phonenumber" maxlength="11" />
      </el-form-item>
      <el-form-item :label="$t('profile.email')" prop="email">
         <el-input v-model="form.email" maxlength="50" />
      </el-form-item>
      <el-form-item :label="$t('profile.sex')">
         <el-radio-group v-model="form.sex">
            <el-radio value="0">{{ $t('profile.male') }}</el-radio>
            <el-radio value="1">{{ $t('profile.female') }}</el-radio>
         </el-radio-group>
      </el-form-item>
      <el-form-item>
      <el-button type="primary" @click="submit">{{ $t('button.save') }}</el-button>
      <el-button type="danger" @click="close">{{ $t('button.close') }}</el-button>
      </el-form-item>
   </el-form>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { updateUserProfile } from "@/api/system/user"

const { t } = useI18n()
const props = defineProps({
  user: {
    type: Object
  }
})

const { proxy } = getCurrentInstance()

const form = ref({})
const rules = ref({
  nickName: [{ required: true, message: t('user.nicknameRequired'), trigger: "blur" }],
  email: [
    { required: true, message: t('user.inputEmail'), trigger: "blur" },
    { type: "email", message: t('user.emailFormat'), trigger: ["blur", "change"] }
  ],
  phonenumber: [
    { required: true, message: t('user.inputPhonenumber'), trigger: "blur" },
    { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: t('user.phonenumberFormat'), trigger: "blur" }
  ],
})

/** 提交按钮 */
function submit() {
  proxy.$refs.userRef.validate(valid => {
    if (valid) {
      updateUserProfile(form.value).then(() => {
        proxy.$modal.msgSuccess(t('profile.updateSuccess'))
        props.user.phonenumber = form.value.phonenumber
        props.user.email = form.value.email
      })
    }
  })
}

/** 关闭按钮 */
function close() {
  proxy.$tab.closePage()
}

// 回显当前登录用户信息
watch(() => props.user, user => {
  if (user) {
    form.value = { nickName: user.nickName, phonenumber: user.phonenumber, email: user.email, sex: user.sex }
  }
},{ immediate: true })
</script>
