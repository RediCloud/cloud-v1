package net.suqatri.cloud.node.console;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.console.LogLevel;
import net.suqatri.cloud.node.console.setup.Setup;
import org.fusesource.jansi.Ansi;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AnsiWriter;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

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
    @Setter
    private Setup currentSetup;
    private List<String> allWroteLines;
    @Getter
    private String prefix = translateColorCodes("Test > ");
    @Setter
    private String mainPrefix;
    private Collection<Consumer<String>> inputHandler;

    public NodeConsole(CommandConsoleManager consoleManager) throws IOException {
        this.consoleManager = consoleManager;
        this.inputStream = System.in;
        this.outputStream = System.out;
        this.lineReader = createLineReader();
        this.thread = new NodeConsoleThread(this, "node-console");
        this.allWroteLines = new ArrayList<>();
        this.inputHandler = new ArrayList<>();
        this.mainPrefix = prefix;
        this.startThread();
    }

    public void addInputHandler(Consumer<String> inputHandler) {
        this.inputHandler.add(inputHandler);
    }

    public void removeInputHandler(Consumer<String> inputHandler) {
        this.inputHandler.remove(inputHandler);
    }

    public void resetPrefix(){
        this.prefix = this.mainPrefix;
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
                .terminal(this.terminal)
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
        print(message);
    }

    @Override
    public void print(String message) {
        if(!message.endsWith(System.lineSeparator())) message += System.lineSeparator();

        this.allWroteLines.add(message);

        String s = translateColorCodes(message);
        this.lineReader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        this.lineReader.getTerminal().writer().print(Ansi.ansi().eraseLine(Ansi.Erase.ALL) + "\r" + s + Ansi.ansi().reset());
        this.lineReader.getTerminal().writer().flush();

        this.redisplay();
    }

    private void redisplay() {
        if (!this.lineReader.isReading()) return;

        this.lineReader.callWidget(LineReader.REDRAW_LINE);
        this.lineReader.callWidget(LineReader.REDISPLAY);
    }

    @Override
    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    @Override
    public LogLevel getLogLevel() {
        return this.logLevel;
    }

    @Override
    public void clearScreen() {
        this.terminal.puts(InfoCmp.Capability.clear_screen);
        this.terminal.flush();
    }

    public void setCommandInput(String input){
        this.lineReader.getBuffer().write(input);
    }

    @Override
    public String translateColorCodes(String message) {
        return ColorTranslator.translate(message);
    }

}
