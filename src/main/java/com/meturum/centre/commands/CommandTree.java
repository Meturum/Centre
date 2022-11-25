package com.meturum.centre.commands;

import com.meturum.centra.ColorList;
import com.meturum.centra.commands.CommandBuilder;
import com.meturum.centra.commands.arguments.CommandArgument;
import com.meturum.centra.commands.exceptions.CommandArgumentException;
import com.meturum.centre.sessions.SessionFactoryImpl;
import com.meturum.centre.sessions.SessionImpl;
import com.meturum.centra.EmojiList;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is just a wrapper class for CommandBuilder. So we can hide the important code :)
 */
public record CommandTree(@NotNull CommandBuilder command) {

    public @NotNull BukkitCommand asBukkitCommand(@NotNull final SessionFactoryImpl sessionFactory) {
        return new BukkitCommand(command.getName(), command.getDescription(), "", List.of(command.getAliases())) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
                SessionImpl session = sessionFactory.search((Player) sender);
                if(session == null) return false;

                String[] trueArguments = args.clone();

                try {
                    try {
                        // Split the args by spaces and quotes, remove any empty strings
                        args = String.join(" ", args).split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                        args = Stream.of(args).filter(s -> !s.isEmpty()).toArray(String[]::new);

                        // Make sure that there are no arguments that are using prefixes.
                        String[] dupedArgs = args.clone();

                        List<String> prefixedArgumentList = new ArrayList<>();
                        for (int i = 0; i < args.length; i++) {
                            if (args[i].contains(":")) {
                                dupedArgs[i] = null; // remove the argument from the args array
                                prefixedArgumentList.add(args[i]); // add the argument to the prefix argument list, to be parsed later. (root node is required to parse prefixes)
                            }
                        }

                        // Remove all null values from the array
                        args = Stream.of(dupedArgs).filter(Objects::nonNull).toArray(String[]::new);

                        // find the root node of the command.
                        CommandBuilder root = command;
                        int index = 0;

                        for (String argument : args) {
                            // Check if the argument is a node
                            CommandBuilder node = root.getNode(argument);
                            if (node == null) break; // Break the loop, end of the tree.

                            // Set the root to the node
                            root = node;
                            index++;
                        }

                        setPermission(root.getPermission());
                        testPermission(sender);

                        // remove the root node from the args
                        args = Stream.of(args).skip(index).toArray(String[]::new);

                        // Create a new context
                        CommandContextImpl context = new CommandContextImpl(root, sender, session, trueArguments);

                        // Parse the prefixed arguments...
                        for (String prefixedArgument : prefixedArgumentList) {
                            String[] argArr = prefixedArgument.split(":");
                            if (argArr.length < 2) continue;

                            // get the argument and make sure prefix use is allowed.
                            CommandArgument<?> argument = root.getArgument(argArr[0]);
                            if (argument == null) continue;
                            if (!argument.isAllowPrefix()) continue;

                            // set the argument value
                            argument = argument.clone();
                            argument.setRawValue(argArr[1]);

                            context.set(argument);
                        }

                        // Now, parse the regular arguments of the command...
                        for (String stringArgument : args) {
                            for (CommandArgument<?> argument : root.getArguments()) {
                                if (context.get(argument.getName()) != null)
                                    continue; // Skip if the argument already has a value.
                                argument = argument.clone(); // We have clone the argument, so we can set the raw value without affecting the original argument. (this is important for the command tree to work properly)

                                argument.setRawValue(stringArgument);
                                context.set(argument);
                                break; // Break the loop, we have found the argument.
                            }
                        }

                        // Make sure that the arguments that are required are present.
                        List<CommandArgument<?>> missingArgumentsList = new ArrayList<>();
                        int expectedArgumentsCount = 0;
                        for (CommandArgument<?> argument : root.getArguments()) {
                            if (argument.isRequired())
                                expectedArgumentsCount++;
                            else continue;

                            if (context.get(argument.getName()) != null) continue;

                            // The argument is required, but it is not present in the context.
                            missingArgumentsList.add(argument);
                        }

                        if (!missingArgumentsList.isEmpty()) {
                            if (missingArgumentsList.size() != expectedArgumentsCount)
                                throw new CommandArgumentException("Missing required arguments: " + missingArgumentsList.stream().map(CommandArgument::getName).collect(Collectors.joining(", ")));

                            // If all arguments are missing, then we can assume that the command was executed incorrectly
                            throw new CommandArgumentException(root.getUsageMessage(), CommandArgumentException.Level.WARNING);
                        }

                        if (root.getExecuteLambda() == null) {
                            // The command does not have an execute lambda, so we can assume that the command was executed incorrectly.
                            // Most likely, a command that doesn't have an execute lambda is a looking for a sub node.

                            // Check if the command has sub nodes
                            if (root.getNodeList().isEmpty()) {
                                // The command does not have sub nodes, so we can assume that the command was executed incorrectly.
                                throw new CommandArgumentException(root.getUsageMessage(), CommandArgumentException.Level.WARNING);
                            }

                            // The command has sub nodes, so we can assume that an incorrect sub node was entered.
                            throw new CommandArgumentException("Unknown command: /" + root.getName() + " " + String.join(" ", context.getTrueArguments()));
                        }

                        // Even when the executeLambda is not null, the sender could still be attempting to access a sub node.
                        if (!root.getNodeList().isEmpty() && context.size() == expectedArgumentsCount && args.length > expectedArgumentsCount)
                            // Since the node list (there is a sub node to access) is not empty, and the context size (all parameters set) is equal to eAC, and the args length is greater than eAC (all parameters + sub node), we can assume that the sender is attempting to access a sub node.
                            throw new CommandArgumentException("Unknown command: /" + root.getName() + " " + String.join(" ", context.getTrueArguments()));

                        if(sender instanceof Player || !root.isAllowConsoleExecution()) {
                            root.getExecuteLambda().run(context);
                        }else sender.sendMessage(ColorList.RED+"Pleas execute this command in-game!");
                    } catch (CommandArgumentException exception) {
                        sender.sendMessage(exception.getMessage());
                    }
                } catch (Exception exception) {
                    sender.sendMessage(EmojiList.ERROR_ICON + " " + ColorList.RED + "Whoops! Looks like an exception occurred while executing this command.");
                    exception.printStackTrace();

                    return false;
                }

                return true;
            }

            @NotNull
            @Override
            public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                List<String> suggestionsList = new ArrayList<>();

                CommandBuilder root = command;
                for (String argument : args) {
                    CommandBuilder node = root.getNode(argument);
                    if (node == null) break;

                    root = node;
                }

                if (root.containsNodes()) { // The sender is attempting to access a sub node. (e.g. /command subnode)
                    for (CommandBuilder node : root.getNodeList()) {
                        if (!node.getName().startsWith(args[args.length - 1])) continue;
                        suggestionsList.add(node.getName());
                    }

                    return suggestionsList;
                }

                String context = args[args.length - 1];
                if (context.split(":").length == 2 || context.contains(":")) { // The sender is attempting to set a prefixed argument. (e.g. /command -arg:val)
                    String[] argArr = context.split(":");

                    CommandArgument<?> commandArgument = root.getArgument(argArr[0]);
                    if (commandArgument == null)
                        return suggestionsList; //  The argument doesn't exist. So we ignore it.
                    if (!commandArgument.isAllowPrefix())
                        return suggestionsList; // Prefix use is not allowed for this argument.

                    String context2 = "";
                    try {
                        context2 = argArr[1];
                    }catch (IndexOutOfBoundsException ignored) { } // We're just trying to get the context, so we can ignore this exception.

                    if(commandArgument.getSuggestionLambda() == null) return suggestionsList;
                    suggestionsList = commandArgument.getSuggestionLambda().run((Player) sender, commandArgument, context2);
                    suggestionsList.replaceAll(s -> commandArgument.getName() + ":" + s);

                    return suggestionsList;
                }

                // Make sure we aren't suggesting an argument that is already set.
                List<String> existingArgumentsList = new ArrayList<>();
                for(String argument : args) {
                    if(argument.contains(":")) {
                        String[] argArr = argument.split(":");
                        existingArgumentsList.add(argArr[0].toLowerCase());
                    }
                }

                for(CommandArgument<?> commandArgument : root.getArguments()) { // The sender is attempting to set a normal argument. (e.g. /command arg)
                    if (!commandArgument.getName().startsWith(context)) continue;
                    if(existingArgumentsList.contains(commandArgument.getName())) continue;
                    suggestionsList.add(commandArgument.getName()+":");
                }

                return suggestionsList;
            }
        };
    }

}
