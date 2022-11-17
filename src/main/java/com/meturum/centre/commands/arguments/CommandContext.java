package com.meturum.centre.commands.arguments;

import com.meturum.centre.commands.CommandTree;
import com.meturum.centre.commands.exceptions.CommandArgumentException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public final class CommandContext {

    private final CommandTree node;
    private final CommandSender sender;
    private final String[] arguments;
    private final HashMap<String, CommandArgument<?>> contextMap = new HashMap<>();

    public CommandContext(@NotNull CommandTree node, @NotNull CommandSender sender, @NotNull String[] arguments) {
        this.node = node;
        this.sender = sender;
        this.arguments = arguments;
    }

    /**
     * The node that this context is for.
     *
     * @return The node.
     */
    public CommandTree getNode() {
        return node;
    }

    /**
     * The sender that executed the command.
     *
     * @return The sender.
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * The arguments that were passed to the command.
     *
     * @return The arguments.
     */
    public String[] getTrueArguments() {
        return arguments;
    }

    /**
     * @return The context map.
     */
    public int size() {
        return contextMap.size();
    }

    /**
     * Gets an argument from the context map.
     *
     * @param name The name of the argument.
     * @return The argument.
     */
    public CommandArgument<?> get(@NotNull String name) {
        return contextMap.get(name);
    }

    /**
     * Gets an argument value from the context map. (Allows for no casting on your part)
     *
     * @param name The name of the argument.
     * @return The argument value.
     * @param <T> The type of the argument.
     */
    public <T> T getArgument(@NotNull String name) {
        if(!contextMap.containsKey(name)) return null;
        return (T) get(name).getValue();
    }

    /**
     * Gets an argument value from the context map. (Allows for no casting on your part)
     *
     * @param name The name of the argument.
     * @return The argument value.
     * @param <T> The type of the argument.
     * @throws CommandArgumentException If the argument is not present.
     */
    public @NotNull <T> T getArgumentNonNull(@NotNull String name) throws CommandArgumentException {
        T value = getArgument(name);
        if(value == null)
            throw new CommandArgumentException("Missing Argument: " + name);

        return value;
    }

    /**
     * Gets an argument value from the context map. (Allows for no casting on your part)
     * This method allows for a default value to be returned if the argument is not present.
     *
     * @param name The name of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The argument value.
     * @param <T> The type of the argument.
     */
    public <T> T getArgumentOrDefault(@NotNull String name, @NotNull T defaultValue) {
        T value = getArgument(name);
        if(value == null)
            return defaultValue;

        return value;
    }

    /**
     * Sets an argument in the context map.
     *
     * @param value The value of the argument.
     */
    public void set(@NotNull CommandArgument<?> value) {
        contextMap.put(value.getName(), value);
    }

    /**
     * Imports multiple arguments into the context map.
     *
     * @param argumentList The list of arguments to import.
     */
    public void importArray(@NotNull List<CommandArgument<?>> argumentList) {
        for (CommandArgument<?> argument : argumentList) {
            set(argument);
        }
    }

    /**
     * @param name The name of the argument.
     * @return returns true if the argument exists in the context map, false otherwise.
     */
    public boolean containsArgument(@NotNull String name) {
        return contextMap.containsKey(name);
    }

}
