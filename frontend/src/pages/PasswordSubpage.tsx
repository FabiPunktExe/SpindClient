import {ContentCopy, Link} from "@mui/icons-material"
import {Password} from "../api.ts"
import {Box, Button, IconButton, TextField, Typography} from "@mui/material"
import {useEffect, useState} from "react"

function TwoFactorAuthField({secret}: { secret: string }) {
    const [code, setCode] = useState<string | null>(null)

    async function copy() {
        if (code) {
            await window.spind$copyToClipboard("2FA Code", code)
        }
    }

    useEffect(() => {
        const fetchCode = () => {
            window.spind$generate2FACode(secret).then(setCode)
        }
        fetchCode()
        const interval = setInterval(fetchCode, 5000)
        return () => clearInterval(interval)
    }, [secret])

    return <Box className="flex flex-row gap-2 items-center">
        <TextField label="2FA Code"
                   value={code ?? "..."}
                   disabled={true}
                   variant="filled"
                   className="grow"/>
        <IconButton color="primary" onClick={copy} disabled={!code}>
            <ContentCopy/>
        </IconButton>
    </Box>
}

export default function PasswordSubpage({password}: {password: Password}) {
    async function copyPassword() {
        await window.spind$copyToClipboard(`Password for ${password.name}`, password.password)
    }

    return <Box className="sm:size-full flex flex-col gap-2 items-center justify-center">
        <Typography variant="h5">Credentials for {password.name}</Typography>
        <Box className="flex flex-col gap-2">
            {Object.keys(password.fields).map((name, key) => {
                if (name.toLowerCase() === "2fa secret") {
                    return <TwoFactorAuthField key={key} secret={password.fields[name]}/>
                }

                function openInBrowser() {
                    window.spind$openInBrowser(password.fields[name])
                }
                return <Box key={key} className="flex flex-row gap-2 items-center">
                    <TextField label={name}
                               value={password.fields[name]}
                               disabled={true}
                               variant="filled"
                               className="grow"/>
                    {name.toLowerCase() === "website" && <IconButton color="primary" onClick={openInBrowser}>
                        <Link/>
                    </IconButton>}
                </Box>
            })}
        </Box>
        <Button variant="contained" startIcon={<ContentCopy/>} onClick={copyPassword}>Copy Password</Button>
    </Box>
}
