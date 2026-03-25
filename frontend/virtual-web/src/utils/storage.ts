const TOKEN_KEY = 'virtual_token'
const LOCALE_KEY = 'virtual_locale'

export function getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
    localStorage.removeItem(TOKEN_KEY)
}

export function getLocale(): string {
    return localStorage.getItem(LOCALE_KEY) || 'en-US'
}

export function setLocale(locale: string): void {
    localStorage.setItem(LOCALE_KEY, locale)
}
