package de.fabiexe.spind.client;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface GsonWebviewInteropFunction {
    /**
     * Processes a request from JavaScript and returns a result.
     *
     * @param args The arguments passed to the JavaScript function as a Gson JsonArray.
     * @return The result of the callback, which must be handleable by
     *         {@link com.google.gson.Gson#toJson(Object)}.
     * @throws Exception If an error occurs during processing, it will be caught
     *                   and returned as a JSON string.
     */
    @Nullable
    Object apply(@NotNull JsonArray args) throws Exception;
}