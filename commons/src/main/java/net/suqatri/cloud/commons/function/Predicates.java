package net.suqatri.cloud.commons.function;

import net.suqatri.cloud.commons.function.future.FutureAction;

/**
 * This utility class provides some useful methods for predicates.
 */
public class Predicates {

    /**
     * Checks if the given object is null.
     * @param object The object to check.
     * @param message The message to be thrown if the object is null.
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
    }

    /**
     * Checks if the given object is null and throws an exception if the condition is true.
     * @param object The object to check.
     * @param message The message to be thrown if the object is null.
     * @param condition The condition to check.
     */
    public static void notNull(Object object, String message, boolean condition) {
        if (object == null && condition) {
            throw new NullPointerException(message);
        }
    }

    /**
     * Check if the give object is null.
     * If the object is null the exception will be provided to the given future action.
     * @param object The object to check.
     * @param message The message to be thrown if the object is null.
     * @param futureAction The future action to provide the exception
     */
    public static void notNull(Object object, String message, FutureAction<?> futureAction) {
        try {
            notNull(object, message);
        }catch (Exception e){
            futureAction.completeExceptionally(e);
            throw new NullPointerException(message);
        }
    }

    /**
     * Check if the give object is null and provided an exception if the condition is true to the given future action.
     * @param object The object to check.
     * @param message The message to be thrown if the object is null.
     * @param futureAction The future action to provide the exception
     * @param condition The condition to check.
     */
    public static void notNull(Object object, String message, FutureAction<?> futureAction, boolean condition) {
        try {
            notNull(object, message, condition);
        }catch (Exception e){
            futureAction.completeExceptionally(e);
        }
    }

    /**
     * Check if an illegal argument is provided.
     * It will throw an exception if the condition is true.
     * @param condition The condition to check.
     * @param message The message to be thrown if the condition is true.
     */
    public static void illegalArgument(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Check if an illegal argument is provided and provided an exception if the condition is true to the given future action.
     * @param condition The condition to check.
     * @param message The message to be thrown if the condition is true.
     * @param futureAction The future action to provide the exception
     */
    public static void illegalArgument(boolean condition, String message, FutureAction<?> futureAction) {
        try {
            illegalArgument(condition, message);
        }catch (Exception e){
            futureAction.completeExceptionally(e);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Check if an null or illegal argument is provided and provided an exception if the condition is true
     * @param object The object to check.
     * @param condition The condition to check.
     * @param message The message to be thrown if the condition is true.
     */
    public static void notNullAndIllegalArgument(Object object, boolean condition, String message) {
        notNull(object, message);
        illegalArgument(condition, message);
    }

    /**
     * Check if a null or illegal argument is provided and provided an exception if the condition is true to the given future action.
     * @param object The object to check.
     * @param condition The condition to check.
     * @param message The message to be thrown if the condition is true.
     * @param futureAction The future action to provide the exception
     */
    public static void notNullAndIllegalArgument(Object object, boolean condition, String message, FutureAction<?> futureAction) {
        try {
            notNull(object, message);
            illegalArgument(condition, message);
        }catch (Exception e){
            futureAction.completeExceptionally(e);
        }
    }

}
