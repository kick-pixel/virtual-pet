<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { getCheckinStatus } from '@/api/checkin'
import RechargeModal from './RechargeModal.vue'
import CheckinPaymentModal from './CheckinPaymentModal.vue'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ (e: 'update:open', val: boolean): void }>()

const { t } = useI18n()

interface DayInfo {
  day: number
  credits: number
  claimed: boolean
  isToday: boolean
}

const checkinDays = ref<DayInfo[]>([])
const currentSequence = ref(0)
const todayClaimed = ref(false)
const loading = ref(false)
const showRechargeModal = ref(false)
const showCheckinPayModal = ref(false)

async function fetchStatus() {
  loading.value = true
  try {
    const res: any = await getCheckinStatus()
    const data = res.data || res
    const days = Array.isArray(data.days) ? data.days : []
    checkinDays.value = days.map((day: any) => ({
      ...day,
      credits: Number(day.day) === 7 ? 50 : 30
    }))
    currentSequence.value = data.currentSequence || 0
    todayClaimed.value = data.todayClaimed || false
  } catch (e) {
    console.error('Failed to fetch checkin status:', e)
  } finally {
    loading.value = false
  }
}

function handleClaim() {
  showCheckinPayModal.value = true
}

async function onCheckinPaySuccess() {
  showCheckinPayModal.value = false
  await fetchStatus()
}

watch(() => props.open, (val) => {
  if (val) fetchStatus()
})

function onRechargeSuccess() {
  // Credits balance refreshed via store elsewhere
}
</script>

<template>
  <a-modal
    :open="props.open"
    @update:open="emit('update:open', $event)"
    @cancel="emit('update:open', false)"
    :title="null"
    :footer="null"
    :width="520"
    centered
    :body-style="{ padding: 0, background: 'transparent' }"
    class="checkin-root-modal"
  >
    <!-- Entire dark area -->
    <div class="ci-wrapper">
      <!-- Inner card -->
      <div class="ci-card">
        <!-- Header -->
        <div class="ci-header">
          <span class="ci-header-icon">🎁</span>
          <div>
            <h2 class="ci-title">{{ t('checkin.dailyCheckin') }}</h2>
            <p class="ci-subtitle">{{ t('checkin.subtitle') }}</p>
          </div>
        </div>

        <!-- 7-day grid -->
        <a-spin :spinning="loading">
          <div class="ci-grid">
            <div
              v-for="day in checkinDays"
              :key="day.day"
              class="ci-day"
              :class="{
                'ci-day--today': day.isToday,
                'ci-day--claimed': day.claimed,
                'ci-day--future': !day.claimed && !day.isToday,
                'ci-day--special': day.day === 7
              }"
            >
              <!-- Grand prize badge -->
              <div v-if="day.day === 7" class="ci-grand-badge">{{ t('checkin.grandPrize') }}</div>

              <!-- Day coin / gift -->
              <div class="ci-day-icon">
                <div v-if="day.day === 7" class="ci-gift">🎁</div>
                <div v-else class="ci-coin" :class="{ 'ci-coin--lit': day.claimed || day.isToday }">
                  <span class="ci-coin-text">+{{ day.credits }}</span>
                </div>
              </div>

              <!-- Day label -->
              <div class="ci-day-label">{{ t('checkin.day', { n: day.day }) }}</div>

              <!-- Claimed check -->
              <div v-if="day.claimed" class="ci-day-check">✓</div>
            </div>
          </div>
        </a-spin>

        <!-- Streak badge -->
        <div v-if="currentSequence > 0" class="ci-streak">
          {{ t('checkin.sequenceLabel', { n: currentSequence }) }}
        </div>

        <!-- Claim / Already claimed button -->
        <button
          v-if="!todayClaimed"
          class="ci-claim-btn"
          :disabled="loading"
          @click="handleClaim"
        >
          <span class="ci-bnb-icon">⊕</span>
          <span>{{ t('checkin.claimBtn') }}</span>
        </button>
        <div v-else class="ci-claimed-bar">
          ✓ {{ t('checkin.alreadyClaimed') }}
        </div>
      </div>

      <!-- Recharge area (outside card, inside modal) -->
      <div class="ci-recharge-area">
        <span class="ci-recharge-link" @click="showRechargeModal = true">
          {{ t('checkin.rechargeForMore') }} ›
        </span>
      </div>
    </div>

    <!-- RechargeModal teleports to body -->
    <RechargeModal
      :visible="showRechargeModal"
      @update:visible="showRechargeModal = $event"
      @success="onRechargeSuccess"
    />

    <!-- CheckinPaymentModal: 签到支付弹框 -->
    <CheckinPaymentModal
      :visible="showCheckinPayModal"
      @update:visible="showCheckinPayModal = $event"
      @success="onCheckinPaySuccess"
    />
  </a-modal>
</template>

<style>
/* Global (non-scoped) to pierce Ant Design Vue portal */
.checkin-root-modal .ant-modal-content {
  background: #fdf8f0 !important;
  padding: 0 !important;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18), 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  border-radius: 16px !important;
  overflow: hidden !important;
}
.checkin-root-modal .ant-modal-close {
  color: rgba(70, 48, 20, 0.45) !important;
  top: 10px !important;
  right: 10px !important;
}
.checkin-root-modal .ant-modal-close:hover {
  color: rgba(45, 28, 8, 0.72) !important;
  background: rgba(180, 140, 60, 0.12) !important;
}
.checkin-root-modal .ant-modal-body {
  padding: 0 !important;
  background: transparent !important;
}
</style>

<style scoped>
.ci-wrapper {
  background: #fdf8f0;
  border-radius: 16px;
  overflow: hidden;
}

.ci-card {
  background: rgba(255, 248, 235, 0.7);
  border-radius: 12px;
  margin: 16px;
  padding: 20px 18px 18px;
  border: 1px solid rgba(190, 155, 95, 0.25);
}

/* Header */
.ci-header {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 18px;
}

.ci-header-icon {
  font-size: 26px;
  line-height: 1;
  margin-top: 2px;
  flex-shrink: 0;
}

.ci-title {
  font-size: 18px;
  font-weight: 800;
  color: rgba(45, 28, 8, 0.92);
  margin: 0 0 3px;
  letter-spacing: 0.02em;
}

.ci-subtitle {
  font-size: 12px;
  color: rgba(100, 65, 20, 0.5);
  margin: 0;
  line-height: 1.4;
}

/* 7-day grid */
.ci-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  margin-bottom: 14px;
}

.ci-day {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 2px 5px;
  border-radius: 10px;
  background: rgba(253, 248, 240, 0.8);
  border: 1.5px solid rgba(190, 155, 95, 0.28);
}

.ci-day--today {
  border-color: #d4895a !important;
  background: rgba(212, 137, 90, 0.08) !important;
}

.ci-day--claimed {
  border-color: rgba(212, 137, 90, 0.5) !important;
  background: rgba(212, 137, 90, 0.06) !important;
}

.ci-day--special {
  border-color: rgba(192, 96, 8, 0.45) !important;
}

/* Grand prize badge */
.ci-grand-badge {
  position: absolute;
  top: -9px;
  left: 50%;
  transform: translateX(-50%);
  background: #d4895a;
  color: white;
  font-size: 8px;
  font-weight: 700;
  padding: 2px 5px;
  border-radius: 4px;
  white-space: nowrap;
}

/* Coin / gift icon */
.ci-day-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 34px;
  height: 34px;
  margin-bottom: 3px;
}

.ci-gift {
  font-size: 22px;
  line-height: 1;
}

.ci-coin {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(253, 248, 240, 0.9);
  border: 2px solid rgba(190, 155, 95, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ci-coin--lit {
  background: linear-gradient(135deg, #d4895a, #ebb07a);
  border-color: #d4895a;
}

.ci-coin-text {
  font-size: 8px;
  font-weight: 800;
  color: rgba(100, 65, 20, 0.38);
  line-height: 1;
}

.ci-coin--lit .ci-coin-text {
  color: rgba(255, 255, 255, 0.92);
}

/* Day label */
.ci-day-label {
  font-size: 9px;
  color: rgba(100, 65, 20, 0.38);
  letter-spacing: 0.02em;
  line-height: 1;
  margin-bottom: 2px;
}

.ci-day--today .ci-day-label {
  color: #c06008;
}

.ci-day--claimed .ci-day-label {
  color: rgba(180, 115, 50, 0.65);
}

.ci-day-check {
  font-size: 9px;
  font-weight: 700;
  color: #c06008;
  line-height: 1;
}

/* Streak */
.ci-streak {
  text-align: center;
  font-size: 12px;
  color: #c06008;
  background: rgba(212, 137, 90, 0.1);
  border: 1px solid rgba(212, 137, 90, 0.3);
  border-radius: 20px;
  padding: 4px 12px;
  margin-bottom: 14px;
}

/* Claim button */
.ci-claim-btn {
  width: 100%;
  height: 50px;
  background: linear-gradient(135deg, #d4895a 0%, #ebb07a 100%);
  border: none;
  border-radius: 10px;
  color: #ffffff;
  font-size: 17px;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  letter-spacing: 0.05em;
  box-shadow: 0 4px 16px rgba(212, 137, 90, 0.38);
}

.ci-claim-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #c07848 0%, #d9a06a 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(212, 137, 90, 0.52);
}

.ci-claim-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.ci-bnb-icon {
  font-size: 22px;
  font-weight: 900;
  line-height: 1;
}

/* Already claimed */
.ci-claimed-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(212, 137, 90, 0.08);
  border: 1px solid rgba(212, 137, 90, 0.3);
  border-radius: 10px;
  color: #c06008;
  font-size: 14px;
  font-weight: 600;
  gap: 6px;
}

/* Recharge area — outside card, inside wrapper */
.ci-recharge-area {
  padding: 12px 18px 16px;
}

.ci-recharge-link {
  color: rgba(100, 65, 20, 0.6);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s;
  letter-spacing: 0.01em;
}

.ci-recharge-link:hover {
  color: #c06008;
}
</style>
