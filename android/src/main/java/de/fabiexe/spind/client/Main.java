package de.fabiexe.spind.client;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Main extends ComponentActivity {
    private static final String TAG = "spind";
    private Path tempDir;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Spind.directory = getBaseContext().getDataDir().toPath();

            // Create temp dir
            tempDir = Files.createTempDirectory("spind");
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
            IOUtils.copy(in, out);
            in.close();
            out.close();
            new ProcessBuilder("tar", "-xf", tempDir.resolve("frontend.tar").toString())
                    .directory(tempDir.toFile())
                    .inheritIO()
                    .start()
                    .waitFor();

            WebView webView = new WebView(getBaseContext());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAllowFileAccess(true);
            webView.setWebChromeClient(new SpindChromeClient());
            webView.setWebViewClient(new SpindWebViewClient());
            webView.addJavascriptInterface(new SpindJsApi(this, getSystemService(ClipboardManager.class)), "spind");
            webView.loadUrl(tempDir.resolve("dist/index.html").toString());
            setContentView(webView);
            getActionBar().hide();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    private static class SpindChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage message) {
            if (message.messageLevel() == ConsoleMessage.MessageLevel.DEBUG) {
                Log.d("BrowserConsole", message.message());
            } else if (message.messageLevel() == ConsoleMessage.MessageLevel.LOG) {
                Log.i("BrowserConsole", message.message());
            } else if (message.messageLevel() == ConsoleMessage.MessageLevel.WARNING) {
                Log.w("BrowserConsole", message.message());
            } else if (message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                Log.e("BrowserConsole", message.message());
            }
            return super.onConsoleMessage(message);
        }
    }

    private class SpindWebViewClient extends WebViewClient {
        @Override
        public @Nullable WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Path file = tempDir.resolve("dist" + request.getUrl().getPath());
            if (request.getMethod().equalsIgnoreCase("get") && Files.exists(file)) {
                try {
                    String extension = MimeTypeMap.getFileExtensionFromUrl(file.toString());
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    return new WebResourceResponse(mimeType, null, Files.newInputStream(file));
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            return super.shouldInterceptRequest(view, request);
        }
    }
}
