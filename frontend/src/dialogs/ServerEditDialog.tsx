import {Server} from "../api.ts"
import {Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField} from "@mui/material"
import {Cancel, Edit} from "@mui/icons-material"
import {FormEvent} from "react"

export default function ServerEditDialog({opened, close, servers, setServers, server}: {
    opened: boolean
    close: () => void
    servers: Server[]
    setServers: (servers: Server[]) => Promise<void>
    server?: Server
}) {
    function onSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        const data = new FormData(event.currentTarget)
        const name = data.get("name") as string
        const address = data.get("address") as string
        const username = data.get("username") as string

        const index = servers.findIndex(s => s == server)
        if (index < 0) {
            return
        }
        const newServers = [...servers]
        newServers[index] = {name, address, username}
        setServers(newServers).then(close)
    }

    return <Dialog open={opened} onClose={close} slotProps={{paper: {component: "form", onSubmit}}}>
        <DialogTitle>Edit server</DialogTitle>
        <DialogContent className="flex flex-col gap-2">
            <TextField name="name"
                       label="Display name"
                       required={true}
                       defaultValue={server?.name}
                       autoComplete="off"
                       autoCorrect="off"
                       className="mt-1.5!"/>
            <TextField name="address"
                       label="Server address"
                       required={true}
                       defaultValue={server?.address}
                       autoComplete="off"
                       autoCorrect="off"/>
            <TextField name="username"
                       label="Username"
                       required={true}
                       defaultValue={server?.username}
                       autoComplete="off"
                       autoCorrect="off"/>
        </DialogContent>
        <DialogActions>
            <Button type="button" variant="outlined" startIcon={<Cancel/>} onClick={close}>Cancel</Button>
            <Button type="submit" variant="contained" startIcon={<Edit/>}>Edit server</Button>
        </DialogActions>
    </Dialog>
}
