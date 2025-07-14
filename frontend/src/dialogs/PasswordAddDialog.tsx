import {Add, Cancel, Shuffle} from "@mui/icons-material"
import {Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField} from "@mui/material"
import {Password} from "../api.ts"
import {FormEvent, useState} from "react"

export default function PasswordAddDialog({opened, close, addPassword}: {
    opened: boolean
    close: () => void
    addPassword: (password: Password) => Promise<void>
}){
    const [password, setPassword] = useState("")

    function onSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        const data = new FormData(event.currentTarget)
        const name = data.get("name") as string
        const username = data.get("username") as string || undefined
        const email = data.get("email") as string || undefined
        const phone = data.get("phone") as string || undefined
        addPassword({name, username, email, phone, password}).then(() => {
            setPassword("")
            close()
        })
    }

    function generateRandomPassword() {
        const characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.:;-_'#+*!@$%^&()"
        let password = ""
        for (let i = 0; i < 16; i++) {
            password += characters.charAt(Math.floor(Math.random() * characters.length))
        }
        setPassword(password)
    }

    return <Dialog open={opened}
                   onClose={close}
                   slotProps={{paper: {component: "form", onSubmit}}}>
        <DialogTitle>Add Password</DialogTitle>
        <DialogContent className="flex flex-col gap-2">
            <TextField name="name"
                       label="Display name"
                       required={true}
                       autoComplete="off"
                       autoCorrect="off"
                       className="mt-1.5!"/>
            <TextField name="username"
                       label="Associated username"
                       autoComplete="off"
                       autoCorrect="off"/>
            <TextField name="email"
                       label="Associated email address"
                       autoComplete="off"
                       autoCorrect="off"/>
            <TextField name="phone"
                       label="Associated phone number"
                       autoComplete="off"
                       autoCorrect="off"/>
            <TextField name="password"
                       label="Password"
                       type="password"
                       value={password}
                       onChange={event => setPassword(event.target.value)}
                       required={true}
                       autoComplete="off"
                       autoCorrect="off"/>
            <Button type="button" startIcon={<Shuffle/>} onClick={generateRandomPassword}>Random password</Button>
        </DialogContent>
        <DialogActions>
            <Button type="button" variant="outlined" startIcon={<Cancel/>} onClick={close}>Cancel</Button>
            <Button type="submit" variant="contained" startIcon={<Add/>}>Add Password</Button>
        </DialogActions>
    </Dialog>
}
