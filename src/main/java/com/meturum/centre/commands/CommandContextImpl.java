package com.meturum.centre.commands;

import com.meturum.centra.commands.CommandBuilder;
import com.meturum.centra.commands.arguments.CommandArgument;
import com.meturum.centra.commands.arguments.CommandContext;
import com.meturum.centra.commands.exceptions.CommandArgumentException;
import com.meturum.centre.sessions.SessionImpl;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public final class CommandContextImpl implements CommandContext {

    private final CommandBuilder node;
    private final CommandSender sender;
    private final SessionImpl session;
    private final String[] arguments;
    private final HashMap<String, CommandArgument<?>> contextMap = new HashMap<>();

    public CommandContextImpl(@NotNull final CommandBuilder node, @NotNull final CommandSender sender, @Nullable final SessionImpl session, @NotNull final String[] arguments) {
        this.node = node;
        this.sender = sender;
        this.session = session;
        this.arguments = arguments;
    }

    public @NotNull CommandBuilder getNode() {
        return node;
    }

    public @NotNull CommandSender getSender() {
        return sender;
    }

    public @Nullable SessionImpl getSession() {
        return session;
    }

    public @NotNull String[] getTrueArguments() {
        return arguments;
    }

    public int size() {
        return contextMap.size();
    }

    public @Nullable CommandArgument<?> get(@NotNull final String name) {
        return contextMap.get(name);
    }

    public @Nullable <T> T getArgument(@NotNull final String name) {
        if(!contextMap.containsKey(name)) return null;
        return (T) get(name).getValue();
    }

    public @NotNull <T> T getArgumentNonNull(@NotNull final String name) throws CommandArgumentException {
        T value = getArgument(name);
        if(value == null)
            throw new CommandArgumentException("Missing Argument: " + name);

        return value;
    }

    public @NotNull <T> T getArgumentOrDefault(@NotNull final String name, @NotNull final T defaultValue) {
        T value = getArgument(name);
        if(value == null)
            return defaultValue;

        return value;
    }

    public void set(@NotNull final CommandArgument<?> value) {
        contextMap.put(value.getName(), value);
    }

    public void importArray(@NotNull final List<CommandArgument<?>> argumentList) {
        for (CommandArgument<?> argument : argumentList) {
            set(argument);
        }
    }

    public boolean containsArgument(@NotNull final String name) {
        return contextMap.containsKey(name);
    }

}
