package net.suqatri.commands;

import java.util.*;
import java.util.stream.Collectors;

public class ConsoleCommand extends BaseCommand {

    @Override
    public List<String> getCommandsForCompletion(CommandIssuer issuer, String[] args) {
        final Set<String> cmds = new HashSet<>();
        boolean finishedLastArg = args.length > 0 ? args[args.length-1].equals("<|>") : false;
        if(finishedLastArg) args = removeLastArgument(args);
        final int cmdIndex = Math.max(0, args.length - 1);
        String argString = Arrays.stream(args).parallel().collect(Collectors.joining(" ")).toLowerCase();
        for (Map.Entry<String, RegisteredCommand> entry : subCommands.entries()) {
            final String key = entry.getKey();
            if ((key.startsWith(argString) && !isSpecialSubcommand(key)) && (!finishedLastArg || !key.equalsIgnoreCase(argString))) {
                final RegisteredCommand value = entry.getValue();
                if (!value.hasPermission(issuer) || value.isPrivate) {
                    continue;
                }
                String[] split = value.prefSubCommand.split(" ");
                cmds.add(finishedLastArg && split.length > cmdIndex+1 ? split[cmdIndex + 1] : split[cmdIndex]);
            }
        }
        return new ArrayList<>(cmds);
    }

    private final String[] removeLastArgument(String[] args){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < args.length-1; i++) {
            if(i > 0) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString().split(" ");
    }
}
