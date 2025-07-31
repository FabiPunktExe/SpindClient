import {Password} from "../api.ts"
import {Box, Button, Popover} from "@mui/material"
import {DeleteForever, Edit} from "@mui/icons-material"

export default function PasswordContextMenu({contextPassword, setContextPassword, anchor, setAnchor, openPasswordEditSubpage, openPasswordDeleteDialog}: {
    contextPassword?: Password
    setContextPassword: (password?: Password) => void
    anchor?: HTMLElement
    setAnchor: (anchor?: HTMLElement) => void
    openPasswordEditSubpage: () => void
    openPasswordDeleteDialog: () => void
}) {
    function close() {
        setContextPassword(undefined)
        setAnchor(undefined)
    }
    function openEditSubpage() {
        setAnchor(undefined)
        openPasswordEditSubpage()
    }
    function openDeleteDialog() {
        setAnchor(undefined)
        openPasswordDeleteDialog()
    }

    return <Popover open={contextPassword != undefined && anchor != undefined}
                    anchorEl={anchor}
                    onClose={close}
                    anchorOrigin={{vertical: "center", horizontal: "right"}}
                    transformOrigin={{vertical: "center", horizontal: "left"}}>
        <Box className="p-2 flex flex-col gap-2">
            <Button type="button"
                    variant="outlined"
                    startIcon={<Edit/>}
                    onClick={openEditSubpage}>Edit password</Button>
            <Button type="button"
                    variant="outlined"
                    color="error"
                    startIcon={<DeleteForever/>}
                    onClick={openDeleteDialog}>Delete password</Button>
        </Box>
    </Popover>
}
