import {Add, Cancel} from "@mui/icons-material"
import {Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField} from "@mui/material"
import {Server} from "../api.ts"
import {FormEvent} from "react"

export default function ServerAddDialog({opened, close, addServer}: {
    opened: boolean
    close: () => void
    addServer: (server: Server) => Promise<void>}){
    function onSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        const data = new FormData(event.currentTarget)
        const name = data.get("name") as string
        const address = data.get("address") as string
        const username = data.get("username") as string
        addServer({name, address, username}).then(close)
    }

    return <Dialog open={opened} onClose={close} slotProps={{paper: {component: "form", onSubmit}}}>
        <DialogTitle>Add server</DialogTitle>
        <DialogContent className="flex flex-col gap-2">
            <TextField name="name"
                       label="Display name"
                       required={true}
                       autoComplete="off"
                       autoCorrect="off"
                       className="mt-1.5!"/>
            <TextField name="address"
                       label="Server address"
                       required={true}
                       autoComplete="off"
                       autoCorrect="off"/>
            <TextField name="username"
                       label="Username"
                       required={true}
                       autoComplete="off"
                       autoCorrect="off"/>
        </DialogContent>
        <DialogActions>
            <Button type="button" variant="outlined" startIcon={<Cancel/>} onClick={close}>Cancel</Button>
            <Button type="submit" variant="contained" startIcon={<Add/>}>Add server</Button>
        </DialogActions>
    </Dialog>
}
