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
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String developmentServer;
        Options options = new Options();
        options.addOption(Option.builder()
                .option("h")
                .longOpt("help")
                .desc("Show help").build());
        options.addOption(Option.builder()
                .option("d")
                .longOpt("development-server")
                .hasArg()
                .argName("url")
                .desc("The development server's url")
                .build());
        try {
            CommandLine commandLine = new DefaultParser().parse(options, args);
            if (commandLine.hasOption("help")) {
                new HelpFormatter().printHelp("spind [OPTIONS]", options);
                return;
            }
            developmentServer = commandLine.getOptionValue("development-server");
        } catch (ParseException | NumberFormatException ignored) {
            new HelpFormatter().printHelp("[OPTIONS]", options);
            return;
        }

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

        StringBuilder html = new StringBuilder();
        if (developmentServer == null) {
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
        }

        // Delete temp dir
        FileUtils.deleteDirectory(tempDir.toFile());
        Runtime.getRuntime().removeShutdownHook(shutdownHook);

        // Start Webview
        WebviewStandalone webview = new WebviewStandalone(true);
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
        webview.setSize(screenSize.width / 2, screenSize.height / 2);
        if (developmentServer == null) {
            webview.setHtml(html.toString());
        } else {
            webview.navigate(developmentServer);
        }
        webview.run();
        webview.close();
    }
}
