import {Server} from "../api.ts"
import {Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography} from "@mui/material"
import {Cancel, Clear} from "@mui/icons-material"

export default function ServerRemoveDialog({opened, close, servers, setServers, server}: {
    opened: boolean
    close: () => void
    servers: Server[]
    setServers: (servers: Server[]) => Promise<void>
    server?: Server
}) {
    function remove() {
        setServers(servers.filter(s => s != server)).then(close)
    }

    return <Dialog open={opened} onClose={close}>
        <DialogTitle>Delete server</DialogTitle>
        <DialogContent className="flex flex-col gap-2">
            <Typography>Are you sure you want to delete the server <strong>{server?.name}</strong>?</Typography>
        </DialogContent>
        <DialogActions>
            <Button type="button" variant="outlined" startIcon={<Cancel/>} onClick={close}>Cancel</Button>
            <Button type="button" variant="contained" startIcon={<Clear/>} onClick={remove}>Remove server</Button>
        </DialogActions>
    </Dialog>
}
