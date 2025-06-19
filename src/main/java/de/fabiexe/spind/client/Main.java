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

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("spind");
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

        StringBuilder html = new StringBuilder();
        try (Stream<Path> paths = Files.walk(tempDir)) {
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
        try (Stream<Path> paths = Files.walk(tempDir)) {
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

        WebviewStandalone webview = new WebviewStandalone(false);
        GsonWebviewInterop interop = new GsonWebviewInterop(webview.getWebview());
        interop.bind("spind$getServers", Spind::getServers);
        interop.bind("spind$setServers", Spind::setServers);
        interop.bind("spind$isLocked", Spind::isLocked);
        interop.bind("spind$setup", Spind::setup);
        interop.bind("spind$unlock", Spind::unlock);
        interop.bind("spind$lock", Spind::lock);
        interop.bind("spind$getPasswords", Spind::getPasswords);
        interop.bind("spind$setPasswords", Spind::setPasswords);
        webview.setTitle("Spind");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        webview.setSize(screenSize.width / 2, screenSize.height / 2);
        webview.setHtml(html.toString());
        webview.run();
        webview.close();
    }
}
