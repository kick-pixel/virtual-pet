import { createWeb3Modal } from '@web3modal/wagmi/vue'
import { createConfig, http, useAccount, useConnect, useDisconnect, useSendTransaction, useBalance, useSignMessage } from '@wagmi/vue'
import { mainnet, sepolia, bsc, bscTestnet } from '@wagmi/vue/chains'
import { walletConnect, injected } from '@wagmi/vue/connectors'
import { watchAccount } from '@wagmi/core'
import { ref, computed } from 'vue'

// 1. Get projectId
const projectId = import.meta.env.VITE_WC_PROJECT_ID || 'YOUR_PROJECT_ID'

// 2. Create wagmiConfig
const metadata = {
    name: 'Pet AI',
    description: 'AI Pet Video Generator',
    url: 'https://zyxgo.online/',
    icons: ['https://avatars.githubusercontent.com/u/37784886']
}

export const config = createConfig({
    // BSC 放第一位作为默认链，确保 Web3Modal 连接时默认使用 BNB Chain
    chains: [bsc, mainnet, bscTestnet, sepolia],
    transports: {
        [bsc.id]: http(),
        [mainnet.id]: http(),
        [bscTestnet.id]: http(),
        [sepolia.id]: http()
    },
    connectors: [
        walletConnect({ projectId, metadata, showQrModal: true }), // Enable QR modal for WalletConnect
        injected({ shimDisconnect: true }) // Auto-detect injected wallets (MetaMask etc.)
    ]
})

// 3. Create modal（defaultChain: bsc 确保初次连接时默认展示 BNB Chain）
const modal = createWeb3Modal({
    wagmiConfig: config,
    projectId,
    defaultChain: bsc,
    enableAnalytics: true,
    allWallets: 'HIDE', // Hide "All Wallets" view
    enableEIP6963: true, // Enable EIP-6963 to detect installed wallets correctly
    // Limit displayed wallets to MetaMask and Binance Web3 Wallet
    includeWalletIds: [
        'c57ca95b47569778a828d19178114f4db188b89b763c899ba0be274e97267d96', // MetaMask
        '8a0ee50d1f22f6995a9623097548a2e1726b7dd8280a4c7795e54f696f4420d9'  // Binance Web3 Wallet
    ]
})

export function useWallet() {
    const { address, isConnected, chainId } = useAccount()
    const { connect, connectors } = useConnect()
    const { disconnect } = useDisconnect()
    const { sendTransactionAsync } = useSendTransaction()
    const { signMessageAsync } = useSignMessage()
    const { data: balance } = useBalance({ address })

    const trimAddress = (addr: string) => {
        if (!addr) return ''
        return `${addr.slice(0, 6)}...${addr.slice(-4)}`
    }

    const openModal = async () => {
        if (modal && modal.open) {
            modal.open()
        } else {
            // Fallback: try to connect with injected connector (MetaMask)
            const injectedConnector = connectors.find((c: any) => c.id === 'injected' || c.id === 'io.metamask')
            if (injectedConnector) {
                try {
                    await connect({ connector: injectedConnector })
                } catch (error) {
                    console.error('Failed to connect wallet:', error)
                }
            }
        }
    }

    return {
        address,
        isConnected,
        chainId,
        balance,
        trimAddress,
        disconnect,
        sendTransactionAsync,
        signMessageAsync,
        openModal
    }
}
