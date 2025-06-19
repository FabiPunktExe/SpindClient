import {MouseEvent, useEffect, useState} from "react"
import {Server} from "./api.ts"
import {Box, Button, Paper, Tab, Tabs} from "@mui/material"
import {Add} from "@mui/icons-material"
import ServerAddDialog from "./dialogs/ServerAddDialog.tsx"
import ServerPage from "./pages/ServerPage.tsx"
import ServerMenu from "./components/ServerMenu.tsx"

export default function App() {
    const [servers, setServers] = useState<Server[]>([])
    const [selectedTab, setSelectedTab] = useState(0)
    const [serverAddDialogOpen, setServerAddDialogOpen] = useState(false)
    const [menuServer, setMenuServer] = useState<Server | undefined>(undefined)
    const [menuAnchor, setMenuAnchor] = useState<HTMLElement | undefined>(undefined)

    useEffect(() => {
        window.spind$getServers().then(servers => {
            servers.sort((a, b) => a.name.localeCompare(b.name))
            setServers(servers)
        })
    }, [])

    async function addServer(server: Server) {
        const newServers = [...servers, server]
        await window.spind$setServers(newServers)
        setServers(newServers)
        setSelectedTab(newServers.length - 1)
    }

    return <Box className="w-screen h-screen p-4 flex flex-row gap-4">
        <Paper className="w-max h-full p-2 flex flex-col gap-2">
            <Button variant="contained"
                    startIcon={<Add/>}
                    onClick={() => setServerAddDialogOpen(true)}
                    className="w-max">Add Spind Server</Button>
            <Tabs value={selectedTab}
                  onChange={(_, tab) => setSelectedTab(parseInt(tab))}
                  orientation="vertical"
                  variant="scrollable">
                {servers.map((server, key) => {
                    function onContextMenu(event: MouseEvent<HTMLDivElement>) {
                        setMenuServer(server)
                        setMenuAnchor(event.currentTarget)
                    }
                    return <Tab key={key} value={key} label={server.name} onContextMenu={onContextMenu}/>
                })}
            </Tabs>
        </Paper>
        {servers.map((server, key) => {
            if (key == selectedTab) {
                return <ServerPage key={key} server={server}/>
            } else {
                return <></>
            }
        })}
        <ServerAddDialog opened={serverAddDialogOpen}
                         close={() => setServerAddDialogOpen(false)}
                         addServer={addServer}/>
        <ServerMenu servers={servers}
                    setServers={async servers => {
                        await window.spind$setServers(servers)
                        setServers(servers)
                    }}
                    server={menuServer}
                    setServer={setMenuServer}
                    anchor={menuAnchor}
                    setAnchor={setMenuAnchor}/>
    </Box>
}
