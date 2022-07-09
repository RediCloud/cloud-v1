package net.suqatri.cloud.node.console.setup;

import com.google.common.primitives.Primitives;
import lombok.Getter;
import net.suqatri.cloud.commons.function.BiSupplier;
import net.suqatri.cloud.commons.reflection.ReflectionUtils;
import net.suqatri.cloud.node.console.NodeConsole;
import net.suqatri.cloud.node.console.setup.annotations.*;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class Setup<T extends Setup<?>> {

    private static final Map<Class<?>, SetupInputParser<?>> inputTransformers = new HashMap<>();

    public static <T> void registerTransformer(Class<T> clazz, SetupInputParser<T> supplier) {
        inputTransformers.put(clazz, supplier);
    }

    static {

        registerTransformer(int.class, (entry, input) -> Integer.parseInt(input));
        registerTransformer(double.class, (entry, input) -> Double.parseDouble(input));
        registerTransformer(long.class, (entry, input) -> Long.parseLong(input));
        registerTransformer(byte.class, ((entry, input) -> Byte.parseByte(input)));
        registerTransformer(short.class, ((entry, input) -> Short.parseShort(input)));
        registerTransformer(float.class, (entry, input) -> Float.parseFloat(input));
        registerTransformer(boolean.class, (entry, input) -> Boolean.parseBoolean(input));

        registerTransformer(String.class, (entry, input) -> input);
        registerTransformer(Integer.class, (entry, input) -> Integer.parseInt(input));
        registerTransformer(Double.class, (entry, input) -> Double.parseDouble(input));
        registerTransformer(Long.class, (entry, input) -> Long.parseLong(input));
        registerTransformer(Byte.class, ((entry, input) -> Byte.parseByte(input)));
        registerTransformer(Short.class, ((entry, input) -> Short.parseShort(input)));
        registerTransformer(Float.class, (entry, input) -> Float.parseFloat(input));
        registerTransformer(Boolean.class, (entry, input) -> Boolean.parseBoolean(input));
        registerTransformer(Enum.class, (entry, input) -> {
            if (entry.getRequiresEnum() == null) {
                throw new IllegalStateException("To use an Enum in Setup you need the @RequiresEnum annotation!");
            }
            RequiresEnum requiresEnum = entry.getRequiresEnum();
            Class value = requiresEnum.value();

            return Enum.valueOf(value, input.trim().toUpperCase());
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

    /**
     * All lines of the console
     */
    private final List<String> restoredLines;

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

    /**
     * If this setup is allowed to be cancelled
     */
    public abstract boolean isCancellable();

    /**
     * If a header should be printed
     */
    public abstract boolean shouldPrintHeader();

    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.CLEAR_SCREEN_AFTER;
    }

    public Setup(NodeConsole console) {

        this.console = console;

        this.cancelled = false;
        this.exitAfterAnswer = false;
        this.map = new HashMap<>();
        this.restoredLines = console.getAllWroteLines();
        this.current = 1;

        this.loadSetupParts();

        this.console.setCurrentSetup(this);

    }

    public void start(SetupListener<T> finishHandler) {
        this.setupListener = finishHandler;

        //Setting current setup
        this.setup = this.getEntry(1);
        this.printQuestion(this.setup.getValue());

        //While current id is in range of map-cache
        while (this.current < this.map.size() + 1) {
            //Reading input and executing Setup#next(String)
            String line = this.console.getInput();
            if (line != null) {
                executeInput(line);
            }
        }

        this.exit(true);
    }

    @SuppressWarnings("unchecked")
    private void exit(boolean success) {

        if (headerBehaviour() == SetupHeaderBehaviour.RESTORE_PREVIOUS_LINES) {
            this.console.clearScreen();
            for (String restoredLine : new ArrayList<>(this.restoredLines)) {
                this.console.print(restoredLine);
            }
        }
        //If already exited by another code line
        if (this.setupListener != null) {
            //Setup done and accepting consumer
            this.console.setCurrentSetup(null);
            if (success) {
                this.setupListener.accept((T) this, SetupControlState.FINISHED);
            } else {
                this.setupListener.accept((T) this, SetupControlState.CANCELLED);
            }
            this.setupListener = null;
        }

    }


    /**
     * Handles the current question with a given input
     * It checks if the answer should exit after or jump
     * to a other question and then set the current part higher
     * <p>
     * Checks for disallowed answers or only allowed answers
     *
     * @param input the input
     */
    public void executeInput(String input) {
        if (this.setup != null) {

            //No input provided
            if (input.trim().isEmpty()) {
                this.console.info("§cPlease do not enter §eempty §cinput!");
                return;
            }

            //Cancelling setup
            if (input.equalsIgnoreCase("cancel")) {
                if (!this.isCancellable()) {
                    this.console.info("§cYou cannot cancel the current setup§c!");
                    return;
                }
                this.console.info("§cThe current setup was §ecancelled§c!");
                this.cancelled = true;
                this.current += 10000;

                this.exit(false);
                return;
            }

            SetupEntry setupEntry = this.setup.getValue();

            //If answer is enum only
            if (!setupEntry.isEnumRequired(input)) {
                this.console.info("§cPossible answers: §e" + Arrays.toString(setup.getValue().getRequiresEnum().value().getEnumConstants()).replace("]", ")").replace("[", "("));
                return;
            }

            //If the current input is not allowed for this setup question because you provided a wrong type
            if (!setupEntry.isAllowed(input)) {

                String[] onlyAllowed = null;
                if (setupEntry.getAnswers() != null) {
                    onlyAllowed = setupEntry.getAnswers().only();
                }

                if (onlyAllowed == null || onlyAllowed.length == 0) {
                    this.console.info("§cCouldn't show you any possible answers because no possible answers were provided in the Setup!");
                } else {
                    this.console.info("§cPossible answers: §e" + Arrays.toString(onlyAllowed).replace("]", "").replace("[", ""));
                }
                this.console.info("§cRequired Type: §e" + this.setup.getKey().getType().getSimpleName());
                return;
            }

            //If the current input is forbidden to use
            if (setupEntry.isForbidden(input)) {
                this.console.info(!input.trim().isEmpty() ? ("§cThe answer '§e" + input + " §cmay not be used for this question!") : "§cThis §eanswer §cmay not be used for this question!");
                return;
            }

            ConditionChecker checker = setupEntry.getChecker();
            if (checker != null) {
                Class<? extends BiSupplier<String, Boolean>> value = checker.value();
                BiSupplier<String, Boolean> supplier = ReflectionUtils.createEmpty(value);
                if (supplier != null) {
                    if (supplier.supply(input)) {
                        this.console.info(checker.message().replace("%input%", input));
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
                    this.console.info("§cPlease try again");
                    return;
                }

                //Setting setup value
                this.setup.getKey().set(this, value);

            } catch (Exception ex) {
                this.console.info("§cThe §einput §cdidn't match any of the available §eAnswerTypes§c!");
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
                        field.getAnnotation(AnswerCompleter.class)
                );

                this.map.put(field, setupEntry);
            }
        }
    }

    private void printQuestion(SetupEntry entry) {
        if (this.shouldPrintHeader()) {
            this.printHeader(getClass().getSimpleName() + " at " + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()));
        }

        //Sending first question without any input

        StringBuilder sb = new StringBuilder(entry.getQuestion().question() + (entry.getQuestionTip() == null ? "" : " (Tip: " + entry.getQuestionTip().value() + ")"));
        if (entry.getAnswers() != null) {
            sb.append(" ").append((Arrays.toString(entry.getAnswers().only())).replace("[", "(").replace("]", ")"));
        }
        this.console.info(sb.toString());

        if (entry.getRequiresEnum() != null) {
            this.console.info("§7Possible Answers§8: §b" + Arrays.toString(entry.getRequiresEnum().value().getEnumConstants()).replace("]", "§8)").replace("[", "§8(§b").replace(",", "§8, §b"));
        }

        if (entry.getSuggestedAnswer() != null) {
            String value = entry.getSuggestedAnswer().value();
            this.console.setCommandInput(value);
        }

    }


    /**
     * Prints the header with its information
     * <p>
     * > If its cancellable
     * > Current Question ID
     * > Setup-Name
     */
    private void printHeader(String header) {
        this.console.clearScreen();

        this.console.info("§8");
        this.console.info(header);
        this.console.info("§8");
        if (this.isCancellable()) {
            this.console.info("§7» §7You can cancel this setup by typing \"§ecancel§7\"!");
        } else {
            this.console.info("§7» §7This setup is §cnot allowed §7to be cancelled!");
        }
        this.console.info("§7» §7Suggested answers can be §coverridden §7by typing your own!");
        this.console.info("§7» §7Suggested answers can be accepted by hitting §aenter§7!");
        this.console.info("§7» §7Hit §eTAB §7to see possible answers§7!");
        this.console.info("§7» §7Current Question §f: §b" + (this.current == 1 ? 1 : current) + "/" + (this.map.keySet().size() == 0 ? "Loading" : this.map.keySet().size() + ""));
        this.console.info("§8");
    }

}
