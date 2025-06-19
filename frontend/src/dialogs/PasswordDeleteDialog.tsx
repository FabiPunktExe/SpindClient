import {Password} from "../api.ts"
import {Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography} from "@mui/material"
import {Cancel, DeleteForever} from "@mui/icons-material"

export default function PasswordDeleteDialog({opened, close, passwords, setPasswords, password}: {
    opened: boolean
    close: () => void
    passwords: Password[]
    setPasswords: (passwords: Password[]) => Promise<void>
    password?: Password
}) {
    function deletePassword() {
        setPasswords(passwords.filter(p => p != password)).then(close)
    }

    return <Dialog open={opened} onClose={close}>
        <DialogTitle>Delete password</DialogTitle>
        <DialogContent className="flex flex-col gap-2">
            <Typography>Are you sure you want to delete the password <strong>{password?.name}</strong>?</Typography>
            <Typography>This cannot be undone.</Typography>
        </DialogContent>
        <DialogActions>
            <Button type="button" variant="outlined" startIcon={<Cancel/>} onClick={close}>Cancel</Button>
            <Button type="button" variant="contained" startIcon={<DeleteForever/>} onClick={deletePassword}>Delete password</Button>
        </DialogActions>
    </Dialog>
}
