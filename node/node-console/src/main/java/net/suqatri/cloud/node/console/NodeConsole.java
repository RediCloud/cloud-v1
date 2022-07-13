package net.suqatri.cloud.node.console;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.CloudAPI;
import net.suqatri.cloud.api.console.IConsole;
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
    private String prefix = translateColorCodes("§b" + System.getProperty("user.name") + "§a@§cUnknownNode §f=> ");
    private String mainPrefix;
    private Collection<Consumer<String>> inputHandler;

    private String textColor = "§b";
    private String highlightColor = "§f";

    public NodeConsole(CommandConsoleManager consoleManager) throws IOException {
        this.consoleManager = consoleManager;
        this.inputStream = System.in;
        this.outputStream = System.out;
        this.lineReader = createLineReader();
        this.thread = new NodeConsoleThread(this, "node");
        this.allWroteLines = new ArrayList<>();
        this.inputHandler = new ArrayList<>();
        this.mainPrefix = prefix;
        this.startThread();
        if(canLog(LogLevel.DEBUG)) this.cleanConsoleMode = false;
    }

    public void printCloudHeader(){
        clearScreen();
        printRaw("    ", true, true);
        printRaw("    ", true, true);
        printRaw("§f     ▄████▄   ██▓     ▒█████   █    ██ ▓█████▄ ", true, true);
        printRaw("§f    ▒██▀ ▀█  ▓██▒    ▒██▒  ██▒ ██  ▓██▒▒██▀ ██▌", true, true);
        printRaw("§f    ▒▓█    ▄ ▒██░    ▒██░  ██▒▓██  ▒██░░██   █▌", true, true);
        printRaw("§f    ▒▓▓▄ ▄██▒▒██░    ▒██   ██░▓▓█  ░██░░▓█▄   ▌", true, true);
        printRaw("§f    ▒ ▓███▀ ░░██████▒░ ████▓▒░▒▒█████▓ ░▒████▓ ", true, true);
        printRaw("§f    ░ ░▒ ▒  ░░ ▒░▓  ░░ ▒░▒░▒░ ░▒▓▒ ▒ ▒  ▒▒▓  ▒ ", true, true);
        printRaw("§f      ░  ▒   ░ ░ ▒  ░  ░ ▒ ▒░ ░░▒░ ░ ░  ░ ▒  ▒ ", true, true);
        printRaw("§f    ░          ░ ░   ░ ░ ░ ▒   ░░░ ░ ░  ░ ░  ░ ", true, true);
        printRaw("§f    ░ ░          ░  ░    ░ ░     ░        ░    ", true, true);
        printRaw("§f    ░                                   ░      ", true, true);
        printRaw("    ", true, true);
        printRaw("    ", true, true);
        printRaw(this.textColor + "    A cluster based cloud system for Minecraft", true, true);
        printRaw("    §8» " + this.textColor + "Version: " + this.highlightColor + CloudAPI.getVersion() + " §8| " + this.textColor + "Discord: " + this.highlightColor +"https://discord.gg/vPwUhbVu4Y", true, true);
        printRaw("    §8» " + this.textColor + "System: " + this.highlightColor + System.getProperty("os.name") + " §8| " + this.textColor + "Java: " + this.highlightColor + System.getProperty("java.version"), true, true);
        printRaw("    ", true, true);
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

    @Override
    public void log(LogLevel level, String message, boolean translateColorCodes, boolean storeInHistory) {
        if(!canLog(level)) return;
        if(!message.endsWith(System.lineSeparator())) message += System.lineSeparator();

        message = message.replaceAll("%tc", this.textColor)
                .replaceAll("%hc", this.highlightColor);

        if(storeInHistory) this.allWroteLines.add(message);

        if(this.currentSetup != null && level != LogLevel.FATAL) return;

        String dateTime = java.time.format.DateTimeFormatter.ofPattern("dd-MM HH:mm:ss:SSS").format(java.time.LocalDateTime.now());
        String prefix = "§7[§f" + dateTime + "§7] §f" + level.name() + "§7: " + (logLevel == LogLevel.INFO ? this.textColor : Ansi.ansi().a(Ansi.Attribute.RESET).fgRgb(level.getColor().getRed(), level.getColor().getGreen(), level.getColor().getBlue()).toString());

        String line = translateColorCodes ? translateColorCodes(prefix + message) : prefix + message;
        this.lineReader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        this.lineReader.getTerminal().writer().print(Ansi.ansi().eraseLine(Ansi.Erase.ALL) + "\r" + line + Ansi.ansi().reset());
        this.lineReader.getTerminal().writer().flush();

        this.redisplay();
    }

    public void commandResponse(String message){
        if(!message.endsWith(System.lineSeparator())) message += System.lineSeparator();

        message = message.replaceAll("%tc", this.textColor)
                .replaceAll("%hc", this.highlightColor);

        this.allWroteLines.add(message);

        if(this.currentSetup != null) return;

        String dateTime = java.time.format.DateTimeFormatter.ofPattern("dd-MM HH:mm:ss:SSS").format(java.time.LocalDateTime.now());
        String prefix = "§7[§f" + dateTime + "§7] §fCOMMAND§7: " + this.textColor;

        String s = translateColorCodes(prefix + message);
        this.lineReader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        this.lineReader.getTerminal().writer().print(Ansi.ansi().eraseLine(Ansi.Erase.ALL) + "\r" + s + Ansi.ansi().reset());
        this.lineReader.getTerminal().writer().flush();

        this.redisplay();
    }

    @Override
    public void error(String message, Throwable throwable) {
        message = message.replaceAll("%tc", this.textColor)
                .replaceAll("%hc", this.highlightColor);
        if(this.cleanConsoleMode) {
            log(LogLevel.ERROR, message);
        } else {
            log(LogLevel.ERROR, message);
            log(LogLevel.ERROR, throwable.getMessage());
            log(LogLevel.ERROR, throwable.getStackTrace());
        }
    }

    @Override
    public void log(LogLevel level, Object[] messages) {
        if(this.currentSetup != null && logLevel != LogLevel.FATAL) return;
        IConsole.super.log(level, messages);
    }

    @Override
    public void log(LogLevel logLevel, String message) {
        if(this.currentSetup != null && logLevel != LogLevel.FATAL) return;
        IConsole.super.log(logLevel, message);
    }

    @Override
    public void print(String message) {
        message = message.replaceAll("%tc", this.textColor)
                .replaceAll("%hc", this.highlightColor);
        this.log(LogLevel.INFO, message);
    }

    public void printForce(String prefix, String message){
        if(!message.endsWith(System.lineSeparator())) message += System.lineSeparator();

        message = message.replaceAll("%tc", this.textColor)
                .replaceAll("%hc", this.highlightColor);

        String dateTime = java.time.format.DateTimeFormatter.ofPattern("dd-MM HH:mm:ss:SSS").format(java.time.LocalDateTime.now());
        String p = "§7[§f" + dateTime + "§7] §f" + prefix + "§7: " + this.textColor;

        String line = translateColorCodes(p + message);
        this.lineReader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        this.lineReader.getTerminal().writer().print(Ansi.ansi().eraseLine(Ansi.Erase.ALL) + "\r" + line + Ansi.ansi().reset());
        this.lineReader.getTerminal().writer().flush();

        this.redisplay();
    }

    @Override
    public void printRaw(String message, boolean translateColorCodes, boolean storeInHistory) {
        if(!message.endsWith(System.lineSeparator())) message += System.lineSeparator();
        message = message.replaceAll("%tc", this.textColor)
                .replaceAll("%hc", this.highlightColor);
        if(storeInHistory) this.allWroteLines.add(message);

        if(this.currentSetup != null) return;

        this.lineReader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        this.lineReader.getTerminal().writer().print(Ansi.ansi().eraseLine(Ansi.Erase.ALL) + "\r" + (translateColorCodes ? translateColorCodes(message) : message) + Ansi.ansi().reset());
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
        if(canLog(LogLevel.DEBUG)) this.cleanConsoleMode = false;
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
