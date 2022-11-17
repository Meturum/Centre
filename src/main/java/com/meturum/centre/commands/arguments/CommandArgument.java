package com.meturum.centre.commands.arguments;

import com.meturum.centra.ColorList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class CommandArgument<T> {

    private final String name;
    private final Class<T> type;

    private @Nullable String description;

    private boolean required = true;
    private boolean allowPrefix = true;

    private @Nullable SuggestionLambda suggestionLambda;

    private @Nullable T value;

    public CommandArgument(@NotNull String name, @NotNull Class<T> type, @Nullable String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public CommandArgument(@NotNull String name, @NotNull Class<T> type) {
        this(name, type, null);
    }

    public final @NotNull String getName() {
        return name;
    }

    public final @NotNull Class<T> getType() {
        return type;
    }

    public final @Nullable String getDescription() {
        return description;
    }

    public final CommandArgument<T> setDescription(@Nullable String description) {
        this.description = description;

        return this;
    }

    public final boolean isRequired() {
        return required;
    }

    public final CommandArgument<T> setRequired(boolean required) {
        this.required = required;

        return this;
    }

    public final boolean isAllowPrefix() { // questionable naming scheme
        return allowPrefix;
    }

    public final CommandArgument<T> setAllowPrefix(boolean allowPrefix) {
        this.allowPrefix = allowPrefix;

        return this;
    }

    public final @Nullable T getValue() {
        return value;
    }

    /**
     * Sets the value of the argument.
     *
     * @param value The value to set.
     */
    public final void setValue(@Nullable T value) {
        this.value = value;
    }

    /**
     * Sets the value of the argument, uses{@link #convert(String)} to parse a string into the correct type.
     *
     * @param raw The string to convert.
     */
    public final void setRawValue(@NotNull String raw) {
        this.value = convert(raw);
    }

    /**
     * Converts a string into the correct type.
     *
     * @param raw The string to convert.
     * @return The converted value. If the value is not supported, null is returned.
     */
    protected T convert(@NotNull String raw) {
        try {
            return switch (getType().getSimpleName()) {
                case "String" -> (T) raw;
                case "Integer" -> (T) Integer.valueOf(raw);
                case "Double" -> (T) Double.valueOf(raw);
                case "Float" -> (T) Float.valueOf(raw);
                case "Boolean" -> (T) Boolean.valueOf(raw);
                case "Long" -> (T) Long.valueOf(raw);
                case "Short" -> (T) Short.valueOf(raw);
                case "Byte" -> (T) Byte.valueOf(raw);
                case "ColorList" -> (T) ColorList.of(raw);
                case "Player" -> (T) Bukkit.getPlayer(raw);
                default -> null;
            };
        }catch (Exception exception) {
            return null;
        }
    }

    /**
     * @return The suggestion lambda for the argument.
     */
    public final @Nullable SuggestionLambda getSuggestionLambda() {
        return suggestionLambda;
    }

    public final CommandArgument<T> suggests(@Nullable SuggestionLambda lambda) {
        if(suggestionLambda != null)
            throw new RuntimeException("Command " + name + " already has an suggestion lambda");

        suggestionLambda = lambda;
        return this;
    }

    @Override
    public CommandArgument<T> clone() throws CloneNotSupportedException {
        return (CommandArgument<T>) super.clone();
    }

    public interface SuggestionLambda {
        List<String> run(Player player, CommandArgument<?> argument, String context);
    }

}
