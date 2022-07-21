package net.suqatri.redicloud.node.console.setup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.suqatri.redicloud.node.console.setup.annotations.*;

import java.lang.reflect.Field;
import java.util.Arrays;

@AllArgsConstructor @Getter
public class SetupEntry {

    /**
     * The field where the annotation was
     */
    private final Field field;

    /**
     * The question of this entry
     */
    private final Question question;

    /**
     * The required enum (optional)
     */
    private final RequiresEnum requiresEnum;

    /**
     * The answers (optional)
     */
    private final Answers answers;

    /**
     * If setup should exit after input (optional)
     */
    private final ExitAfterInput exitAfterInput;

    /**
     * The suggested answer (optional)
     */
    private final SuggestedAnswer suggestedAnswer;

    /**
     * A tip that is going to be after the question
     */
    private final QuestionTip questionTip;

    /**
     * To check conditions for setup
     */
    private final ConditionChecker checker;

    /**
     * Completer for console
     */
    private final AnswerCompleter completer;

    /**
     * Checks if a provided answer causes a {@link Setup} to exit
     *
     * @param answer the provided answer
     * @return boolean if exit
     */
    public boolean isExitAfterInput(String answer) {
        if (exitAfterInput == null) {
            return false;
        }
        return Arrays.stream(exitAfterInput.value()).anyMatch(forbiddenAnswer -> forbiddenAnswer.trim().isEmpty() || forbiddenAnswer.equalsIgnoreCase(answer.trim()));
    }

    /**
     * Checks if a provided answer is forbidden to enter
     *
     * @param answer the provided answer
     * @return boolean if exit
     */
    public boolean isForbidden(String answer) {
        if (answers == null || answers.forbidden().length == 0) {
            return false;
        }
        return Arrays.stream(answers.forbidden()).anyMatch(forbiddenAnswer -> forbiddenAnswer.trim().isEmpty() || forbiddenAnswer.equalsIgnoreCase(answer));
    }


    /**
     * Checks if an answer is allowed (type - based)
     *
     * @param answer the answer you provided
     * @return allowed
     */
    public boolean isAllowed(String answer) {
        if (answers == null || answers.only().length == 0) {
            return true;
        }
        return Arrays.stream(answers.only()).anyMatch(onlyAnswer -> onlyAnswer.equalsIgnoreCase(answer));
    }

    /**
     * Checks if a setup passes the enum only
     *
     * @param answer the answer
     * @return if allowed passing
     */
    public boolean isEnumRequired(String answer) {
        if (requiresEnum == null) {
            return true;
        } else {
            Class<? extends Enum<?>> aClass = requiresEnum.value();
            for (Enum<?> enumConstant : aClass.getEnumConstants()) {
                if (enumConstant.name().equalsIgnoreCase(answer.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

}
