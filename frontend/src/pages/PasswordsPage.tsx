import {useEffect, useState, MouseEvent} from "react"
import {Password, Server} from "../api.ts"
import {Box, Button, Paper, Tab, Tabs, TextField, Typography} from "@mui/material"
import {Add, ContentCopy} from "@mui/icons-material"
import PasswordAddDialog from "../dialogs/PasswordAddDialog.tsx"
import PasswordMenu from "../components/PasswordMenu.tsx"

function PasswordDisplay({password}: {password: Password}) {
    async function copyPassword() {
        await navigator.clipboard.writeText(password.password)
    }
    return <Box className="h-full grow flex flex-col gap-2 items-center justify-center">
        <Typography variant="h5">Credentials for {password.name}</Typography>
        {password.email && <TextField label="Associated email address"
                                      defaultValue={password.email}
                                      disabled={true}
                                      variant="filled"/>}
        {password.phone && <TextField label="Associated phone number"
                                      defaultValue={password.phone}
                                      disabled={true}
                                      variant="filled"/>}
        <Button variant="contained" startIcon={<ContentCopy/>} onClick={copyPassword}>Copy password</Button>
    </Box>
}

export default function PasswordsPage({server}: {server: Server}) {
    const [passwords, setPasswords] = useState<Password[]>([])
    const [selectedTab, setSelectedTab] = useState(0)
    const [passwordAddDialogOpen, setPasswordAddDialogOpen] = useState(false)
    const [menuPassword, setMenuPassword] = useState<Password | undefined>(undefined)
    const [menuAnchor, setMenuAnchor] = useState<HTMLElement | undefined>(undefined)

    useEffect(() => {
        window.spind$getPasswords(server).then(passwords => {
            passwords.sort((a, b) => a.name.localeCompare(b.name))
            setPasswords(passwords)
        })
    }, [server])

    async function addPassword(password: Password) {
        const newPasswords = [...passwords, password]
        window.spind$setPasswords(server, newPasswords).then(result => {
            if (result === true) {
                setPasswords(newPasswords)
                setSelectedTab(newPasswords.length - 1)
            } else {
                alert(result)
            }
        })
    }

    return <Paper className="h-full grow p-2 flex flex-row gap-2">
        <Box className="flex flex-col gap-2">
            <Button variant="contained"
                    startIcon={<Add/>}
                    onClick={() => setPasswordAddDialogOpen(true)}>Add Password</Button>
            <Tabs value={selectedTab}
                  onChange={(_, tab) => setSelectedTab(parseInt(tab))}
                  orientation="vertical"
                  variant="scrollable">
                {passwords.map((password, key) => {
                    function onContextMenu(event: MouseEvent<HTMLDivElement>) {
                        setMenuPassword(password)
                        setMenuAnchor(event.currentTarget)
                    }
                    return <Tab key={key} value={key} label={password.name} onContextMenu={onContextMenu}/>
                })}
            </Tabs>
        </Box>
        {passwords.map((password, key) => {
            if (key == selectedTab) {
                return <PasswordDisplay key={key} password={password}/>
            } else {
                return <></>
            }
        })}
        <PasswordAddDialog opened={passwordAddDialogOpen}
                           close={() => setPasswordAddDialogOpen(false)}
                           addPassword={addPassword}/>
        <PasswordMenu passwords={passwords}
                      setPasswords={async passwords => {
                          const result = await window.spind$setPasswords(server, passwords)
                          if (result === true) {
                              setPasswords(passwords)
                          } else {
                              alert(result)
                          }
                      }}
                      password={menuPassword}
                      setPassword={setMenuPassword}
                      anchor={menuAnchor}
                      setAnchor={setMenuAnchor}/>
    </Paper>
}
