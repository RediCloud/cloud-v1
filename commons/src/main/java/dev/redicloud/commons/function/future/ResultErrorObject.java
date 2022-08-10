package dev.redicloud.commons.function.future;

import lombok.Data;

@Data
public class ResultErrorObject<T> {

    private final T result;
    private final Throwable error;

}
