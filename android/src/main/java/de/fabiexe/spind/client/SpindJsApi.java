package de.fabiexe.spind.client;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.Strictness;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public class SpindJsApi {
    private static final Gson gson = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    private final Context context;
    private final ClipboardManager clipboardManager;

    public SpindJsApi(@NotNull Context context, @NotNull ClipboardManager clipboardManager) {
        this.context = context;
        this.clipboardManager = clipboardManager;
    }

    @JavascriptInterface
    public String getServers() {
        return gson.toJson(Spind.getServers().toArray());
    }

    @JavascriptInterface
    public boolean setServers(@NotNull String serversJson) {
        try {
            Spind.setServers(gson.fromJson(serversJson, new TypeToken<List<Server>>() {}.getType()));
            return true;
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    @JavascriptInterface
    public boolean isLocked(@NotNull String serverJson) {
        try {
            Server server = gson.fromJson(serverJson, Server.class);
            return Spind.isLocked(server);
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return true;
        }
    }

    @JavascriptInterface
    public String unlock(@NotNull String serverJson, @NotNull String password) {
        try {
            Server server = gson.fromJson(serverJson, Server.class);
            boolean result = Spind.unlock(server, password);
            return gson.toJson(result);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            return gson.toJson(e.getMessage());
        }
    }

    @JavascriptInterface
    public void lock(@NotNull String serverJson) {
        try {
            Server server = gson.fromJson(serverJson, Server.class);
            Spind.lock(server);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
        }
    }

    @JavascriptInterface
    public String setup(@NotNull String serverJson, @NotNull String password) {
        try {
            Server server = gson.fromJson(serverJson, Server.class);
            Spind.setup(server, password);
            return gson.toJson(true);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            return gson.toJson(e.getMessage());
        }
    }

    @JavascriptInterface
    public String getPasswords(@NotNull String serverJson) {
        try {
            Server server = gson.fromJson(serverJson, Server.class);
            List<Password> passwords = Spind.getPasswords(server);
            return gson.toJson(passwords);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            return "[]";
        }
    }

    @JavascriptInterface
    public String setPasswords(@NotNull String serverJson, @NotNull String passwordsJson) {
        try {
            Server server = gson.fromJson(serverJson, Server.class);
            List<Password> passwords = gson.fromJson(passwordsJson, new TypeToken<List<Password>>() {}.getType());
            boolean result = Spind.setPasswords(server, passwords);
            return gson.toJson(result);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            return gson.toJson(e.getMessage());
        }
    }

    @JavascriptInterface
    public void copyToClipboard(@NotNull String label, @NotNull String text) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    @JavascriptInterface
    public void openInBrowser(@NotNull String url) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @JavascriptInterface
    public String generate2FACode(@NotNull String secret) {
        try {
            return String.format(Locale.getDefault(), "%06d", Spind.twoFA(secret));
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
}
