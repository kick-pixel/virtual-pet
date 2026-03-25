import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getBalance } from '@/api/credits'

export const useCreditsStore = defineStore('credits', () => {
    const balance = ref(0)
    const frozen = ref(0)
    const totalSpent = ref(0)
    const totalCredits = ref(0)
    const loading = ref(false)

    async function fetchBalance() {
        loading.value = true
        try {
            const res: any = await getBalance()
            // Support both direct data return and wrapped response
            const data = res.data || res
            balance.value = data.availableCredits ?? data.balance ?? 0
            frozen.value = data.frozenCredits ?? data.frozen ?? 0
            totalSpent.value = data.totalSpent ?? 0
            totalCredits.value = data.totalCredits ?? 0
        } catch (e) {
            console.error('Failed to fetch credits:', e)
        } finally {
            loading.value = false
        }
    }

    return {
        balance,
        frozen,
        totalSpent,
        totalCredits,
        loading,
        fetchBalance
    }
})
