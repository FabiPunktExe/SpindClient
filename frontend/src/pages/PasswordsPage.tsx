import {useEffect, useState, MouseEvent, ChangeEvent, ReactNode} from "react"
import {Password, Server} from "../api.ts"
import {Box, Button, Paper, Tab, Tabs, TextField, Typography} from "@mui/material"
import {Add, Save} from "@mui/icons-material"
import PasswordContextMenu from "../components/PasswordContextMenu.tsx"
import PasswordAddOrEditSubpage from "./PasswordAddOrEditSubpage.tsx"
import PasswordSubpage from "./PasswordSubpage.tsx"
import PasswordDeleteDialog from "../dialogs/PasswordDeleteDialog.tsx"

export default function PasswordsPage({server, onError}: {server: Server, onError: (error: string) => void}) {
    const [passwords, setPasswords] = useState<Password[]>([])
    const [searchQuery, setSearchQuery] = useState("")
    const [selectedTab, setSelectedTab] = useState<number | false>(false)
    const [subpage, setSubpage] = useState<ReactNode>(undefined)
    const [deleteDialog, setDeleteDialog] = useState<Password | undefined>(undefined)
    const [contextPassword, setContextPassword] = useState<Password | undefined>(undefined)
    const [contextMenuAnchor, setContextMenuAnchor] = useState<HTMLElement | undefined>(undefined)

    useEffect(() => {
        window.spind$getPasswords(server).then(passwords => {
            passwords.sort((a, b) => a.name.localeCompare(b.name))
            setPasswords(passwords)
        })
    }, [server])

    function changePasswordFilter(event: ChangeEvent<HTMLInputElement>) {
        setSearchQuery(event.target.value)
    }

    async function addPassword(password: Password) {
        const newPasswords = [...passwords, password]
        const result = await window.spind$setPasswords(server, newPasswords)
        if (result === true) {
            newPasswords.sort((a, b) => a.name.localeCompare(b.name))
            setPasswords(newPasswords)
            setSelectedTab(newPasswords.indexOf(password))
            setSubpage(<PasswordSubpage password={password}/>)
        } else {
            onError(result)
        }
    }

    async function updatePassword(oldPassword: Password, newPassword: Password) {
        const newPasswords = passwords.map(p => p === oldPassword ? newPassword : p)
        const result = await window.spind$setPasswords(server, newPasswords)
        if (result === true) {
            newPasswords.sort((a, b) => a.name.localeCompare(b.name))
            setPasswords(newPasswords)
            setSelectedTab(newPasswords.indexOf(newPassword))
            setSubpage(<PasswordSubpage password={newPassword}/>)
        } else {
            onError(result)
        }
    }

    async function deletePassword() {
        const newPasswords = passwords.filter(p => p !== deleteDialog)
        const result = await window.spind$setPasswords(server, newPasswords)
        if (result === true) {
            newPasswords.sort((a, b) => a.name.localeCompare(b.name))
            setPasswords(newPasswords)
            setSubpage(undefined)
        } else {
            onError(result)
        }
    }

    function openPasswordSubpage(tab: number) {
        setSelectedTab(tab)
        setSubpage(<PasswordSubpage password={passwords[tab]}/>)
    }

    function openPasswordAddSubpage() {
        setSelectedTab(false)
        setSubpage(<PasswordAddOrEditSubpage title="Add Password"
                                             buttonLabel="Add Passwword"
                                             buttonIcon={<Add/>}
                                             submit={addPassword}/>)
    }

    function openPasswordEditSubpage() {
        setSelectedTab(false)
        setSubpage(<PasswordAddOrEditSubpage title="Edit Password"
                                             buttonLabel="Save Password"
                                             buttonIcon={<Save/>}
                                             defaultPassword={contextPassword}
                                             submit={newPassword => updatePassword(contextPassword!, newPassword)}/>)
    }

    function openPasswordDeleteDialog() {
        setDeleteDialog(contextPassword)
    }

    const filteredPasswords = passwords.filter((password: Password) => {
        const name = password.name.toLowerCase()
        const parts = searchQuery.split(" ")
        for (const part of parts) {
            if (!name.includes(part.toLowerCase())) {
                return false
            }
        }
        return true
    })

    return <>
        <Paper className="sm:h-full grow p-2 flex max-sm:flex-col sm:flex-row gap-2">
            <Box className="max-sm:max-h-80 flex flex-col gap-2">
                <Button variant="contained" startIcon={<Add/>} onClick={openPasswordAddSubpage}>Add Password</Button>
                <TextField size="small"
                           placeholder="Search"
                           value={searchQuery}
                           onChange={changePasswordFilter}/>
                <Tabs value={selectedTab}
                      onChange={(_, tab) => openPasswordSubpage(parseInt(tab))}
                      orientation="vertical"
                      variant="scrollable">
                    {filteredPasswords.map((password, key) => {
                        function onContextMenu(event: MouseEvent<HTMLDivElement>) {
                            setContextPassword(password)
                            setContextMenuAnchor(event.currentTarget)
                        }
                        const label = <span className="w-full flex">{password.name}</span>
                        return <Tab key={key} value={key} label={label} onContextMenu={onContextMenu}/>
                    })}
                </Tabs>
                {searchQuery && filteredPasswords.length == 0 && <Typography color="textSecondary" align="center">
                    No passwords match your search
                </Typography>}
            </Box>
            <Box className="max-sm:hidden grow flex items-center justify-center">{subpage}</Box>
        </Paper>
        <Paper className="sm:hidden! grow p-2 flex items-center justify-center">{subpage}</Paper>
        <PasswordContextMenu contextPassword={contextPassword}
                             setContextPassword={setContextPassword}
                             anchor={contextMenuAnchor}
                             setAnchor={setContextMenuAnchor}
                             openPasswordEditSubpage={openPasswordEditSubpage}
                             openPasswordDeleteDialog={openPasswordDeleteDialog}/>
        <Box className="grow flex overflow-y-auto"><Box className="m-auto">{subpage}</Box></Box>
        <PasswordDeleteDialog opened={deleteDialog !== undefined}
                              close={() => setDeleteDialog(undefined)}
                              password={deleteDialog}
                              submit={deletePassword}/>
    </Paper>
}
