package de.fabiexe.spind.client;

import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Password {
    private final String name;
    private final String password;
    private final Map<String, String> fields;

    public Password(@NotNull String name, @NotNull String password, @NotNull Map<String, String> fields) {
        this.name = name;
        this.password = password;
        this.fields = fields;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public @NotNull Map<String, String> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Password other &&
                name.equals(other.name) &&
                password.equals(other.password) &&
                fields.equals(other.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, password, fields);
    }

    @Override
    public String toString() {
        return "Password[" +
                "name=" + name + ", " +
                "password=" + password + ", " +
                "fields=" + fields + "]";
    }

    public static class V1 {
        private final String name;
        private final String email;
        private final String phone;
        private final String password;

        public V1(@NotNull String name, @Nullable String email, @Nullable String phone, @NotNull String password) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.password = password;
        }

        public @NotNull String getName() {
            return name;
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
    }

    public static class V2 {
        private final String name;
        private final String username;
        private final String email;
        private final String phone;
        private final String password;

        public V2(@NotNull String name,
                  @Nullable String username,
                  @Nullable String email,
                  @Nullable String phone,
                  @NotNull String password) {
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
    }
}
