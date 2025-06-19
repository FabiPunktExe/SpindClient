import {Password} from "../api.ts"
import {Box, Button, Popover} from "@mui/material"
import {DeleteForever, Edit} from "@mui/icons-material"
import {useState} from "react"
import PasswordEditDialog from "../dialogs/PasswordEditDialog.tsx"
import PasswordDeleteDialog from "../dialogs/PasswordDeleteDialog.tsx"

export default function PasswordMenu({passwords, setPasswords, password, setPassword, anchor, setAnchor}: {
    passwords: Password[]
    setPasswords: (passwords: Password[]) => Promise<void>
    password?: Password
    setPassword: (password?: Password) => void
    anchor?: HTMLElement
    setAnchor: (anchor?: HTMLElement) => void
}) {
    const [editDialogOpen, setEditDialogOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)

    function close() {
        setPassword(undefined)
        setAnchor(undefined)
    }
    function openEditDialog() {
        setAnchor(undefined)
        setEditDialogOpen(true)
    }
    function openRemoveDialog() {
        setAnchor(undefined)
        setDeleteDialogOpen(true)
    }

    return <>
        <Popover open={password != undefined && anchor != undefined}
                 anchorEl={anchor}
                 onClose={close}
                 anchorOrigin={{vertical: "center", horizontal: "right"}}
                 transformOrigin={{vertical: "center", horizontal: "left"}}>
            <Box className="p-2 flex flex-col gap-2">
                <Button type="button"
                        variant="outlined"
                        startIcon={<Edit/>}
                        onClick={openEditDialog}>Edit password</Button>
                <Button type="button"
                        variant="outlined"
                        color="error"
                        startIcon={<DeleteForever/>}
                        onClick={openRemoveDialog}>Delete password</Button>
            </Box>
        </Popover>
        <PasswordEditDialog opened={editDialogOpen}
                            close={() => setEditDialogOpen(false)}
                            passwords={passwords}
                            setPasswords={setPasswords}
                            password={password}/>
        <PasswordDeleteDialog opened={deleteDialogOpen}
                              close={() => setDeleteDialogOpen(false)}
                              passwords={passwords}
                              setPasswords={setPasswords}
                              password={password}/>
    </>
}
