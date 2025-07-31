package de.fabiexe.spind.client;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.stream.Stream;
import net.notjustanna.webview.WebviewStandalone;
import org.apache.commons.io.FileUtils;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String developmentServer = System.getenv("SPIND_DEVELOPMENT_SERVER");
        String enableDevTools = System.getenv("SPIND_ENABLE_DEVTOOLS");

        StringBuilder html = new StringBuilder();
        if (developmentServer == null) {
            // Create temp dir
            Path tempDir = Files.createTempDirectory("spind");
            Thread shutdownHook = new Thread(() -> {
                try {
                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            // Extract frontend
            OutputStream out = Files.newOutputStream(tempDir.resolve("frontend.tar"), StandardOpenOption.CREATE_NEW);
            InputStream in = Objects.requireNonNull(Main.class.getResourceAsStream("/frontend.tar"));
            in.transferTo(out);
            in.close();
            out.close();
            new ProcessBuilder("tar", "-xf", tempDir.resolve("frontend.tar").toString())
                    .directory(tempDir.toFile())
                    .inheritIO()
                    .start()
                    .waitFor();

            // Read HTML, CSS, and JS files
            try (Stream<Path> paths = Files.walk(tempDir.resolve("dist"))) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".html") || path.toString().endsWith(".css"))
                        .forEach(path -> {
                            try {
                                String content = Files.readString(path);
                                if (path.getFileName().toString().endsWith(".html")) {
                                    html.append(content).append("\n");
                                } else if (path.getFileName().toString().endsWith(".css")) {
                                    html.append("<style>\n").append(content).append("\n</style>\n");
                                }
                            } catch (IOException e) {
                                e.printStackTrace(System.err);
                            }
                        });
            }
            try (Stream<Path> paths = Files.walk(tempDir.resolve("dist"))) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".js"))
                        .forEach(path -> {
                            try {
                                String content = Files.readString(path);
                                html.append("<script>\n").append(content).append("\n</script>\n");
                            } catch (IOException e) {
                                e.printStackTrace(System.err);
                            }
                        });
            }

            // Delete temp dir
            FileUtils.deleteDirectory(tempDir.toFile());
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }

        // Start Webview
        WebviewStandalone webview = new WebviewStandalone("true".equals(enableDevTools));
        GsonWebviewInterop interop = new GsonWebviewInterop(webview.getWebview());
        interop.bind("spind$getServers", SpindJsApi::getServers);
        interop.bind("spind$setServers", SpindJsApi::setServers);
        interop.bind("spind$isLocked", SpindJsApi::isLocked);
        interop.bind("spind$setup", SpindJsApi::setup);
        interop.bind("spind$unlock", SpindJsApi::unlock);
        interop.bind("spind$lock", SpindJsApi::lock);
        interop.bind("spind$getPasswords", SpindJsApi::getPasswords);
        interop.bind("spind$setPasswords", SpindJsApi::setPasswords);
        interop.bind("spind$copyToClipboard", SpindJsApi::copyToClipboard);
        webview.setTitle("Spind");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        webview.setSize(screenSize.width * 2/3, screenSize.height * 2/3);
        if (developmentServer == null) {
            webview.setHtml(html.toString());
        } else {
            webview.navigate(developmentServer);
        }
        webview.run();
        webview.close();
    }
}
