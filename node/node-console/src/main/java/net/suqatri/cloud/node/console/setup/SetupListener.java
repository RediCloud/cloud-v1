package net.suqatri.cloud.node.console.setup;

import net.suqatri.cloud.commons.function.ExceptionallyBiConsumer;

public interface SetupListener<T extends Setup<?>> extends ExceptionallyBiConsumer<T, SetupControlState> {
}
