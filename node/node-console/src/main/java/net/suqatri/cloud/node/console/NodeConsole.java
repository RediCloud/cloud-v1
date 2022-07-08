package net.suqatri.cloud.node.console;

import lombok.Getter;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.console.LogLevel;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Getter
public class NodeConsole implements IConsole {

    private final CommandConsoleManager consoleManager;
    private LogLevel logLevel = LogLevel.DEBUG;
    private NodeConsoleThread thread;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private Terminal terminal;
    private final LineReader lineReader;
    private ConsoleCompleter consoleCompleter;

    public NodeConsole(CommandConsoleManager consoleManager) throws IOException {
        this.consoleManager = consoleManager;
        this.thread = new NodeConsoleThread(this, "node-console");
        this.inputStream = System.in;
        this.outputStream = System.out;
        this.lineReader = createLineReader();
    }

    public String getPrefix() {
        return "Test > ";
    }

    private LineReader createLineReader() throws IOException {
        this.terminal = TerminalBuilder.builder()
                .system(true)
                .streams(this.inputStream, this.outputStream)
                .encoding(StandardCharsets.UTF_8)
                .dumb(true)
                .build();

        this.consoleCompleter = new ConsoleCompleter(this.consoleManager);

        return LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(this.consoleCompleter)
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .option(LineReader.Option.AUTO_REMOVE_SLASH, false)
                .option(LineReader.Option.INSERT_TAB, false)
                .build();
    }

    private void startThread(){
        this.thread.start();
    }

    public void stopThread(){
        if(this.lineReader != null){
            this.lineReader.getTerminal().reader().shutdown();
            this.lineReader.getTerminal().pause();
        }
        if(this.thread != null){
            this.thread.interrupt();
        }
    }

    @Override
    public void log(LogLevel logLevel, String message) {
        if(!canLog(logLevel)) return;
        System.out.println(message);
    }

    @Override
    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    @Override
    public LogLevel getLogLevel() {
        return this.logLevel;
    }
}
