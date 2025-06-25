package de.fabiexe.spind.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.notjustanna.webview.WebviewCore;
import net.notjustanna.webview.interop.WebviewReflectionInterop;
import net.notjustanna.webview.interop.callback.WebviewInteropFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GsonWebviewInterop extends WebviewReflectionInterop<GsonWebviewInterop, GsonWebviewInteropFunction> {
    private final Gson gson = new Gson();

    public GsonWebviewInterop(WebviewCore webview) {
        super(webview);
    }

    @Override
    protected @Nullable String handleResult(Object res) {
        return gson.toJson(res);
    }

    @Override
    protected @NotNull Object[] handleArgs(@NotNull String req, @NotNull Class<?>[] params) {
        JsonArray json = gson.toJsonTree(req).getAsJsonArray();
        if (json.size() != params.length) {
            throw new IllegalArgumentException("Invalid number of arguments");
        }
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            JsonElement node = json.get(i);
            if (node.isJsonNull()) {
                continue;
            }
            args[i] = gson.fromJson(node, params[i]);
        }
        return args;
    }

    @Override
    protected @NotNull WebviewInteropFunction wrap(@NotNull GsonWebviewInteropFunction callback) {
        return req -> handleResult(callback.apply(gson.fromJson(req, JsonArray.class)));
    }

    @Override
    protected @NotNull String errorToJson(@NotNull Throwable e) {
        e.printStackTrace(System.err);
        JsonObject errorObject = new JsonObject();
        errorObject.addProperty("error", e.getMessage());
        return errorObject.toString();
    }
}