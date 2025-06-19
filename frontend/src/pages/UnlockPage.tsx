import {Server} from "../api.ts"
import {useState} from "react"
import {Button, CircularProgress, Paper, TextField, Typography} from "@mui/material"
import {LockOpen} from "@mui/icons-material"

export default function UnlockPage({server, onSuccess, onSetupRequired, onFail}: {
    server: Server
    onSuccess: () => void
    onSetupRequired: () => void
    onFail: (error: string) => void
}) {
    const [loading, setLoading] = useState(false)

    function submit(data: FormData) {
        const password = data.get("password") as string
        setLoading(true)
        window.spind$unlock(server, password).then(result => {
            setLoading(false)
            if (result === true) {
                onSuccess()
            } else if (result === false) {
                onSetupRequired()
            } else {
                onFail(result)
            }
        })
    }

    return <Paper component="form"
                  action={submit}
                  className="h-full grow p-2 flex flex-col gap-2 items-center justify-center">
        <Typography>This password safe is locked</Typography>
        <TextField name="password"
                   type="password"
                   label="Password"
                   autoComplete="off"
                   required={true}/>
        <Button type="submit" startIcon={<LockOpen/>} variant="contained">Unlock</Button>
        {loading && <CircularProgress/>}
        {loading && <Typography>Unlocking your password safe...</Typography>}
    </Paper>
}