import {Password} from "../api.ts"
import {ChangeEvent, ReactNode, useState} from "react"
import {Autocomplete, Box, Button, IconButton, TextField, Typography} from "@mui/material"
import {AddCircle, Link, RemoveCircle, Shuffle} from "@mui/icons-material"

const defaultFields = ["Website", "Email", "Username", "Phone", "2FA Secret"]

export default function PasswordAddOrEditSubpage({title, buttonLabel, buttonIcon, defaultPassword, submit}: {
    title: string
    buttonLabel: string
    buttonIcon: ReactNode
    defaultPassword?: Password
    submit: (password: Password) => void
}) {
    const [password, setPassword] = useState(defaultPassword?.password || "")
    const [newField, setNewField] = useState("")
    const [fields, setFields] = useState<Password["fields"]>(defaultPassword?.fields || {})

    function action(data: FormData) {
        const name = data.get("name") as string
        submit({name, password, fields})
    }

    function removeField(name: string) {
        const newFields = {...fields}
        delete newFields[name]
        setFields(newFields)
    }

    function addField() {
        if (newField) {
            setFields({...fields, [newField]: ""})
            setNewField("")
        }
    }

    function generateRandomPassword() {
        const characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.:;-_'#+*!@$%^&()"
        let password = ""
        for (let i = 0; i < 16; i++) {
            password += characters.charAt(Math.floor(Math.random() * characters.length))
        }
        setPassword(password)
    }

    return <Box component="form" action={action} className="flex flex-col gap-2 items-center">
        <Typography variant="h5">{title}</Typography>
        <Box className="flex flex-col gap-2">
            <TextField name="name"
                       label="Display name"
                       required={true}
                       defaultValue={defaultPassword?.name}
                       autoComplete="off"
                       autoCorrect="off"/>
            <Box className="flex flex-row gap-2">
                <Autocomplete options={defaultFields.filter(defaultField => typeof fields[defaultField] === "undefined")}
                              freeSolo
                              renderInput={props => <TextField {...props} label="Add Field"/>}
                              value={newField}
                              onInputChange={(_, value) => setNewField(value)}
                              className="grow"/>
                <IconButton color="primary" onClick={addField}><AddCircle/></IconButton>
            </Box>
            {Object.keys(fields).map((name, key) => {
                function onChange(event: ChangeEvent<HTMLInputElement>) {
                    setFields({...fields, [name]: event.target.value})
                }

                return <Box key={key} className="flex flex-row gap-2 items-center">
                    <TextField className="grow"
                               label={name}
                               type={name.toLowerCase() === "website" ? "url" : "text"}
                               autoComplete="off"
                               autoCorrect="off"
                               value={fields[name]}
                               onChange={onChange}/>
                    {name.toLowerCase() === "website" &&
                        <IconButton color="primary" onClick={() => window.spind$openInBrowser(fields[name])}>
                            <Link/>
                        </IconButton>}
                    <IconButton color="error" onClick={() => removeField(name)}><RemoveCircle/></IconButton>
                </Box>
            })}
            <TextField name="password"
                       label="Password"
                       type="password"
                       value={password}
                       onChange={event => setPassword(event.target.value)}
                       required={true}
                       autoComplete="off"
                       autoCorrect="off"/>
            <Button type="button"
                    variant="outlined"
                    startIcon={<Shuffle/>}
                    onClick={generateRandomPassword}>Random password</Button>
        </Box>
        <Button type="submit" variant="contained" startIcon={buttonIcon}>{buttonLabel}</Button>
    </Box>
}
