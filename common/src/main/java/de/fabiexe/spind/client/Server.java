package de.fabiexe.spind.client;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public record Server(@NotNull String name, @NotNull String address, @NotNull String username) {
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
