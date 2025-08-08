import {Password, Server} from "./api.ts"

declare global {
    interface Window {
        spind: {
            getServers(): string
            setServers(servers: string): boolean
            isLocked(server: string): boolean
            unlock(server: string, password: string): string
            lock(server: string): void
            setup(server: string, password: string): string
            getPasswords(server: string): string
            setPasswords(server: string, passwords: string): string
            copyToClipboard(label: string, text: string): void
            openInBrowser(url: string): void
            generate2FACode(secret: string): string | null
        }
    }
}

export function registerAndroidApi() {
    window.spind$getServers = async () => {
        return JSON.parse(window.spind.getServers())
    }
    window.spind$setServers = async (servers: Server[]) => {
        window.spind.setServers(JSON.stringify(servers))
    }
    window.spind$isLocked = async (server: Server) => {
        return window.spind.isLocked(JSON.stringify(server))
    }
    window.spind$unlock = async (server: Server, password: string) => {
        return JSON.parse(window.spind.unlock(JSON.stringify(server), password))
    }
    window.spind$lock = async (server: Server) => {
        window.spind.lock(JSON.stringify(server))
    }
    window.spind$setup = async (server: Server, password: string) => {
        return JSON.parse(window.spind.setup(JSON.stringify(server), password))
    }
    window.spind$getPasswords = async (server: Server) => {
        return JSON.parse(window.spind.getPasswords(JSON.stringify(server)))
    }
    window.spind$setPasswords = async (server: Server, passwords: Password[]) => {
        return JSON.parse(window.spind.setPasswords(JSON.stringify(server), JSON.stringify(passwords)))
    }
    window.spind$copyToClipboard = async (label: string, text: string) => {
        window.spind.copyToClipboard(label, text)
    }
    window.spind$openInBrowser = async (url: string) => {
        window.spind.openInBrowser(url)
    }
    window.spind$generate2FACode = async (secret: string) => {
        return window.spind.generate2FACode(secret)
    }
}
