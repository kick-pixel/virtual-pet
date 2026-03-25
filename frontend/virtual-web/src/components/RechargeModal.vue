<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { useAccount, useSendTransaction, useSwitchChain } from '@wagmi/vue'
import { parseEther, parseUnits } from 'viem'
import { getPackages, getWalletAddress, getExchangeRates, getNetworks, getTokens, createOrder, getOrder, submitTxHash } from '@/api/payment'
import { formatCredits } from '@/utils/format'
import { Loader2, Check, X, AlertCircle, Sparkles, Globe, Wallet, ChevronLeft } from 'lucide-vue-next'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success', credits: number): void
}>()

const { t } = useI18n()

// ===== Wallet Connection =====
// useAccount().chainId 由 wagmi 维护，支持 MetaMask / WalletConnect / Coinbase Wallet 等所有钱包
const { address, isConnected, chainId: walletChainId } = useAccount()
const { sendTransactionAsync } = useSendTransaction()
const { switchChainAsync } = useSwitchChain()

// 使用 wagmi useSwitchChain，兼容所有钱包类型（MetaMask/WalletConnect/Coinbase Wallet）
async function handleSwitchChain() {
  if (!selectedNetworkChainId.value) return
  try {
    await switchChainAsync({ chainId: selectedNetworkChainId.value })
  } catch (e: any) {
    // 用户主动取消切换，不报错
    if (e?.code !== 4001 && e?.name !== 'UserRejectedRequestError') {
      message.error(t('credits.switchNetworkFailed') || 'Failed to switch network')
    }
    throw e
  }
}

// ===== State =====
type Step = 'select' | 'connect' | 'confirm' | 'processing' | 'polling' | 'success' | 'error' | 'timeout'
const step = ref<Step>('select')
const packages = ref<any[]>([])
const exchangeRates = ref<any[]>([])
const networks = ref<any[]>([])
const allTokens = ref<any[]>([])
const allWalletAddresses = ref<any[]>([])
const selectedPackageId = ref<number | null>(null)
const selectedNetworkType = ref('')
const payToken = ref('')
const currentOrderNo = ref('')
const errorMsg = ref('')

// ===== Computed =====
const selectedPackage = computed(() =>
  packages.value.find(p => p.packageId === selectedPackageId.value)
)

const filteredTokens = computed(() => {
  if (!selectedNetworkType.value) return []
  return allTokens.value.filter((tk: any) => tk.networkType === selectedNetworkType.value)
})

const platformWalletAddress = computed(() => {
  if (!selectedNetworkType.value) return ''
  const matched = allWalletAddresses.value.find(
    (a: any) => a.networkType === selectedNetworkType.value
  )
  return matched?.walletAddress || ''
})

const selectedNetworkDisplay = computed(() => {
  const net = networks.value.find((n: any) => n.networkType === selectedNetworkType.value)
  return net?.networkName || selectedNetworkType.value
})

const currentRate = computed(() => {
  return exchangeRates.value.find((r: any) => r.tokenSymbol === payToken.value)
})

const payAmount = computed(() => {
  if (!selectedPackage.value) return '0'
  const pkg = selectedPackage.value as any
  const usdtPrice = Number(pkg.priceUsdt || pkg.price || 0)
  if (!usdtPrice) return '0'

  if (isNativeToken.value) {
    // 原生代币（BNB/ETH）：优先用套餐预设的 token 价格（更精确），没有再用汇率换算
    const tokenSymbol = payToken.value?.toUpperCase()
    const presetPrice = tokenSymbol === 'BNB' ? Number(pkg.priceBnb)
                      : tokenSymbol === 'ETH' ? Number(pkg.priceEth)
                      : 0
    if (presetPrice > 0) return presetPrice.toFixed(6)

    // 回退：用 effectiveRate（考虑手动覆盖的最终汇率）换算
    const rate = Number(currentRate.value?.effectiveRate || currentRate.value?.currentRate || 0)
    if (!rate) return '0'
    return (usdtPrice / rate).toFixed(6)
  }

  // ERC-20 稳定币（USDT/USDC）：金额就是套餐的 USDT 价格，无需换算
  return usdtPrice.toFixed(6)
})

// 根据钱包实际 chainId（wagmi 维护，支持所有钱包类型）在后端 networks 列表中查找对应的 networkType
const walletNetworkType = computed(() => {
  if (!walletChainId.value) return ''
  const matched = networks.value.find((n: any) => Number(n.chainId) === walletChainId.value)
  return matched?.networkType || ''
})

// 当前 UI 选中网络的 chainId（来源于后端配置，权威值）
const selectedNetworkChainId = computed(() => {
  const net = networks.value.find((n: any) => n.networkType === selectedNetworkType.value)
  return net ? Number(net.chainId) : null
})

const isNetworkMismatch = computed(() => {
  if (!isConnected.value || !selectedNetworkType.value || !networks.value.length) return false
  // 只要钱包当前网络与选中网络不一致就报错（包括钱包在未配置的网络上）
  return walletNetworkType.value !== selectedNetworkType.value
})

// Selected token's full config (contains isNative, contractAddress, decimals)
const currentTokenData = computed(() =>
  allTokens.value.find(
    (tk: any) => tk.tokenSymbol === payToken.value && tk.networkType === selectedNetworkType.value
  )
)

// 后端返回 isNative 为 boolean（true/false），原生代币无 contractAddress
const isNativeToken = computed(() => {
  if (!currentTokenData.value) return false
  return currentTokenData.value.isNative === true || !currentTokenData.value.contractAddress
})

// ===== Load Data =====
onMounted(async () => {
  // wagmi 自动追踪链切换，无需手动监听 chainChanged 事件
  await Promise.all([
    loadPackages(),
    loadExchangeRates(),
    loadNetworks(),
    loadAllTokens(),
    loadAllWalletAddresses()
  ])
  autoSelectNetwork()
})

async function loadPackages() {
  try {
    const res: any = await getPackages()
    packages.value = res.data || res.rows || []
  } catch (e) {
    console.error('Failed to load packages:', e)
  }
}

async function loadExchangeRates() {
  try {
    const res: any = await getExchangeRates()
    exchangeRates.value = res.data || res.rows || []
  } catch (e) {
    console.error('Failed to load exchange rates:', e)
  }
}

async function loadNetworks() {
  try {
    const res: any = await getNetworks()
    networks.value = res.data || res.rows || []
  } catch (e) {
    console.error('Failed to load networks:', e)
  }
}

async function loadAllTokens() {
  try {
    const res: any = await getTokens()
    allTokens.value = res.data || res.rows || []
  } catch (e) {
    console.error('Failed to load tokens:', e)
  }
}

async function loadAllWalletAddresses() {
  try {
    const res: any = await getWalletAddress()
    allWalletAddresses.value = res.data || res.rows || []
  } catch (e) {
    console.error('Failed to load wallet addresses:', e)
  }
}

// ===== Network & Token Selection =====
function autoSelectNetwork() {
  const walletNet = walletNetworkType.value
  if (walletNet && networks.value.some((n: any) => n.networkType === walletNet)) {
    selectNetwork(walletNet)
  } else if (networks.value.length > 0) {
    selectNetwork(networks.value[0].networkType)
  }
}

function selectNetwork(networkType: string) {
  selectedNetworkType.value = networkType
  const networkTokens = allTokens.value.filter((tk: any) => tk.networkType === networkType)
  if (networkTokens.length > 0) {
    payToken.value = networkTokens[0].tokenSymbol
  } else {
    payToken.value = ''
  }
}

// ===== Watchers =====
// Auto-advance from connect step once wallet is connected
watch(() => isConnected.value, (connected) => {
  if (connected && step.value === 'connect' && selectedPackageId.value) {
    step.value = 'confirm'
  }
})

// 当 walletChainId 变化（钱包切链），自动匹配对应网络
watch(walletChainId, () => {
  const walletNet = walletNetworkType.value
  if (walletNet && networks.value.some((n: any) => n.networkType === walletNet)) {
    selectNetwork(walletNet)
  }
})

// ===== Helper Functions =====
function close() {
  emit('update:visible', false)
}

// Reset state when modal closes
watch(() => props.visible, (visible) => {
  if (!visible) {
    stopPolling()
    setTimeout(() => {
      step.value = 'select'
      selectedPackageId.value = null
      currentOrderNo.value = ''
      errorMsg.value = ''
      autoSelectNetwork()
    }, 300)
  }
})

// ===== Payment Flow =====
function selectPackage(packageId: number) {
  selectedPackageId.value = packageId
  if (isConnected.value) {
    step.value = 'confirm'
  } else {
    step.value = 'connect'
  }
}

async function handlePay() {
  if (!selectedPackage.value || !isConnected.value) return
  if (!platformWalletAddress.value) {
    message.error(t('credits.noPlatformAddress'))
    return
  }
  if (!selectedNetworkChainId.value) {
    message.error(t('credits.selectNetwork') || 'Please select a network')
    return
  }
  // 严格验证：钱包实际链必须与选中网络完全一致（兼容所有钱包类型）
  if (!walletChainId.value || walletChainId.value !== selectedNetworkChainId.value) {
    message.error(t('credits.networkMismatch', [selectedNetworkDisplay.value]))
    return
  }

  step.value = 'processing'

  try {
    // 1. Create order（传 selectedNetworkChainId，来自后端 networks 配置）
    if (!selectedNetworkChainId.value) {
      throw new Error(t('credits.selectNetwork') || 'Please select a network')
    }
    const orderRes: any = await createOrder({
      packageId: selectedPackage.value.packageId,
      chainId: selectedNetworkChainId.value,
      payToken: payToken.value,
      walletAddress: address.value as string
    })

    const orderData = orderRes.data || orderRes
    currentOrderNo.value = orderData.orderNo

    // 2. Send transaction — native token vs ERC-20
    let txHash: string
    if (isNativeToken.value) {
      // BNB / ETH / MATIC: send native value
      txHash = String(await sendTransactionAsync({
        to: platformWalletAddress.value as `0x${string}`,
        value: parseEther(String(payAmount.value)),
        chainId: selectedNetworkChainId.value!
      }))
    } else {
      // ERC-20 (USDT / USDC): manually encode transfer(address,uint256) calldata
      // selector = keccak256("transfer(address,uint256)")[0:4] = 0xa9059cbb
      const decimals: number = currentTokenData.value?.decimals ?? 18
      const contractAddr = currentTokenData.value?.contractAddress as `0x${string}`
      const amountBig = parseUnits(String(payAmount.value), decimals)
      const addrHex = (platformWalletAddress.value as string).slice(2).toLowerCase().padStart(64, '0')
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const amountHex: string = (amountBig as any).toString(16).padStart(64, '0')
      const erc20Data = ('0xa9059cbb' + addrHex + amountHex) as `0x${string}`
      txHash = String(await sendTransactionAsync({
        to: contractAddr,
        data: erc20Data,
        value: 0n,
        chainId: selectedNetworkChainId.value!
      }))
    }

    // 3. Submit tx hash to backend
    try {
      await submitTxHash(currentOrderNo.value, String(txHash))
    } catch (err) {
      console.warn('Failed to submit tx hash:', err)
    }

    // 4. Start polling order status
    step.value = 'polling'
    startPolling(currentOrderNo.value)

  } catch (e: any) {
    step.value = 'error'
    // 用户主动拒绝（钱包弹窗点取消）
    if (e?.code === 4001 || e?.name === 'UserRejectedRequestError') {
      errorMsg.value = t('credits.userRejected') || 'Transaction cancelled'
    } else {
      // 优先用 shortMessage（wagmi 的简短描述），其次 message（来自 request.ts 已脱敏）
      const raw = e?.shortMessage || e?.message || ''
      // 过滤掉明显的技术堆栈信息（包含 Java 类名、SQL 关键字等）
      const isTechnical = /Exception|at com\.|mybatis|SQLException|NullPointer/i.test(raw)
      errorMsg.value = isTechnical ? t('credits.paymentFailed') || 'Payment failed, please try again' : (raw || t('credits.paymentFailed') || 'Payment failed')
    }
  }
}

// ===== Order Polling =====
let pollTimer: ReturnType<typeof setInterval> | null = null
const pollCount = ref(0)
const MAX_POLL_COUNT = 60

function startPolling(orderNo: string) {
  pollCount.value = 0
  pollTimer = setInterval(async () => {
    pollCount.value++

    if (pollCount.value >= MAX_POLL_COUNT) {
      stopPolling()
      step.value = 'timeout'
      return
    }

    try {
      const res: any = await getOrder(orderNo)
      const order = res.data || res

      if (order.status === 'completed') {
        stopPolling()
        step.value = 'success'
        const totalCredits = (order.creditsAmount || 0) + (order.bonusCredits || 0)
        emit('success', totalCredits)
      } else if (order.status === 'cancelled' || order.status === 'expired') {
        stopPolling()
        step.value = 'error'
        errorMsg.value = 'Order ' + order.status
      }
    } catch (err) {
      // Ignore polling errors
    }
  }, 5000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onUnmounted(() => {
  stopPolling()
})

function retry() {
  step.value = 'select'
  selectedPackageId.value = null
  currentOrderNo.value = ''
  errorMsg.value = ''
  autoSelectNetwork()
}
</script>

<template>
  <a-modal
    :open="visible"
    :footer="null"
    :closable="false"
    :width="860"
    centered
    wrapClassName="recharge-modal"
    @cancel="close"
  >
    <div class="rch-wrap">
      <!-- Back Button -->
      <button v-if="step === 'connect' || step === 'confirm'" @click="step = 'select'" class="rch-corner-btn rch-back">
        <ChevronLeft style="width: 20px; height: 20px;" />
      </button>

      <!-- Close Button -->
      <button @click="close" class="rch-corner-btn rch-close">
        <X style="width: 20px; height: 20px;" />
      </button>

      <!-- ===== SELECT STEP ===== -->
      <div v-if="step === 'select'">
        <!-- Header -->
        <div class="rch-header">
          <div class="rch-header-icon-wrap">
            <span style="font-size: 28px;">💎</span>
          </div>
          <div>
            <h3 class="rch-title">{{ t('credits.rechargeTitle') || 'Recharge Credits' }}</h3>
            <p class="rch-subtitle">{{ t('credits.rechargeSubtitle') || 'Choose a package and pay with cryptocurrency' }}</p>
          </div>
        </div>

        <!-- Packages Grid (single row) -->
        <div class="rch-pkg-grid">
          <div
            v-for="pkg in packages"
            :key="pkg.packageId"
            class="rch-pkg-card"
            :class="{ 'rch-pkg-card--selected': selectedPackageId === pkg.packageId }"
            @click="selectPackage(pkg.packageId)"
          >
            <div v-if="pkg.badge" class="rch-pkg-badge">{{ pkg.badge }}</div>
            <div class="rch-pkg-name">{{ pkg.packageName }}</div>
            <div class="rch-pkg-credits">{{ formatCredits(pkg.creditsAmount) }}</div>
            <div class="rch-pkg-unit">{{ t('generate.credits') || 'Credits' }}</div>
            <div v-if="pkg.bonusCredits" class="rch-pkg-bonus">+{{ formatCredits(pkg.bonusCredits) }} bonus</div>
            <div v-else class="rch-pkg-bonus-placeholder"></div>
            <div class="rch-pkg-price">${{ pkg.priceUsdt }}</div>
            <div v-if="pkg.description" class="rch-pkg-desc">{{ pkg.description }}</div>
          </div>
        </div>

        <!-- Network + Token row -->
        <div class="rch-nt-row">
          <!-- Network Selection -->
          <div class="rch-nt-section">
            <div class="rch-section-label">
              <Globe style="width: 13px; height: 13px;" />
              {{ t('credits.selectNetwork') }}
            </div>
            <div class="rch-tab-group">
              <button
                v-for="net in networks"
                :key="net.networkType"
                class="rch-tab"
                :class="{ 'rch-tab--active': selectedNetworkType === net.networkType }"
                @click="selectNetwork(net.networkType)"
              >{{ net.networkName }}</button>
            </div>
          </div>

          <!-- Token Selection -->
          <div class="rch-nt-section">
            <div class="rch-section-label">
              {{ t('credits.selectToken') }}
            </div>
            <div class="rch-tab-group">
              <button
                v-for="token in filteredTokens"
                :key="token.tokenSymbol + '-' + token.networkType"
                class="rch-tab"
                :class="{ 'rch-tab--active': payToken === token.tokenSymbol }"
                @click="payToken = token.tokenSymbol"
              >{{ token.tokenSymbol }}</button>
              <span v-if="filteredTokens.length === 0 && selectedNetworkType" class="rch-no-token">
                {{ t('credits.noTokensForNetwork') }}
              </span>
            </div>
          </div>
        </div>

        <!-- Network mismatch warning -->
        <div v-if="isConnected && isNetworkMismatch && selectedNetworkType" class="rch-warning">
          <AlertCircle style="width: 14px; height: 14px; flex-shrink: 0;" />
          <span>{{ t('credits.networkMismatch', [selectedNetworkDisplay]) }}</span>
        </div>

        <!-- Hint -->
        <p class="rch-hint">{{ t('credits.selectPackageHint') || 'Click a package above to continue' }}</p>
      </div>

      <!-- ===== CONNECT WALLET STEP ===== -->
      <div v-if="step === 'connect'" class="rch-connect">
        <div class="rch-connect-icon-wrap">
          <Wallet style="width: 30px; height: 30px; color: #fb923c;" />
        </div>
        <h3 class="rch-title" style="margin-bottom: 6px;">
          {{ t('credits.connectWalletTitle') || 'Connect Your Wallet' }}
        </h3>
        <p class="rch-subtitle" style="margin-bottom: 24px;">
          {{ t('credits.connectWalletHint') || 'Connect your wallet to proceed with payment' }}
        </p>

        <!-- Selected package summary -->
        <div v-if="selectedPackage" class="rch-connect-summary">
          <div class="rch-connect-summary-row">
            <span>{{ t('credits.selectPackage') }}</span>
            <strong style="color: rgba(45,28,8,0.88);">{{ formatCredits(selectedPackage.creditsAmount) }} {{ t('generate.credits') || 'Credits' }}</strong>
          </div>
          <div class="rch-connect-summary-row">
            <span>{{ t('credits.amount') }}</span>
            <strong style="color: #c07040;">${{ selectedPackage.priceUsdt }}</strong>
          </div>
          <div v-if="selectedNetworkType" class="rch-connect-summary-row">
            <span>{{ t('wallet.network') }}</span>
            <strong style="color: rgba(45,28,8,0.88);">{{ selectedNetworkDisplay }} / {{ payToken }}</strong>
          </div>
        </div>

        <!-- Web3Modal connect button -->
        <div class="rch-w3m-wrap">
          <w3m-button balance="hide" />
        </div>
      </div>

      <!-- ===== CONFIRM STEP ===== -->
      <div v-if="step === 'confirm'">
        <div style="text-align: center; margin-bottom: 20px;">
          <div style="display: inline-flex; justify-content: center; align-items: center; width: 56px; height: 56px; border-radius: 16px; background: linear-gradient(135deg, rgba(251, 146, 60, 0.2), rgba(245, 158, 11, 0.05)); margin: 0 auto 12px; border: 1px solid rgba(251, 146, 60, 0.2);">
            <Sparkles style="width: 28px; height: 28px; color: #fb923c;" />
          </div>
          <h3 class="rch-title" style="font-size: 18px;">{{ t('credits.step3') || 'Confirm Payment' }}</h3>
        </div>

        <div class="rch-summary" style="margin-bottom: 16px; padding: 12px 16px;">
          <div class="summary-row">
            <span style="color: rgba(70,48,20,0.55);">{{ t('credits.selectPackage') }}</span>
            <span style="font-weight: 600; font-size: 15px;">{{ formatCredits(selectedPackage?.creditsAmount || 0) }} <span style="font-size: 12px; color: rgba(70,48,20,0.45); font-weight: 400;">{{ t('generate.credits') }}</span></span>
          </div>
          <div class="summary-row">
            <span style="color: rgba(70,48,20,0.55);">{{ t('wallet.network') }}</span>
            <span style="font-weight: 600; font-size: 15px;">{{ selectedNetworkDisplay }}</span>
          </div>
          <div class="summary-row" style="border-bottom: none; padding-bottom: 0;">
            <span style="color: rgba(70,48,20,0.55);">{{ t('credits.selectToken') }}</span>
            <span style="font-weight: 600; font-size: 15px;">{{ payToken }}</span>
          </div>
        </div>

        <div style="background: rgba(212, 137, 90, 0.06); border: 1px solid rgba(212, 137, 90, 0.3); border-radius: 12px; padding: 14px 16px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
          <span style="color: rgba(70,48,20,0.72); font-size: 14px; font-weight: 500;">{{ t('credits.amount') }}</span>
          <div style="text-align: right;">
            <div style="font-weight: 800; font-size: 20px; color: #c07040; line-height: 1; margin-bottom: 4px;">{{ payAmount }} <span style="font-size: 13px; font-weight: 600;">{{ payToken }}</span></div>
            <div v-if="currentRate" style="font-size: 11px; color: rgba(70,48,20,0.45);">
              {{ t('credits.exchangeRate') }}: 1 {{ payToken }} ≈ ${{ currentRate.currentRate }}
            </div>
          </div>
        </div>

        <!-- Network mismatch warning in confirm step -->
        <div v-if="isNetworkMismatch" class="rch-mismatch-card">
          <div class="rch-mismatch-body">
            <AlertCircle class="rch-mismatch-icon" />
            <div>
              <div class="rch-mismatch-title">{{ t('wallet.network') }}</div>
              <div class="rch-mismatch-desc">{{ t('credits.networkMismatch', [selectedNetworkDisplay]) }}</div>
            </div>
          </div>
          <a-button
            type="primary"
            block
            @click="handleSwitchChain"
            class="rch-switch-btn"
          >
            {{ t('wallet.switchNetwork') }} → {{ selectedNetworkDisplay }}
          </a-button>
        </div>

        <a-button
          @click="handlePay"
          type="primary"
          block
          size="large"
          :disabled="isNetworkMismatch"
          style="height: 52px; border-radius: 12px; background: linear-gradient(90deg, #d4895a 0%, #ebb07a 100%); border: none; font-weight: 600; font-size: 16px; box-shadow: 0 4px 14px rgba(212, 137, 90, 0.3);"
        >
          {{ t('credits.payNow') || 'Pay Now' }}
        </a-button>
      </div>

      <!-- ===== PROCESSING STEP ===== -->
      <div v-if="step === 'processing'" style="text-align: center; padding: 40px 0;">
        <Loader2 class="spin-icon" style="width: 60px; height: 60px; color: #d4895a; margin: 0 auto 20px;" />
        <h3 class="rch-title">{{ t('credits.paymentSent') || 'Sending Payment...' }}</h3>
        <p class="rch-subtitle">{{ t('wallet.signing') || 'Please confirm in your wallet' }}</p>
      </div>

      <!-- ===== POLLING STEP ===== -->
      <div v-if="step === 'polling'" style="text-align: center; padding: 40px 0;">
        <Loader2 class="spin-icon" style="width: 60px; height: 60px; color: #d4895a; margin: 0 auto 20px;" />
        <h3 class="rch-title">{{ t('credits.waitingConfirmation') || 'Waiting for Confirmation...' }}</h3>
        <p class="rch-subtitle" style="margin-bottom: 12px;">{{ t('credits.confirmationHint') || 'This may take 1–5 minutes' }}</p>
        <div style="font-size: 12px; color: rgba(70,48,20,0.45);">{{ pollCount }} / {{ MAX_POLL_COUNT }}</div>
      </div>

      <!-- ===== SUCCESS STEP ===== -->
      <div v-if="step === 'success'" style="text-align: center; padding: 40px 0;">
        <div class="rch-result-icon rch-result-icon--success">
          <Check style="width: 44px; height: 44px; color: #22c55e;" />
        </div>
        <h3 class="rch-title">{{ t('credits.rechargeSuccess') || 'Recharge Successful!' }}</h3>
        <p class="rch-subtitle" style="margin-bottom: 24px;">
          {{ t('credits.creditsAdded', [(selectedPackage?.creditsAmount || 0) + (selectedPackage?.bonusCredits || 0)]) }}
        </p>
        <a-button
          @click="close"
          type="primary"
          size="large"
          style="height: 48px; padding: 0 32px; border-radius: 12px; background: #22c55e; border: none; font-weight: 600;"
        >
          {{ t('common.confirm') || 'OK' }}
        </a-button>
      </div>

      <!-- ===== ERROR STEP ===== -->
      <div v-if="step === 'error'" style="text-align: center; padding: 40px 0;">
        <div class="rch-result-icon rch-result-icon--error">
          <X style="width: 44px; height: 44px; color: #ef4444;" />
        </div>
        <h3 class="rch-title">{{ t('credits.rechargeFailed') || 'Recharge Failed' }}</h3>
        <p class="rch-subtitle" style="margin-bottom: 24px;">{{ errorMsg }}</p>
        <a-space :size="12">
          <a-button @click="retry" size="large" style="height: 44px; padding: 0 24px; border-radius: 10px;">
            {{ t('credits.tryAgain') || 'Try Again' }}
          </a-button>
          <a-button @click="close" type="primary" size="large" style="height: 44px; padding: 0 24px; border-radius: 10px;">
            {{ t('credits.close') || 'Close' }}
          </a-button>
        </a-space>
      </div>

      <!-- ===== TIMEOUT STEP ===== -->
      <div v-if="step === 'timeout'" style="text-align: center; padding: 40px 0;">
        <div class="rch-result-icon rch-result-icon--warn">
          <AlertCircle style="width: 44px; height: 44px; color: #f59e0b;" />
        </div>
        <h3 class="rch-title">{{ t('credits.rechargeTimeout') || 'Taking Longer Than Expected' }}</h3>
        <p class="rch-subtitle" style="margin-bottom: 24px;">{{ t('credits.timeoutHint') }}</p>
        <a-button @click="close" type="primary" size="large" style="height: 48px; padding: 0 32px; border-radius: 12px;">
          {{ t('credits.checkLater') || 'OK' }}
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<style scoped>
.rch-wrap {
  padding: 24px 28px 28px;
  position: relative;
}

/* Corner buttons */
.rch-corner-btn {
  position: absolute;
  top: 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: rgba(70, 48, 20, 0.45);
  transition: all 0.2s;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 8px;
}
.rch-corner-btn:hover {
  color: rgba(45, 28, 8, 0.9);
  background: rgba(180, 140, 60, 0.12);
}
.rch-close {
  right: 16px;
}
.rch-back {
  left: 16px;
}

/* Header */
.rch-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.rch-header-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  flex-shrink: 0;
}
.rch-title {
  font-size: 20px;
  font-weight: 800;
  color: rgba(45, 28, 8, 0.92);
  margin: 0 0 4px;
}
.rch-subtitle {
  font-size: 13px;
  color: rgba(70, 48, 20, 0.62);
  margin: 0;
}

/* Package grid - auto-fit so all packages appear in one row */
.rch-pkg-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}
.rch-pkg-card {
  position: relative;
  padding: 14px 10px 12px;
  border: 1px solid rgba(190, 155, 95, 0.28);
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  background: rgba(255, 248, 236, 0.7);
  backdrop-filter: blur(12px);
}
.rch-pkg-card:hover {
  border-color: rgba(212, 137, 90, 0.55);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(212, 137, 90, 0.15);
}
.rch-pkg-card--selected {
  border-color: #d4895a;
  background: linear-gradient(135deg, rgba(212, 137, 90, 0.1), rgba(235, 176, 122, 0.06));
  box-shadow: 0 8px 24px rgba(212, 137, 90, 0.2);
}
.rch-pkg-badge {
  position: absolute;
  top: -8px;
  right: -6px;
  background: linear-gradient(135deg, #d4895a, #ebb07a);
  color: white;
  font-size: 9px;
  font-weight: 700;
  padding: 3px 7px;
  border-radius: 10px;
  text-transform: uppercase;
  letter-spacing: 0.4px;
  white-space: nowrap;
}
.rch-pkg-name {
  font-size: 11px;
  font-weight: 600;
  color: rgba(70, 48, 20, 0.55);
  margin-bottom: 5px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.rch-pkg-credits {
  font-size: 26px;
  font-weight: 800;
  color: #c07040;
  line-height: 1.1;
}
.rch-pkg-unit {
  font-size: 11px;
  color: rgba(70, 48, 20, 0.45);
  margin-bottom: 3px;
}
.rch-pkg-bonus {
  font-size: 11px;
  color: #059669;
  font-weight: 600;
  margin-bottom: 5px;
}
.rch-pkg-bonus-placeholder {
  height: 17px;
  margin-bottom: 5px;
}
.rch-pkg-price {
  font-size: 17px;
  font-weight: 700;
  color: rgba(45, 28, 8, 0.88);
}
.rch-pkg-desc {
  font-size: 10px;
  color: rgba(70, 48, 20, 0.45);
  margin-top: 3px;
}

/* Network + Token row */
.rch-nt-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 12px;
}
.rch-section-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 600;
  color: rgba(70, 48, 20, 0.55);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 7px;
}

/* Pill tabs for network / token */
.rch-tab-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.rch-tab {
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid rgba(190, 155, 95, 0.3);
  background: rgba(253, 248, 240, 0.8);
  font-size: 13px;
  font-weight: 500;
  color: rgba(70, 48, 20, 0.72);
  cursor: pointer;
  transition: all 0.2s;
  line-height: 1.6;
  white-space: nowrap;
}
.rch-tab:hover {
  border-color: rgba(212, 137, 90, 0.55);
  color: rgba(45, 28, 8, 0.92);
  background: rgba(212, 137, 90, 0.08);
}
.rch-tab--active {
  border-color: #d4895a;
  background: linear-gradient(90deg, #d4895a 0%, #ebb07a 100%);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(212, 137, 90, 0.3);
}
.rch-no-token {
  font-size: 12px;
  color: rgba(70, 48, 20, 0.4);
}

/* Warning（select 步骤小提示条） */
.rch-warning {
  display: flex;
  align-items: center;
  gap: 7px;
  background: #fef3c7;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 10px;
  font-size: 12px;
  color: #92400e;
}

/* Network mismatch card（confirm 步骤完整卡片） */
.rch-mismatch-card {
  border: 1.5px solid #fbbf24;
  border-radius: 12px;
  background: linear-gradient(135deg, #fffbeb, #fef9ee);
  padding: 14px 16px;
  margin-bottom: 16px;
}
.rch-mismatch-body {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
}
.rch-mismatch-icon {
  width: 18px;
  height: 18px;
  color: #d97706;
  flex-shrink: 0;
  margin-top: 2px;
}
.rch-mismatch-title {
  font-size: 12px;
  font-weight: 600;
  color: #92400e;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 2px;
}
.rch-mismatch-desc {
  font-size: 13px;
  color: #78350f;
  line-height: 1.45;
}
.rch-switch-btn {
  height: 38px !important;
  border-radius: 9px !important;
  background: linear-gradient(135deg, #f59e0b, #d97706) !important;
  border: none !important;
  font-weight: 600 !important;
  font-size: 13px !important;
  letter-spacing: 0.01em;
}

/* Hint text at bottom of select step */
.rch-hint {
  text-align: center;
  font-size: 12px;
  color: rgba(70, 48, 20, 0.45);
  margin: 4px 0 0;
}

/* Connect wallet step */
.rch-connect {
  text-align: center;
  padding: 16px 40px 4px;
}
.rch-connect-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  margin-bottom: 16px;
}
.rch-connect-summary {
  background: rgba(255, 248, 236, 0.8);
  border: 1px solid rgba(190, 155, 95, 0.25);
  border-radius: 10px;
  padding: 12px 16px;
  margin-bottom: 20px;
  text-align: left;
}
.rch-connect-summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: rgba(70, 48, 20, 0.72);
  padding: 5px 0;
}
.rch-connect-summary-row:not(:last-child) {
  border-bottom: 1px solid rgba(190, 155, 95, 0.2);
}
.rch-w3m-wrap {
  display: flex;
  justify-content: center;
  margin: 0 0 12px;
}

/* Confirm step summary */
.rch-summary {
  background: rgba(255, 248, 236, 0.8);
  border: 1px solid rgba(190, 155, 95, 0.25);
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 20px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 7px 0;
  font-size: 14px;
  color: rgba(45, 28, 8, 0.85);
}
.summary-row:not(:last-child) {
  border-bottom: 1px solid rgba(190, 155, 95, 0.2);
}

/* Result step icons */
.rch-result-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 20px;
}
.rch-result-icon--success { background: #dcfce7; }
.rch-result-icon--error   { background: #fee2e2; }
.rch-result-icon--warn    { background: #fef3c7; }

/* Spin animation */
.spin-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}
</style>

<style>
/* 充值弹框 - 暖米白背景 */
.recharge-modal .ant-modal-content {
  background: #fdf8f0 !important;
  border-radius: 16px !important;
  overflow: hidden !important;
  padding: 0 !important;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18) !important;
}
.recharge-modal .ant-modal-body {
  padding: 0 !important;
  background: #fdf8f0 !important;
}
</style>
