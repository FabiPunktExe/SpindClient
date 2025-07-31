import {Server} from "../api.ts"
import {Box, Button, Popover} from "@mui/material"
import {Clear, Edit} from "@mui/icons-material"
import {useState} from "react"
import ServerEditDialog from "../dialogs/ServerEditDialog.tsx"
import ServerRemoveDialog from "../dialogs/ServerRemoveDialog.tsx"

export default function ServerContextMenu({servers, setServers, server, setServer, anchor, setAnchor}: {
    servers: Server[]
    setServers: (servers: Server[]) => Promise<void>
    server?: Server
    setServer: (server?: Server) => void
    anchor?: HTMLElement
    setAnchor: (anchor?: HTMLElement) => void
}) {
    const [editDialogOpen, setEditDialogOpen] = useState(false)
    const [removeDialogOpen, setRemoveDialogOpen] = useState(false)

    function close() {
        setServer(undefined)
        setAnchor(undefined)
    }
    function openEditDialog() {
        setAnchor(undefined)
        setEditDialogOpen(true)
    }
    function openRemoveDialog() {
        setAnchor(undefined)
        setRemoveDialogOpen(true)
    }

    return <>
        <Popover open={server != undefined && anchor != undefined}
                 anchorEl={anchor}
                 onClose={close}
                 anchorOrigin={{vertical: "center", horizontal: "right"}}
                 transformOrigin={{vertical: "center", horizontal: "left"}}>
            <Box className="p-2 flex flex-col gap-2">
                <Button type="button"
                        variant="outlined"
                        startIcon={<Edit/>}
                        onClick={openEditDialog}>Edit server</Button>
                <Button type="button"
                        variant="outlined"
                        color="error"
                        startIcon={<Clear/>}
                        onClick={openRemoveDialog}>Remove server</Button>
            </Box>
        </Popover>
        <ServerEditDialog opened={editDialogOpen}
                          close={() => setEditDialogOpen(false)}
                          servers={servers}
                          setServers={setServers}
                          server={server}/>
        <ServerRemoveDialog opened={removeDialogOpen}
                            close={() => setRemoveDialogOpen(false)}
                            servers={servers}
                            setServers={setServers}
                            server={server}/>
    </>
}
