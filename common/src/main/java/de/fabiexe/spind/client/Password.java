package de.fabiexe.spind.client;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Password {
    private final String name;
    private final String username;
    private final String email;
    private final String phone;
    private final String password;

    public Password(@NotNull String name, @Nullable String username, @Nullable String email, @Nullable String phone, @NotNull String password) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public @NotNull String getName() {
        return name;
    }

    public @Nullable String getUsername() {
        return username;
    }

    public @Nullable String getEmail() {
        return email;
    }

    public @Nullable String getPhone() {
        return phone;
    }

    public @NotNull String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Password other &&
                name.equals(other.name) &&
                Objects.equals(username, other.username) &&
                Objects.equals(email, other.email) &&
                Objects.equals(phone, other.phone) &&
                password.equals(other.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, username, email, phone, password);
    }

    @Override
    public String toString() {
        return "Password[" +
                "name=" + name + ", " +
                "username=" + username + ", " +
                "email=" + email + ", " +
                "phone=" + phone + ", " +
                "password=" + password + ']';
    }

    public static class V1 {
        private final String name;
        private final String password;
        private final String phone;
        private final String url;

        public V1(@NotNull String name, @NotNull String password, @Nullable String phone, @Nullable String url) {
            this.name = name;
            this.password = password;
            this.phone = phone;
            this.url = url;
        }

        public @NotNull String getName() {
            return name;
        }

        public @NotNull String getPassword() {
            return password;
        }

        public @Nullable String getPhone() {
            return phone;
        }

        public @Nullable String getUrl() {
            return url;
        }
    }
}
