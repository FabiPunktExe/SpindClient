import {Server} from "../api.ts"
import {ReactNode, useEffect, useState} from "react"
import SetupPage from "./SetupPage.tsx"
import PasswordsPage from "./PasswordsPage.tsx"
import UnlockPage from "./UnlockPage.tsx"
import {Alert, Snackbar} from "@mui/material"
import {AlertColor} from "@mui/material/Alert"

export default function ServerPage({server}: {server: Server}) {
    const [state, setState] = useState(0)
    const [alert, setAlert] = useState<{color: AlertColor, content: ReactNode} | undefined>(undefined)
    const [alertOpen, setAlertOpen] = useState(false)

    useEffect(() => {
        window.spind$isLocked(server).then(locked => {
            if (!locked && state == 0) {
                setState(2)
            }
        })
    }, [server, state])

    function onSetupRequired() {
        setState(1)
        setAlert({color: "info", content: "You need to set up your password safe"})
        setAlertOpen(true)
    }
    function onUnlockFail(error: string) {
        setAlert({color: "error", content: error})
        setAlertOpen(true)
    }
    function onSetupSuccess() {
        setState(0)
        setAlert({color: "success", content: "Your password safe was set up successfully"})
        setAlertOpen(true)
    }
    function onSetupFail(error: string) {
        setAlert({color: "error", content: error})
        setAlertOpen(true)
    }
    function onPasswordError(error: string) {
        setAlert({color: "error", content: error})
        setAlertOpen(true)
    }

    return <>
        {state == 0 && <UnlockPage server={server}
                                   onSuccess={() => setState(2)}
                                   onSetupRequired={onSetupRequired}
                                   onFail={onUnlockFail}/>}
        {state == 1 && <SetupPage server={server}
                                  onSuccess={onSetupSuccess}
                                  onFail={onSetupFail}/>}
        {state == 2 && <PasswordsPage server={server} onError={onPasswordError}/>}
        <Snackbar open={alertOpen}
                  autoHideDuration={7000}
                  onClose={() => setAlertOpen(false)}>
            <Alert onClose={() => setAlertOpen(false)}
                   severity={alert?.color}
                   variant="filled">
                {alert?.content}
            </Alert>
        </Snackbar>
    </>
}