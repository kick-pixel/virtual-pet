<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :span="24" style="text-align: center; padding: 60px 20px;">
        <div class="welcome-container">
          <h1 class="welcome-title">{{ $t('common.welcome') }} {{ title }}</h1>
          <p class="welcome-subtitle">{{ $t('dashboard.subtitle') }}</p>
          <div class="version-info">
            <el-tag type="primary" size="large">{{ $t('dashboard.version') }}: v{{ version }}</el-tag>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-divider />

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon size="24" color="#409EFF"><Platform /></el-icon>
              <span class="card-title">{{ $t('dashboard.frontend') }}</span>
            </div>
          </template>
          <ul class="tech-list">
            <li>Vue 3</li>
            <li>Element Plus</li>
            <li>Vite</li>
            <li>Pinia</li>
            <li>Vue Router</li>
            <li>Axios</li>
          </ul>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon size="24" color="#67C23A"><Monitor /></el-icon>
              <span class="card-title">{{ $t('dashboard.backend') }}</span>
            </div>
          </template>
          <ul class="tech-list">
            <li>Spring Boot</li>
            <li>Spring Security</li>
            <li>MyBatis</li>
            <li>JWT</li>
            <li>Redis</li>
            <li>MySQL</li>
          </ul>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-card shadow="hover" class="feature-card">
          <template #header>
            <div class="card-header">
              <el-icon size="24" color="#F56C6C"><Connection /></el-icon>
              <span class="card-title">{{ $t('dashboard.features') }}</span>
            </div>
          </template>
          <ul class="tech-list">
            <li>{{ $t('dashboard.feature1') }}</li>
            <li>{{ $t('dashboard.feature2') }}</li>
            <li>{{ $t('dashboard.feature3') }}</li>
            <li>{{ $t('dashboard.feature4') }}</li>
            <li>{{ $t('dashboard.feature5') }}</li>
            <li>{{ $t('dashboard.feature6') }}</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import { Platform, Monitor, Connection } from '@element-plus/icons-vue'

const title = import.meta.env.VITE_APP_TITLE || 'Admin System'
const version = ref('3.9.1')
</script>

<style scoped lang="scss">
.home {
  .welcome-container {
    padding: 40px 0;

    .welcome-title {
      font-size: 42px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 20px;
      background: linear-gradient(to right, #409EFF, #67C23A);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .welcome-subtitle {
      font-size: 18px;
      color: #606266;
      margin-bottom: 30px;
    }

    .version-info {
      margin-top: 20px;
    }
  }

  .feature-card {
    margin-bottom: 20px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;

      .card-title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }

    .tech-list {
      list-style: none;
      padding: 0;
      margin: 0;

      li {
        padding: 10px 0;
        border-bottom: 1px solid #EBEEF5;
        color: #606266;
        font-size: 15px;
        transition: all 0.2s;

        &:last-child {
          border-bottom: none;
        }

        &:hover {
          color: #409EFF;
          padding-left: 10px;
        }
      }
    }
  }
}
</style>
