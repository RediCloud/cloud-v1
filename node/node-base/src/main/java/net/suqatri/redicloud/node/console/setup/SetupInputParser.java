package net.suqatri.redicloud.node.console.setup;

public interface SetupInputParser<T> {

    T parse(SetupEntry entry, String input);

}
