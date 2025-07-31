import {Password} from "../api.ts"
import {Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography} from "@mui/material"
import {Cancel, DeleteForever} from "@mui/icons-material"
import {useState} from "react"

export default function PasswordDeleteDialog({opened, close, password, submit}: {
    opened: boolean
    close: () => void
    password?: Password
    submit: () => Promise<void>
}) {
    const [loading, setLoading] = useState(false)

    function onClick() {
        setLoading(true)
        submit().then(() => {
            setLoading(false)
            close()
        })
    }
    return <Dialog open={opened} onClose={close}>
        <DialogTitle>Delete password</DialogTitle>
        <DialogContent className="flex flex-col gap-2">
            <Typography>Are you sure you want to delete the password <strong>{password?.name}</strong>?</Typography>
            <Typography>This cannot be undone.</Typography>
        </DialogContent>
        <DialogActions>
            <Button type="button"
                    variant="outlined"
                    startIcon={<Cancel/>}
                    loading={loading}
                    onClick={close}>Cancel</Button>
            <Button type="button"
                    variant="contained"
                    startIcon={<DeleteForever/>}
                    loading={loading}
                    onClick={onClick}>Delete password</Button>
        </DialogActions>
    </Dialog>
}
