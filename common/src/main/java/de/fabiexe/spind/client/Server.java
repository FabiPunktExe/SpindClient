package de.fabiexe.spind.client;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class Server {
    private final @NotNull String name;
    private final @NotNull String address;
    private final @NotNull String username;

    public Server(@NotNull String name, @NotNull String address, @NotNull String username) {
        this.name = name;
        this.address = address;
        this.username = username;
    }

    // Record-style accessors
    public @NotNull String name() {
        return name;
    }

    public @NotNull String address() {
        return address;
    }

    public @NotNull String username() {
        return username;
    }

    // JavaBean-style getters for compatibility
    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getAddress() {
        return address;
    }

    public @NotNull String getUsername() {
        return username;
    }

    @Override
    public @NotNull String toString() {
        return username + "@" + address;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Server other && address.equals(other.address) && username.equals(other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, username);
    }
}
