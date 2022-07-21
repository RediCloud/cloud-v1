package net.suqatri.cloud.node.console;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.console.IConsole;
import net.suqatri.cloud.api.console.IConsoleLine;
import net.suqatri.cloud.api.console.IConsoleLineEntry;
import net.suqatri.cloud.api.console.LogLevel;
import net.suqatri.cloud.node.console.setup.Setup;
import org.fusesource.jansi.Ansi;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
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

    @Setter
    private boolean cleanConsoleMode = true;
    private final CommandConsoleManager consoleManager;
    private LogLevel logLevel = LogLevel.INFO;
    private final NodeConsoleThread thread;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private Terminal terminal;
    private final LineReader lineReader;
    private ConsoleCompleter consoleCompleter;
    @Setter
    private Setup currentSetup;
    private final List<IConsoleLineEntry> lineEntries;
    @Getter
    private String prefix = translateColorCodes("§b" + System.getProperty("user.name") + "§a@§cUnknownNode §f=> ");
    private String mainPrefix;
    private final Collection<Consumer<String>> inputHandler;

    private final String textColor = "§b";
    private final String highlightColor = "§f";
    private boolean colorsEnabled = true;

    public NodeConsole(CommandConsoleManager consoleManager) throws IOException {
        this.consoleManager = consoleManager;
        this.inputStream = System.in;
        this.outputStream = System.out;
        this.lineReader = createLineReader();
        this.thread = new NodeConsoleThread(this, "node");
        this.lineEntries = new ArrayList<>();
        this.inputHandler = new ArrayList<>();
        this.mainPrefix = prefix;
        this.startThread();
        if(this.logLevel.getId() <= LogLevel.DEBUG.getId()) this.cleanConsoleMode = false;
    }

    public void printCloudHeader(boolean printWarning){
        log(new ConsoleLine("", "     ").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", "§f     _______                 __   _      ______  __                         __  ").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", "§f    |_   __ \\               |  ] (_)   .' ___  |[  |                       |  ] ").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", "§f      | |__) |  .---.   .--.| |  __   / .'   \\_| | |  .--.   __   _    .--.| |  ").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", "§f      |  __ /  / /__\\\\/ /'`\\' | [  |  | |        | |/ .'`\\ \\[  | | | / /'`\\' |  ").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", "§f     _| |  \\ \\_| \\__.,| \\__/  |  | |  \\ `.___.'\\ | || \\__. | | \\_/ |,| \\__/  |  ").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", "§f    |____| |___|'.__.' '.__.;__][___]  `.____ .'[___]'.__.'  '.__.'_/ '.__.;__] ").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", "").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", this.textColor + "    A redis based cluster cloud system for Minecraft").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", "    §8» " + this.textColor + "Version: " + this.highlightColor + CloudAPI.getVersion() + " §8| " + this.textColor + "Discord: " + this.highlightColor +"https://discord.gg/g2HV52VV4G").setPrintPrefix(false).setPrintTimestamp(false));
        log(new ConsoleLine("", "     ").setPrintPrefix(false).setPrintTimestamp(false));
        if(this.cleanConsoleMode && printWarning){
            warn("§cClean console mode is enabled! Stacktraces will not be printed, only the message.");
            warn("§cTo disable this mode, set the property 'cleanConsoleMode' to false.");
        }
    }

    public void addInputHandler(Consumer<String> inputHandler) {
        this.inputHandler.add(inputHandler);
    }

    public void removeInputHandler(Consumer<String> inputHandler) {
        this.inputHandler.remove(inputHandler);
    }

    public void resetPrefix(){
        this.prefix = this.mainPrefix;
        this.redisplay();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.redisplay();
    }

    public void setMainPrefix(String mainPrefix) {
        this.mainPrefix = mainPrefix;
        this.redisplay();
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

    public void printForce(String prefix, String message){
        if(!message.endsWith(System.lineSeparator())) message += System.lineSeparator();

        message = message.replaceAll("%tc", this.textColor)
                .replaceAll("%hc", this.highlightColor);

        String dateTime = java.time.format.DateTimeFormatter.ofPattern("dd-MM HH:mm:ss:SSS").format(java.time.LocalDateTime.now());
        String p = "§7[§f" + dateTime + "§7] §f" + prefix + "§7: " + this.textColor;

        String line = this.colorsEnabled ? translateColorCodes(p + message) : ColorTranslator.removeColorCodes(p + message);
        this.lineReader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        this.lineReader.getTerminal().writer().print(Ansi.ansi().eraseLine(Ansi.Erase.ALL) + "\r" + line + Ansi.ansi().reset());
        this.lineReader.getTerminal().writer().flush();

        this.redisplay();
    }

    @Override
    public void log(IConsoleLine consoleLine){
        String message = consoleLine.getMessage();
        if(!message.endsWith(System.lineSeparator())) message += System.lineSeparator();

        if(consoleLine.isStored()) this.lineEntries.add(consoleLine);

        if(!canLog(consoleLine)) return;

        message = message.replaceAll("%tc", this.textColor)
                .replaceAll("%hc", this.highlightColor);

        String dateTime = "";
        if(consoleLine.printTimestamp()) {
            long time = consoleLine.getTime();
            dateTime = java.time.format.DateTimeFormatter.ofPattern("dd-MM HH:mm:ss:SSS").format(java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(time), java.time.ZoneId.systemDefault()));
            dateTime = "§7[§f" + dateTime + "§7] ";
        }
        String prefix = "";
        if(consoleLine.printPrefix()){
            prefix = "§f" + consoleLine.getPrefix() + "§7: " + (logLevel == LogLevel.INFO ? this.textColor : Ansi.ansi().a(Ansi.Attribute.RESET).fgRgb(consoleLine.getLogLevel().getColor().getRed(), consoleLine.getLogLevel().getColor().getGreen(), consoleLine.getLogLevel().getColor().getBlue()).toString());
        }

        String line = this.colorsEnabled ? translateColorCodes(dateTime + prefix + message) : ColorTranslator.removeColorCodes(dateTime + prefix + message);
        this.lineReader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        this.lineReader.getTerminal().writer().print(Ansi.ansi().eraseLine(Ansi.Erase.ALL) + "\r" + line + Ansi.ansi().reset());
        this.lineReader.getTerminal().writer().flush();

        this.redisplay();
    }

    public void log(LogLevel level, String message){
        this.log(new ConsoleLine(level, message));
    }

    private void redisplay() {
        if (!this.lineReader.isReading()) return;

        this.lineReader.callWidget(LineReader.REDRAW_LINE);
        this.lineReader.callWidget(LineReader.REDISPLAY);
    }

    @Override
    public void error(String message, Throwable throwable) {
        this.log(LogLevel.ERROR, message);
        if(this.cleanConsoleMode) return;
        this.log(LogLevel.ERROR, throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        for (StackTraceElement element : throwable.getStackTrace()) {
            this.log(LogLevel.ERROR, element.toString());
        }
    }

    @Override
    public void error(String message) {
        this.log(LogLevel.ERROR, message);
    }

    @Override
    public void warn(String message) {
        this.log(LogLevel.WARN, message);
    }

    @Override
    public void info(String message) {
        this.log(LogLevel.INFO, message);
    }

    @Override
    public void debug(String message) {
        this.log(LogLevel.DEBUG, message);
    }

    @Override
    public void trace(String message) {
        this.log(LogLevel.TRACE, message);
    }

    @Override
    public void fatal(String message, Throwable throwable) {
        this.log(LogLevel.FATAL, message);
        this.log(LogLevel.FATAL, throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        for (StackTraceElement element : throwable.getStackTrace()) {
            this.log(LogLevel.FATAL, element.toString());
        }
    }

    @Override
    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
        if(level.getId() <= LogLevel.DEBUG.getId()) this.cleanConsoleMode = false;
    }

    @Override
    public LogLevel getLogLevel() {
        return this.logLevel;
    }

    @Override
    public boolean canLog(IConsoleLine line) {
        if(this.currentSetup != null && (line.getLogLevel() != LogLevel.ERROR || line.getLogLevel() != LogLevel.FATAL)) return false;
        return IConsole.super.canLog(line);
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

    public void disableColors() {
        this.colorsEnabled = false;
        this.warn("Console colors are now diabled! Use this only for debugging purposes!");
    }
}
