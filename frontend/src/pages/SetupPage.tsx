import {Server} from "../api.ts"
import {Button, CircularProgress, Paper, TextField, Typography} from "@mui/material"
import {Lock} from "@mui/icons-material"
import {useState} from "react"

export default function SetupPage({server, onSuccess, onFail}: {server: Server, onSuccess: () => void, onFail?: () => void}) {
    const [loading, setLoading] = useState(false)

    function submit(data: FormData) {
        const password = data.get("password") as string
        const repeatPassword = data.get("repeat-password") as string
        if (password !== repeatPassword) {
            alert("Passwords do not match")
            return
        }

        setLoading(true)
        window.spind$setup(server, password).then(result => {
            setLoading(false)
            if (result === true) {
                onSuccess()
            } else {
                alert(result)
                if (onFail) {
                    onFail()
                }
            }
        })
    }

    return <Paper component="form"
                  action={submit}
                  className="h-full grow p-2 flex flex-col gap-2 items-center justify-center">
        <Typography>This password safe has not been setup yet</Typography>
        <TextField name="password"
                   type="password"
                   label="Password"
                   autoComplete="off"
                   required={true}/>
        <TextField name="repeat-password"
                   type="password"
                   label="Repeat Password"
                   autoComplete="off"
                   required={true}/>
        <Button type="submit" startIcon={<Lock/>} variant="contained">Setup</Button>
        {loading && <CircularProgress/>}
        {loading && <Typography>Setting up your password safe...</Typography>}
    </Paper>
}
