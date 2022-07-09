package net.suqatri.cloud.node.console;

import net.suqatri.commands.RootCommand;
import org.jline.reader.UserInterruptException;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NodeConsoleThread extends Thread {

    private final NodeConsole nodeConsole;

    public NodeConsoleThread(NodeConsole nodeConsole, String name) {
        super(name + "-console-thread");
        this.nodeConsole = nodeConsole;
    }

    @Override
    public void run() {
        String line;
        try {
            while(!Thread.currentThread().isInterrupted()){
                line = this.nodeConsole.getLineReader().readLine(this.nodeConsole.getPrefix());
                handleInput(line);
            }
        }catch (UserInterruptException | IllegalStateException e){
            e.printStackTrace();
        }
    }

    public void handleInput(String raw){
        String line = raw;

        if(line == null) return;
        if(line.isEmpty()) return;



        while(line.startsWith(" ")){
            line = line.substring(1);
        }

        for (Consumer<String> inputHandler : this.nodeConsole.getInputHandler()) {
            inputHandler.accept(line);
        }

        if(this.nodeConsole.getCurrentSetup() != null) return;

        String name = line.split(" ")[0];
        String[] args = line.split(" ");
        if(args.length > 1) {
            String[] args1 = new String[args.length - 1];
            System.arraycopy(args, 1, args1, 0, args1.length);
            args = args1;
        } else {
            args = new String[0];
        }

        List<RootCommand> commands = this.nodeConsole.getConsoleManager().getRegisteredRootCommands().parallelStream()
                .filter(rootCommand -> rootCommand.getCommandName().startsWith(name))
                .collect(Collectors.toList());

        for (RootCommand command : commands) {
            if(command.getCommandName().equalsIgnoreCase(name)) {
                command.execute(this.nodeConsole.getConsoleManager().getCommandIssuer(this.nodeConsole.getConsoleManager().getCommandSender()), name, args);
                return;
            }
        }
    }
}
