/**
 * Format a number as credits display
 */
export function formatCredits(value: number | undefined | null): string {
    if (value == null) return '0'
    return value.toLocaleString()
}

/**
 * Format date string to local display
 */
export function formatDate(dateStr: string | undefined | null): string {
    if (!dateStr) return '-'
    const d = new Date(dateStr)
    return d.toLocaleString()
}

/**
 * Format short date
 */
export function formatShortDate(dateStr: string | undefined | null): string {
    if (!dateStr) return '-'
    const d = new Date(dateStr)
    return d.toLocaleDateString()
}

/**
 * Shorten wallet address: 0x1234...5678
 */
export function shortenAddress(address: string | undefined | null, chars = 4): string {
    if (!address) return ''
    return `${address.slice(0, chars + 2)}...${address.slice(-chars)}`
}

/**
 * Format file size
 */
export function formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * Format duration in seconds to display string
 */
export function formatDuration(seconds: number | undefined | null): string {
    if (seconds == null) return '-'
    if (seconds < 60) return `${seconds}s`
    const min = Math.floor(seconds / 60)
    const sec = seconds % 60
    return sec > 0 ? `${min}m ${sec}s` : `${min}m`
}
