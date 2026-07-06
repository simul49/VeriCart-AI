<template>
  <div class="page-container" style="max-width:600px">
    <h2 class="section-title">My Profile</h2>

    <div style="background:white;border-radius:16px;padding:32px">
      <div style="display:flex;align-items:center;gap:16px;margin-bottom:24px">
        <el-avatar :size="64" :src="profile.avatar">{{ profile.username?.[0]?.toUpperCase() }}</el-avatar>
        <div>
          <h3 style="font-size:20px">{{ profile.username }}</h3>
          <p style="color:#6B7280">{{ profile.email }}</p>
          <el-tag size="small">{{ profile.role }}</el-tag>
        </div>
      </div>

      <el-form :model="profile" label-position="top">
        <el-form-item label="Phone">
          <el-input v-model="profile.phone" placeholder="Phone number" />
        </el-form-item>
        <el-form-item label="Avatar URL">
          <el-input v-model="profile.avatar" placeholder="https://..." />
        </el-form-item>
        <el-button type="primary" @click="handleUpdate" :loading="saving">Save Changes</el-button>
      </el-form>
    </div>

    <div style="background:white;border-radius:16px;padding:32px;margin-top:16px">
      <h3 style="margin-bottom:16px">Change Password</h3>
      <el-form :model="pwForm" label-position="top">
        <el-form-item label="Current Password">
          <el-input v-model="pwForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="New Password">
          <el-input v-model="pwForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-button type="warning" @click="handleChangePw" :loading="changingPw">Change Password</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api'
import { ElMessage } from 'element-plus'

const profile = ref({})
const saving = ref(false)
const changingPw = ref(false)
const pwForm = ref({ oldPassword: '', newPassword: '' })

onMounted(async () => {
  const res = await userApi.profile()
  profile.value = res.data
})

async function handleUpdate() {
  saving.value = true
  try {
    await userApi.updateProfile({ phone: profile.value.phone, avatar: profile.value.avatar })
    ElMessage.success('Profile updated')
  } finally { saving.value = false }
}

async function handleChangePw() {
  if (!pwForm.value.oldPassword || !pwForm.value.newPassword) {
    return ElMessage.warning('Please fill in both fields')
  }
  changingPw.value = true
  try {
    await userApi.changePassword(pwForm.value)
    ElMessage.success('Password changed')
    pwForm.value = { oldPassword: '', newPassword: '' }
  } finally { changingPw.value = false }
}
</script>
