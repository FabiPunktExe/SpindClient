import {StrictMode} from "react"
import {createRoot} from "react-dom/client"
import "./index.css"
import App from "./App.tsx"
import {createTheme, ThemeProvider} from "@mui/material"
import {registerAndroidApi} from "./api-android.ts"

if (/android/i.test(navigator.userAgent)) {
    registerAndroidApi()
}

window.addEventListener("contextmenu", event => {
    event.preventDefault()
})

const theme = createTheme({
    palette: {
        background: {
            default: "#0a0a0a",
            paper: "#1e1e1e"
        },
        primary: {
            main: "#dc7814"
        },
        secondary: {
            main: "#32c81e"
        },
        text: {
            primary: "#ffffff",
            secondary: "#bbbbbb",
            disabled: "#bbbbbb"
        },
        action: {
            disabled: "#bbbbbb",
            disabledBackground: "#666666"
        }
    }
})

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <ThemeProvider theme={theme}>
            <App/>
        </ThemeProvider>
    </StrictMode>
)
