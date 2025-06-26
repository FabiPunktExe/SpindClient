package de.fabiexe.spind.client;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.Strictness;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpindJsApi {
    private static final Gson gson = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    private final ClipboardManager clipboardManager;

    public SpindJsApi(@NotNull ClipboardManager clipboardManager) {
        this.clipboardManager = clipboardManager;
    }

    @JavascriptInterface
    public String getServers() {
        return gson.toJson(Spind.getServers().toArray());
    }

    @JavascriptInterface
    public boolean setServers(@NotNull String serversJson) {
        try {
            Spind.setServers(gson.fromJson(serversJson, new TypeToken<>() {}));
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
}
