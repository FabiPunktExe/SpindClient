package de.fabiexe.spind.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Password(@NotNull String name, @Nullable String email, @Nullable String phone, @NotNull String password) {}
