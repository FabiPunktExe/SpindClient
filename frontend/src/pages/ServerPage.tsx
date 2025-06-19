import {Server} from "../api.ts"
import {useEffect, useState} from "react"
import SetupPage from "./SetupPage.tsx"
import PasswordsPage from "./PasswordsPage.tsx"
import UnlockPage from "./UnlockPage.tsx"

export default function ServerPage({server}: {server: Server}) {
    const [state, setState] = useState(0)

    useEffect(() => {
        window.spind$isLocked(server).then(locked => {
            if (!locked && state == 0) {
                setState(2)
            }
        })
    }, [server, state])

    if (state == 0) {
        return <UnlockPage server={server}
                           onSuccess={() => setState(2)}
                           onSetupRequired={() => setState(1)}/>
    } else if (state == 1) {
        return <SetupPage server={server} onSuccess={() => setState(0)}/>
    } else {
        return <PasswordsPage server={server}/>
    }
}