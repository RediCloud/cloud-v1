package net.suqatri.redicloud.node.console.setup;

import com.google.common.primitives.Primitives;
import lombok.Getter;
import net.suqatri.redicloud.api.console.IConsoleInput;
import net.suqatri.redicloud.api.console.IConsoleLine;
import net.suqatri.redicloud.api.console.IConsoleLineEntry;
import net.suqatri.redicloud.api.node.service.screen.IServiceScreen;
import net.suqatri.redicloud.commons.function.BiSupplier;
import net.suqatri.redicloud.commons.reflection.ReflectionUtils;
import net.suqatri.redicloud.node.NodeLauncher;
import net.suqatri.redicloud.node.console.NodeConsole;
import net.suqatri.redicloud.node.console.setup.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public abstract class Setup<T extends Setup<?>> {

    private static final Map<Class<?>, SetupInputParser<?>> inputTransformers = new HashMap<>();

    static {

        registerTransformer(int.class, (entry, input) -> Integer.parseInt(input.replaceAll(" ", "")));
        registerTransformer(double.class, (entry, input) -> Double.parseDouble(input.replaceAll(" ", "")));
        registerTransformer(long.class, (entry, input) -> Long.parseLong(input.replaceAll(" ", "")));
        registerTransformer(byte.class, ((entry, input) -> Byte.parseByte(input.replaceAll(" ", ""))));
        registerTransformer(short.class, ((entry, input) -> Short.parseShort(input.replaceAll(" ", ""))));
        registerTransformer(float.class, (entry, input) -> Float.parseFloat(input.replaceAll(" ", "")));
        registerTransformer(boolean.class, (entry, input) -> Boolean.parseBoolean(input.replaceAll(" ", "")));

        registerTransformer(String.class, (entry, input) -> input);
        registerTransformer(Integer.class, (entry, input) -> Integer.parseInt(input.replaceAll(" ", "")));
        registerTransformer(Double.class, (entry, input) -> Double.parseDouble(input.replaceAll(" ", "")));
        registerTransformer(Long.class, (entry, input) -> Long.parseLong(input.replaceAll(" ", "")));
        registerTransformer(Byte.class, ((entry, input) -> Byte.parseByte(input.replaceAll(" ", ""))));
        registerTransformer(Short.class, ((entry, input) -> Short.parseShort(input.replaceAll(" ", ""))));
        registerTransformer(Float.class, (entry, input) -> Float.parseFloat(input.replaceAll(" ", "")));
        registerTransformer(Boolean.class, (entry, input) -> Boolean.parseBoolean(input.replaceAll(" ", "")));
        registerTransformer(Enum.class, (entry, input) -> {
            if (entry.getRequiresEnum() == null) {
                throw new IllegalStateException("To use an Enum in Setup you need the @RequiresEnum annotation!");
            }
            RequiresEnum requiresEnum = entry.getRequiresEnum();
            Class value = requiresEnum.value();

            return Enum.valueOf(value, input.replaceAll(" ", "").trim().toUpperCase());
        });
    }

    /**
     * The setup parts
     */
    private final Map<Field, SetupEntry> map;
    /**
     * The console to display questions
     */
    private final NodeConsole console;
    private final List<IServiceScreen> pausedScreens;
    /**
     * All lines of the console
     */
    private List<IConsoleLineEntry> restoredLines;

    /**
     * The current setup part
     */
    private int current;

    /**
     * If the setup is cancelled
     */
    @Getter
    private boolean cancelled;

    /**
     * If the setup was exited after one answer
     */
    @Getter
    private boolean exitAfterAnswer;

    /**
     * The current setup fields and their questions
     */
    @Getter
    private Map.Entry<Field, SetupEntry> setup;

    /**
     * The consumer when its finished
     */
    private SetupListener<T> setupListener;
    private Consumer<String> inputConsumer;

    public Setup(NodeConsole console) {

        this.console = console;

        this.cancelled = false;
        this.pausedScreens = new ArrayList<>();
        this.exitAfterAnswer = false;
        this.map = new HashMap<>();
        this.restoredLines = console.getLineEntries();
        this.restoredLines = new ArrayList<>(this.restoredLines.size() < 80 ? this.restoredLines : this.restoredLines.subList(this.restoredLines.size() - 80, this.restoredLines.size()));
        this.current = 1;

        this.loadSetupParts();

        this.console.setCurrentSetup(this);
        for (IServiceScreen activeScreen : NodeLauncher.getInstance().getScreenManager().getActiveScreens()) {
            NodeLauncher.getInstance().getScreenManager().leave(activeScreen);
            this.pausedScreens.add(activeScreen);
        }
    }

    public static <T> void registerTransformer(Class<T> clazz, SetupInputParser<T> supplier) {
        inputTransformers.put(clazz, supplier);
    }

    /**
     * If this setup is allowed to be cancelled
     */
    public abstract boolean isCancellable();

    /**
     * If a header should be printed
     */
    public abstract boolean shouldPrintHeader();

    public String getPrefix() {
        return "SETUP";
    }

    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.NOTHING;
    }

    public void start(SetupListener<T> finishHandler) {
        this.setupListener = finishHandler;

        //Setting current setup
        this.setup = this.getEntry(1);
        this.printQuestion(this.setup.getValue());

        this.inputConsumer = input -> {
            if (this.current < this.map.size() + 1) {
                executeInput(input);
            } else {
                this.exit(true);
            }
        };
        this.console.addInputHandler(this.inputConsumer);
    }

    @SuppressWarnings("unchecked")
    private void exit(boolean success) {

        if (this.inputConsumer != null) {
            this.console.removeInputHandler(this.inputConsumer);
        }

        switch (headerBehaviour()){
            case RESTORE_PREVIOUS_LINES:
                this.console.clearScreen();
                for (IConsoleLineEntry restoredLine : this.restoredLines) {
                    if (restoredLine instanceof IConsoleLine) {
                        ((IConsoleLine) restoredLine).setStored(false);
                        ((IConsoleLine) restoredLine).println();
                    } else if (restoredLine instanceof IConsoleInput) {
                        ((IConsoleInput) restoredLine).logAsFake();
                    }
                }
                break;
            case CLEAR_SCREEN_AFTER:
                this.console.clearScreen();
                break;
        }

        //Setup done and accepting consumer
        this.console.setCurrentSetup(null);

        //If already exited by another code line
        if (this.setupListener != null) {
            if (success) {
                this.setupListener.accept((T) this, SetupControlState.FINISHED);
            } else {
                this.setupListener.accept((T) this, SetupControlState.CANCELLED);
            }
            this.setupListener = null;
        }

        for (IServiceScreen pausedScreen : this.pausedScreens) {
            NodeLauncher.getInstance().getScreenManager().join(pausedScreen);
        }

    }


    /**
     * Handles the current question with a given input
     * It checks if the answer should exit after or jump
     * to an other question and then set the current part higher
     * <p>
     * Checks for disallowed answers or only allowed answers
     *
     * @param input the input
     */
    public void executeInput(String input) {
        if (this.setup != null) {

            //No input provided
            if (input.trim().isEmpty() && this.setup.getValue().getAcceptEmptyInput() == null) {
                this.console.printForce(getPrefix(), "§cPlease do not enter empty input!");
                return;
            }

            //Cancelling setup
            if (input.equalsIgnoreCase("cancel")) {
                if (!this.isCancellable()) {
                    this.console.printForce(getPrefix(), "§cYou cannot cancel the current setup!");
                    return;
                }
                this.console.printForce(getPrefix(), "§cThe current setup was cancelled!");
                this.cancelled = true;
                this.current += 10000;

                this.exit(false);
                return;
            }

            SetupEntry setupEntry = this.setup.getValue();

            //If answer is enum only
            if (!setupEntry.isEnumRequired(input)) {
                this.console.printForce(getPrefix(), "§cPossible answers: §e" + Arrays.toString(setup.getValue().getRequiresEnum().value().getEnumConstants()).replace("]", ")").replace("[", "("));
                return;
            }

            //If the current input is not allowed for this setup question because you provided a wrong type
            if (!setupEntry.isAllowed(input)) {

                String[] onlyAllowed = null;
                if (setupEntry.getAnswers() != null) {
                    onlyAllowed = setupEntry.getAnswers().only();
                }

                if (onlyAllowed == null || onlyAllowed.length == 0) {
                    this.console.printForce(getPrefix(), "§cCouldn't show you any possible answers because no possible answers were provided in the Setup!");
                } else {
                    this.console.printForce(getPrefix(), "§cPossible answers: §e" + Arrays.toString(onlyAllowed).replace("]", "").replace("[", ""));
                }
                this.console.printForce(getPrefix(), "§cRequired Type: §e" + this.setup.getKey().getType().getSimpleName());
                return;
            }

            //If the current input is forbidden to use
            if (setupEntry.isForbidden(input)) {
                this.console.printForce(getPrefix(), !input.trim().isEmpty() ? ("§cThe answer '§e" + input + " §cmay not be used for this question!") : "§cThis §eanswer §cmay not be used for this question!");
                return;
            }

            ConditionChecker checker = setupEntry.getChecker();
            if (checker != null) {
                Class<? extends BiSupplier<String, Boolean>> value = checker.value();
                BiSupplier<String, Boolean> supplier = ReflectionUtils.createEmpty(value);
                if (supplier != null) {
                    if (supplier.supply(input)) {
                        this.console.printForce(getPrefix(), checker.message().replace("%input%", input));
                        return;
                    }
                }
            }

            //Accessing the setup field
            this.setup.getKey().setAccessible(true);
            try {
                Object value;
                Field field = this.setup.getKey();
                Class<?> type = Primitives.wrap(field.getType());

                if (Enum.class.isAssignableFrom(type)) {
                    type = Enum.class;
                }

                SetupInputParser<?> transformer = inputTransformers.get(type);
                value = transformer.parse(setupEntry, input);

                if (value == null) {
                    this.console.printForce(getPrefix(), "§cPlease try again");
                    return;
                }

                //Setting setup value
                this.setup.getKey().set(this, value);

            } catch (Exception ex) {
                this.console.printForce(getPrefix(), "§cThe §einput §cdidn't match any of the available §eAnswerTypes§c!");
                return;
            }

            //If the setup should exit after this answer
            if (setupEntry.isExitAfterInput(input)) {
                this.current += 10000;
                this.exitAfterAnswer = true;
                return;
            }
        }

        //Going to next question going +1
        this.current++;
        this.setup = this.getEntry(this.current);

        //Could be last question and setup is not found
        if (this.setup != null) {
            //Sending question again and waiting for input
            this.printQuestion(setup.getValue());
        } else {
            this.exit(true);
        }
    }

    /**
     * Get an entry from the map cache
     *
     * @param id the question id
     * @return entry
     */
    private Map.Entry<Field, SetupEntry> getEntry(int id) {
        Map.Entry<Field, SetupEntry> entry = null;
        for (Map.Entry<Field, SetupEntry> currentEntry : this.map.entrySet()) {
            if (currentEntry.getValue().getQuestion().id() == id) {
                entry = currentEntry;
            }
        }
        return entry;
    }

    /**
     * Loads all setup fields that are
     * annotated with {@link Question}
     */
    private void loadSetupParts() {
        //Caching the setup fields and parts
        for (Field field : getClass().getDeclaredFields()) {
            if (field.getAnnotation(Question.class) != null) {

                SetupEntry setupEntry = new SetupEntry(
                        field,
                        field.getAnnotation(Question.class),
                        field.getAnnotation(RequiresEnum.class),
                        field.getAnnotation(Answers.class),
                        field.getAnnotation(ExitAfterInput.class),
                        field.getAnnotation(SuggestedAnswer.class),
                        field.getAnnotation(QuestionTip.class),
                        field.getAnnotation(ConditionChecker.class),
                        field.getAnnotation(AnswerCompleter.class),
                        field.getAnnotation(AcceptEmptyInput.class)
                );

                this.map.put(field, setupEntry);
            }
        }
    }

    private void printQuestion(SetupEntry entry) {
        if (this.shouldPrintHeader()) {
            this.printHeader("");
        }

        //Sending first question without any input

        StringBuilder sb = new StringBuilder(entry.getQuestion().question() + (entry.getQuestionTip() == null ? "" : " (Tip: " + entry.getQuestionTip().value() + ")"));
        if (entry.getAnswers() != null) {
            sb.append(" ").append((Arrays.toString(entry.getAnswers().only())).replace("[", "(").replace("]", ")"));
        }
        this.console.printForce(getPrefix(), sb.toString());

        if (entry.getRequiresEnum() != null) {
            this.console.printForce(getPrefix(), this.console.getTextColor() + "Possible Answers: " + this.console.getHighlightColor() + Arrays.toString(entry.getRequiresEnum().value().getEnumConstants()).replace("]", "§8)").replace("[", "§8(" + this.console.getHighlightColor()).replace(",", "§7, " + this.console.getHighlightColor()));
        }

        if (entry.getSuggestedAnswer() != null) {
            String value = entry.getSuggestedAnswer().value();
            this.console.setCommandInput(value);
        }

    }


    /**
     * Prints the header with its information
     *
     * > If its cancellable
     * > Current Question ID
     * > Setup-Name
     */
    private void printHeader(String header) {
        this.console.clearScreen();

        this.console.printForce(getPrefix(), "§8");
        if (this.isCancellable()) {
            this.console.printForce(getPrefix(), "     " + this.console.getTextColor() + "You can cancel this setup by typing §7\"" + this.console.getHighlightColor() + "cancel§7\"" + this.console.getTextColor() + "!");
        }
        this.console.printForce(getPrefix(), "     " + this.console.getTextColor() + "Use " + this.console.getHighlightColor() + "TAB" + this.console.getTextColor() + " to see possible answers!");
        this.console.printForce(getPrefix(), "     " + this.console.getTextColor() + "Current Question: " + this.console.getHighlightColor() + (this.current == 1 ? 1 : current) + "/" + (this.map.keySet().size() == 0 ? "Loading" : this.map.keySet().size() + ""));
        this.console.printForce(getPrefix(), "§8");
    }

}
