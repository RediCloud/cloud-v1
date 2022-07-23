package net.suqatri.redicloud.api.node.service.screen;

import net.suqatri.redicloud.api.console.IConsoleLine;

import java.io.Serializable;

public interface IScreenLine extends Serializable {

    long getTime();

    String getLine();

    void print();

    IConsoleLine createConsoleLine();

}
