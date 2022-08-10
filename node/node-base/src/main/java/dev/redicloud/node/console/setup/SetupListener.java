package dev.redicloud.node.console.setup;

import dev.redicloud.commons.function.ExceptionallyBiConsumer;

public interface SetupListener<T extends Setup<?>> extends ExceptionallyBiConsumer<T, SetupControlState> {
}
