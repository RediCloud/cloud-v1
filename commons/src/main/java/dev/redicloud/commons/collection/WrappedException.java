package dev.redicloud.commons.collection;

public class WrappedException extends RuntimeException {

    public WrappedException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrappedException(Throwable cause) {
        super(cause);
    }

    public static RuntimeException rethrow(Throwable ex) {
        if (ex instanceof Error)
            throw (Error) ex;
        if (ex instanceof RuntimeException)
            throw (RuntimeException) ex;
        throw silent(ex);
    }

    public static WrappedException silent(Throwable cause) {
        return new SilentWrappedException(cause);
    }

    public static WrappedException silent(String message, Throwable cause) {
        return new SilentWrappedException(message, cause);
    }

    @Override
    public Throwable getCause() {
        return super.getCause();
    }

    public static class SilentWrappedException extends WrappedException {

        public SilentWrappedException(String message, Throwable cause) {
            super(message, cause);
        }

        public SilentWrappedException(Throwable cause) {
            super(cause);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

    }
}
