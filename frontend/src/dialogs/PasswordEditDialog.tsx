import {Cancel, Edit, Shuffle} from "@mui/icons-material"
import {Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField} from "@mui/material"
import {Password} from "../api.ts"
import {FormEvent, useState} from "react"

export default function PasswordEditDialog({opened, close, passwords, setPasswords, password}: {
    opened: boolean
    close: () => void
    passwords: Password[]
    setPasswords: (passwords: Password[]) => void
    password?: Password
}) {
    const [passwordValue, setPasswordValue] = useState(password?.password)

    function onSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        const data = new FormData(event.currentTarget)
        const name = data.get("name") as string
        const email = data.get("email") as string || undefined
        const phone = data.get("phone") as string || undefined

        const index = passwords.findIndex(p => p == password)
        if (index < 0) {
            return
        }
        const newPasswords = [...passwords]
        newPasswords[index] = {name, email, phone, password: passwordValue || ""}
        setPasswords(newPasswords)
    }

    function generateRandomPassword() {
        const characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.:;-_'#+*!@$%^&()"
        let password = ""
        for (let i = 0; i < 16; i++) {
            password += characters.charAt(Math.floor(Math.random() * characters.length))
        }
        setPasswordValue(password)
    }

    return <Dialog open={opened}
                   onClose={close}
                   slotProps={{paper: {component: "form", onSubmit}}}>
        <DialogTitle>Edit Password</DialogTitle>
        <DialogContent className="flex flex-col gap-2">
            <TextField name="name"
                       label="Display name"
                       required={true}
                       defaultValue={password?.name}
                       autoComplete="off"
                       autoCorrect="off"
                       className="mt-1.5!"/>
            <TextField name="email"
                       label="Associated email address"
                       defaultValue={password?.email}
                       autoComplete="off"
                       autoCorrect="off"/>
            <TextField name="phone"
                       label="Associated phone number"
                       defaultValue={password?.phone}
                       autoComplete="off"
                       autoCorrect="off"/>
            <TextField name="password"
                       label="Password"
                       type="password"
                       value={passwordValue}
                       onChange={event => setPasswordValue(event.target.value)}
                       required={true}
                       autoComplete="off"
                       autoCorrect="off"/>
            <Button type="button" startIcon={<Shuffle/>} onClick={generateRandomPassword}>Random password</Button>
        </DialogContent>
        <DialogActions>
            <Button type="button" variant="outlined" startIcon={<Cancel/>} onClick={close}>Cancel</Button>
            <Button type="submit" variant="contained" startIcon={<Edit/>}>Edit Password</Button>
        </DialogActions>
    </Dialog>
}
