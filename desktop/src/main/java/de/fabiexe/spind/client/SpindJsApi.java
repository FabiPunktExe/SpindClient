package de.fabiexe.spind.client;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class SpindJsApi {
    private static final Gson gson = new GsonBuilder().setStrictness(Strictness.LENIENT).create();

    public static List<Server> getServers(JsonArray params) {
        return Spind.getServers();
    }

    public static boolean setServers(JsonArray params) {
        try {
            return Spind.setServers(gson.fromJson(params.get(0), new TypeToken<>() {}));
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public static boolean isLocked(JsonArray params) {
        return Spind.isLocked(gson.fromJson(params.get(0), Server.class));
    }

    public static Object unlock(JsonArray params) {
        try {
            Server server = gson.fromJson(params.get(0), Server.class);
            String password = gson.fromJson(params.get(1), String.class);
            if (password == null || password.isEmpty()) {
                return false;
            }
            return Spind.unlock(server, password);
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return e.getMessage();
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    public static Object lock(JsonArray params) {
        try {
            Spind.lock(gson.fromJson(params.get(0), Server.class));
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    public static Object setup(JsonArray params) {
        try {
            Server server = gson.fromJson(params.get(0), Server.class);
            String password = gson.fromJson(params.get(1), String.class);
            if (server == null || password == null) {
                return "Invalid parameters";
            }
            Spind.setup(server, password);
            return true;
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return e.getMessage();
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    public static List<Password> getPasswords(JsonArray params) {
        try {
            return Spind.getPasswords(gson.fromJson(params.get(0), Server.class));
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return List.of();
        }
    }

    public static Object setPasswords(JsonArray params) {
        try {
            Server server = gson.fromJson(params.get(0), Server.class);
            List<Password> passwords = gson.fromJson(params.get(1), new TypeToken<List<Password>>() {}.getType());
            if (server == null || passwords == null) {
                return "Invalid parameters";
            }
            return Spind.setPasswords(server, passwords);
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return e.getMessage();
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    public static Object copyToClipboard(JsonArray params) {
        try {
            String text = params.get(1).getAsString();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    public static Object openInBrowser(JsonArray params) {
        try {
            String url = params.get(0).getAsString();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (JsonSyntaxException | IOException | URISyntaxException ignored) {}
        return null;
    }

    public static String generate2FACode(JsonArray params) {
        try {
            return String.valueOf(Spind.twoFA(params.get(0).getAsString()));
        } catch (JsonSyntaxException ignored) {}
        return null;
    }
}
