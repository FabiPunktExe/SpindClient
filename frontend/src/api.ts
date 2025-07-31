export type Server = {
    name: string
    address: string
    username: string
}

export type Password = {
    name: string
    password: string
    fields: {[key: string]: string}
}

declare global {
    interface Window {
        spind$getServers(): Promise<Server[]>
        spind$setServers(servers: Server[]): Promise<void>
        spind$isLocked(server: Server): Promise<boolean>
        spind$unlock(server: Server, password: string): Promise<boolean | string>
        spind$lock(server: Server): Promise<void>
        spind$setup(server: Server, password: string): Promise<true | string>
        spind$getPasswords(server: Server): Promise<Password[]>
        spind$setPasswords(server: Server, passwords: Password[]): Promise<true | string>
        spind$copyToClipboard(label: string, text: string): Promise<void>
    }
}
