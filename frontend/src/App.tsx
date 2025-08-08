import {MouseEvent, useEffect, useState} from "react"
import {Server} from "./api.ts"
import {Box, Button, IconButton, Paper, SwipeableDrawer, Tab, Tabs, Typography} from "@mui/material"
import {Add, Menu} from "@mui/icons-material"
import ServerAddDialog from "./dialogs/ServerAddDialog.tsx"
import ServerPage from "./pages/ServerPage.tsx"
import ServerContextMenu from "./components/ServerContextMenu.tsx"

export default function App() {
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
    const [servers, setServers] = useState<Server[]>([])
    const [selectedTab, setSelectedTab] = useState<number | false>(false)
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
        newServers.sort((a, b) => a.name.localeCompare(b.name))
        setServers(newServers)
        setSelectedTab(newServers.length - 1)
    }

    function tabChanged(_: any, tab: string) {
        setSelectedTab(parseInt(tab))
        setMobileMenuOpen(false)
    }

    const serverTabs = <>
        <Button variant="contained"
                startIcon={<Add/>}
                onClick={() => setServerAddDialogOpen(true)}
                className="w-max">Add Spind Server</Button>
        <Tabs value={selectedTab}
              onChange={tabChanged}
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
    </>

    return <Box className="max-sm:size-full sm:w-screen sm:h-screen flex flex-col">
        <IconButton onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                    className="sm:hidden! left-1 top-1 w-fit">
            <Menu htmlColor="#ffffff"/>
        </IconButton>
        <Typography variant="h5" className="sm:hidden absolute top-1 left-1/2 -translate-x-1/2 text-center">
            Spind
        </Typography>
        <SwipeableDrawer open={mobileMenuOpen}
                         onOpen={() => setMobileMenuOpen(true)}
                         onClose={() => setMobileMenuOpen(false)}
                         variant={selectedTab === false ? "permanent" : "temporary"}
                         ModalProps={{keepMounted: true}}
                         className="sm:hidden">
            <Box className="p-2 flex flex-col gap-2">{serverTabs}</Box>
        </SwipeableDrawer>
        <Box className="h-full max-sm:p-2 sm:p-4 flex max-sm:flex-col sm:flex-row max-sm:gap-2 sm:gap-4">
            <Paper className="max-sm:hidden w-max h-full p-2 flex flex-col gap-2">{serverTabs}</Paper>
            {servers.map((server, key) => {
                if (key === selectedTab) {
                    return <ServerPage key={key} server={server}/>
                } else {
                    return <></>
                }
            })}
        </Box>
        <ServerAddDialog opened={serverAddDialogOpen}
                         close={() => setServerAddDialogOpen(false)}
                         addServer={addServer}/>
        <ServerContextMenu servers={servers}
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
