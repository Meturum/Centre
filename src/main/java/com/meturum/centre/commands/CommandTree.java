package com.meturum.centre.commands;

import com.google.common.base.Preconditions;
import com.meturum.centra.ColorList;
import com.meturum.centre.commands.exceptions.CommandArgumentException;
import com.meturum.centre.commands.arguments.CommandArgument;
import com.meturum.centre.commands.arguments.CommandContext;
import com.meturum.centre.util.EmojiList;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandTree {

    private final String name;
    private @Nullable String description;
    private String[] aliases;
    private String permission;

    private @Nullable String usageMessage;

    private List<CommandArgument<?>> argumentList = new ArrayList<>();

    private CommandTree parent;
    private final List<CommandTree> nodeList = new ArrayList<>();

    private @Nullable ExecuteLambda executeLambda;

    private boolean allowConsoleExecution = false;

    public CommandTree(@NotNull String name, @Nullable String description, @Nullable String permission, String... aliases) {
        this.name = name.toLowerCase();
        this.description = description;
        this.permission = permission;
        this.aliases = aliases;
    }

    public CommandTree(@NotNull String name, @Nullable String description, @Nullable String permission) {
        this(name, description, permission, new String[0]);
    }

    public CommandTree(@NotNull String name, @Nullable String description) {
        this(name, description, null, new String[0]);
    }

    public CommandTree(@NotNull String name) {
        this(name, null, null, new String[0]);
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        if(description == null)return "";

        return description;
    }

    public final CommandTree setDescription(@Nullable String description) {
        this.description = description;

        return this;
    }

    public final String getPermission() {
        return permission;
    }

    public final CommandTree setPermission(@NotNull String permission) {
        this.permission = permission;

        return this;
    }

    public final String[] getAliases() {
        return aliases;
    }

    public final CommandTree setAliases(String... aliases) {
        Preconditions.checkNotNull(aliases, "Aliases cannot be null!");

        this.aliases = aliases;

        return this;
    }

    public final String getUsageMessage() {
        if(usageMessage != null) return usageMessage; // If the usage message is already set, return it.

        StringBuilder builder = new StringBuilder().append("/");

        // include the hierarchy in the command usage.
        List<CommandTree> hierarchyList = hierarchy();
        for (CommandTree commandTree : hierarchyList) {
            builder.append(commandTree.getName()).append(" ");
        }

        builder.append(name);

        for(CommandArgument<?> argument : argumentList) {
            builder.append(argument.isRequired()
                    ? " <" + argument.getName() + ":" + argument.getType().getSimpleName() + ">"
                    : " [" + argument.getName() + ":" + argument.getType().getSimpleName() + "]"
            );
        }

        return builder.toString();
    }

    public final void setUsageMessage(@Nullable String usageMessage) {
        this.usageMessage = usageMessage;
    }

    public final CommandArgument<?> getArgument(@NotNull String name) {
        for(CommandArgument<?> argument : argumentList) {
            if(argument.getName().equalsIgnoreCase(name))
                return argument;
        }

        return null;
    }

    public final CommandTree addArgument(@NotNull CommandArgument<?> argument) throws IllegalArgumentException {
        if(!verifyArguments(List.of(argument)))
            throw new IllegalArgumentException("Argument " + argument.getName() + " is already present in this command tree");

        argumentList.add(argument);
        return this;
    }

    public final CommandTree setArguments(CommandArgument<?>... arguments) throws IllegalArgumentException {
        Preconditions.checkNotNull(arguments, "Arguments cannot be null!");

        List<CommandArgument<?>> argumentList = List.of(arguments);

        if(!verifyArguments(argumentList))
            throw new IllegalArgumentException("Arguments must be unique");

        this.argumentList = List.of(arguments);
        return this;
    }

    /**
     * Verifies that all arguments are valid and ready to be executed.
     *
     * @return true if the arguments are secure, false otherwise.
     */
    private boolean verifyArguments(@NotNull List<CommandArgument<?>> argumentList) {
        for (CommandArgument<?> argument : argumentList) {
            for(CommandArgument<?> matched : this.argumentList) {
                if (argument == matched) return false;
                if (argument.getName().equals(matched.getName())) return false;
            }
        }

        return true;
    }

    public final @Nullable CommandTree getNode(@NotNull String name) {
        for(CommandTree node : nodeList) {
            if(!node.getName().equalsIgnoreCase(name)) continue;
            return node;
        }

        return null;
    }

    /**
     * @return returns true if the command tree has children nodes, false otherwise.
     */
    public final boolean containsNodes() {
        return !nodeList.isEmpty();
    }

    public final CommandTree branch(@NotNull CommandTree node) throws IllegalArgumentException {
        if(!verifyNodes(node))
            throw new IllegalArgumentException("Node " + node.getName() + " is already present in this command tree");

        if(node instanceof CommandTree) {
            ((CommandTree) node).parent = this;
            nodeList.add(node);
        }
        return this;
    }

    /**
     * @return the parent of the command tree.
     */
    public final CommandTree getParent() {
        return parent;
    }

    /**
     * @return the root of the command tree.
     */
    public final CommandTree root() {
        CommandTree parent = this.parent;

        while(parent.parent != null) {
            parent = parent.parent;
        }

        return parent;
    }

    /**
     * @return the list of all nodes in the command tree.
     */
    public final List<CommandTree> hierarchy() {
        List<CommandTree> hierarchy = new ArrayList<>();
        CommandTree parent = this.parent;

        while(parent != null) {
            hierarchy.add(parent);
            parent = parent.parent;
        }

        return hierarchy;
    }

    public final boolean isAllowConsoleExecution() {
        return allowConsoleExecution;
    }

    public final CommandTree setAllowConsoleExecution(boolean allowConsoleExecution) {
        this.allowConsoleExecution = allowConsoleExecution;

        return this;
    }

    public final CommandTree executes(@Nullable ExecuteLambda lambda) throws RuntimeException {
        if(executeLambda != null)
            throw new RuntimeException("Command " + name + " already has an execute lambda");

        executeLambda = lambda;
        return this;
    }

    /**
     * Verifies that all nodes are valid and ready to be executed.
     *
     * @return true if the nodes are secure, false otherwise.
     */
    private boolean verifyNodes(@NotNull CommandTree node) {
        for (CommandTree matched : nodeList) {
            if (node == matched) return false;
            if (node.getName().equals(matched.getName())) return false;
        }

        return true;
    }

    /**
     * Converts the command tree instance into a Bukkit command.
     *
     * @return the Bukkit command.
     */
    public final BukkitCommand asBukkitCommand() {
        return new BukkitCommand(name, getDescription(), "", List.of(aliases)) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
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
                        CommandTree root = CommandTree.this;
                        int index = 0;

                        for (String argument : args) {
                            // Check if the argument is a node
                            CommandTree node = root.getNode(argument);
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
                        CommandContext context = new CommandContext(root, sender, trueArguments);

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
                            for (CommandArgument<?> argument : root.argumentList) {
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
                        for (CommandArgument<?> argument : root.argumentList) {
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

                        if (root.executeLambda == null) {
                            // The command does not have an execute lambda, so we can assume that the command was executed incorrectly.
                            // Most likely, a command that doesn't have an execute lambda is a looking for a sub node.

                            // Check if the command has sub nodes
                            if (root.nodeList.isEmpty()) {
                                // The command does not have sub nodes, so we can assume that the command was executed incorrectly.
                                throw new CommandArgumentException(root.getUsageMessage(), CommandArgumentException.Level.WARNING);
                            }

                            // The command has sub nodes, so we can assume that an incorrect sub node was entered.
                            throw new CommandArgumentException("Unknown command: /" + root.getName() + " " + String.join(" ", context.getTrueArguments()));
                        }

                        // Even when the executeLambda is not null, the sender could still be attempting to access a sub node.
                        if (!root.nodeList.isEmpty() && context.size() == expectedArgumentsCount && args.length > expectedArgumentsCount)
                            // Since the node list (there is a sub node to access) is not empty, and the context size (all parameters set) is equal to eAC, and the args length is greater than eAC (all parameters + sub node), we can assume that the sender is attempting to access a sub node.
                            throw new CommandArgumentException("Unknown command: /" + root.getName() + " " + String.join(" ", context.getTrueArguments()));

                        if(sender instanceof Player || !root.isAllowConsoleExecution()) {
                            root.executeLambda.run(context);
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

                CommandTree root = CommandTree.this;
                for (String argument : args) {
                    CommandTree node = root.getNode(argument);
                    if (node == null) break;

                    root = node;
                }

                if (root.containsNodes()) { // The sender is attempting to access a sub node. (e.g. /command subnode)
                    for (CommandTree node : root.nodeList) {
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

                for(CommandArgument<?> commandArgument : root.argumentList) { // The sender is attempting to set a normal argument. (e.g. /command arg)
                    if (!commandArgument.getName().startsWith(context)) continue;
                    if(existingArgumentsList.contains(commandArgument.getName())) continue;
                    suggestionsList.add(commandArgument.getName()+":");
                }

                return suggestionsList;
            }
        };
    }
    
    public interface ExecuteLambda {
        void run(CommandContext context) throws CommandArgumentException;
    }

}