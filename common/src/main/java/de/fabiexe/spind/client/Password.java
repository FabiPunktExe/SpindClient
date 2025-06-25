package de.fabiexe.spind.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Password {
    private final @NotNull String name;
    private final @Nullable String email;
    private final @Nullable String phone;
    private final @NotNull String password;

    public Password(@NotNull String name, @Nullable String email, @Nullable String phone, @NotNull String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    // Record-style accessors
    public @NotNull String name() {
        return name;
    }

    public @Nullable String email() {
        return email;
    }

    public @Nullable String phone() {
        return phone;
    }

    public @NotNull String password() {
        return password;
    }

    // JavaBean-style getters for compatibility
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password1 = (Password) o;
        return name.equals(password1.name) && 
               Objects.equals(email, password1.email) && 
               Objects.equals(phone, password1.phone) && 
               password.equals(password1.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, phone, password);
    }

    @Override
    public String toString() {
        return "Password[" +
                "name=" + name + ", " +
                "email=" + email + ", " +
                "phone=" + phone + ", " +
                "password=" + password + ']';
    }
}
