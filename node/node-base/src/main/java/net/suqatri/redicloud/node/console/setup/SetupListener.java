package net.suqatri.redicloud.node.console.setup;

import net.suqatri.redicloud.commons.function.ExceptionallyBiConsumer;

public interface SetupListener<T extends Setup<?>> extends ExceptionallyBiConsumer<T, SetupControlState> {
}
