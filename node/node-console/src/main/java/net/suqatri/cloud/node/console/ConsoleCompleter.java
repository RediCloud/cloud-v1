package net.suqatri.cloud.node.console;

import net.suqatri.commands.console.ConsoleCommandIssuer;
import net.suqatri.commands.core.RootCommand;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.stream.Collectors;

public class ConsoleCompleter implements Completer {

    private final CommandConsoleManager consoleManager;

    public ConsoleCompleter(CommandConsoleManager consoleManager) {
        this.consoleManager = consoleManager;
    }

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        ConsoleCommandIssuer commandSender = this.consoleManager.getCommandIssuer(this.consoleManager.getCommandSender());
        String line = parsedLine.line();

        String name = line.split(" ")[0];
        String[] args = line.split(" ");
        if(args.length > 1) {
            args = removeFirstArguments(args, 1);
        } else {
            args = new String[0];
        }

        List<RootCommand> commands = this.consoleManager.getRegisteredRootCommands().parallelStream()
                .filter(rootCommand -> rootCommand.getCommandName().toLowerCase().startsWith(name.toLowerCase()))
                .collect(Collectors.toList());

        RootCommand matchedCommand = commands.parallelStream()
                .filter(rootCommand -> rootCommand.getCommandName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if(args.length == 0 && matchedCommand == null){
            for (RootCommand command : commands) {
                list.add(new Candidate(command.getCommandName()));
            }
            return;
        }

        if(matchedCommand == null) return;

        List<Candidate> candidates = matchedCommand
                .getTabCompletions(commandSender, matchedCommand.getCommandName(), line.endsWith(" ") && args.length != 0 ? addEmptyArgument(args) : args)
                .parallelStream()
                .filter(s -> !s.isEmpty())
                .filter(s -> !s.equals("<|>"))
                .map(Candidate::new)
                .collect(Collectors.toList());
        list.addAll(candidates);
    }

    private String[] removeFirstArguments(String[] args, int count){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < args.length; i++) {
            if(i >= count) {
                if(!sb.toString().isEmpty()) sb.append(" ");
                sb.append(args[i]);
            }
        }
        return sb.toString().split(" ");
    }

    private String[] addEmptyArgument(String[] args){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < args.length; i++) {
            if(!sb.toString().isEmpty()) sb.append(" ");
            sb.append(args[i]);
        }
        String toAppend = "";
        if(!sb.toString().isEmpty()) toAppend = " ";
        toAppend += "<|>";
        sb.append(toAppend);
        return sb.toString().split(" ");
    }
}
