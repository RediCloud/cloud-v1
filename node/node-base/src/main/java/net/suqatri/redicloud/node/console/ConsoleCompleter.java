package net.suqatri.redicloud.node.console;

import net.suqatri.redicloud.commons.reflection.ReflectionUtils;
import net.suqatri.redicloud.node.console.setup.Setup;
import net.suqatri.redicloud.node.console.setup.SetupEntry;
import net.suqatri.redicloud.node.console.setup.SetupSuggester;
import net.suqatri.commands.ConsoleCommandIssuer;
import net.suqatri.commands.RootCommand;
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
        String line = parsedLine.line();

        Setup<?> currentSetup = consoleManager.getNodeConsole().getCurrentSetup();
        if(currentSetup != null){
            SetupEntry entry = currentSetup.getSetup().getValue();
            if(entry.getCompleter() != null){
                Class<? extends SetupSuggester> value = entry.getCompleter().value();
                SetupSuggester suggester = ReflectionUtils.createEmpty(value);
                for (String s : suggester.suggest(currentSetup, currentSetup.getSetup().getValue())) {
                    list.add(new Candidate(s));
                }
            }
            return;
        }

        ConsoleCommandIssuer commandSender = this.consoleManager.getCommandIssuer(this.consoleManager.getCommandSender());

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
