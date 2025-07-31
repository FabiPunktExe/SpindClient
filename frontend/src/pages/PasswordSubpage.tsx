import {ContentCopy} from "@mui/icons-material"
import {Password} from "../api.ts"
import {Box, Button, TextField, Typography} from "@mui/material"

export default function PasswordSubpage({password}: {password: Password}) {
    async function copyPassword() {
        await window.spind$copyToClipboard(`Password for ${password.name}`, password.password)
    }

    return <Box className="flex flex-col gap-2 items-center">
        <Typography variant="h5">Credentials for {password.name}</Typography>
        <Box className="flex flex-col gap-2">
            {Object.keys(password.fields).map((name, key) =>
                <TextField key={key}
                           label={name}
                           value={password.fields[name]}
                           disabled={true}
                           variant="filled"/>)}
        </Box>
        <Button variant="contained" startIcon={<ContentCopy/>} onClick={copyPassword}>Copy Password</Button>
    </Box>
}
