package net.suqatri.cloud.api.node.service.screen;

import net.suqatri.cloud.api.console.IConsoleLine;

import java.io.Serializable;

public interface IScreenLine extends Serializable {

    long getTime();
    String getLine();
    void print();
    IConsoleLine createConsoleLine();

}
