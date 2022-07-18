package net.suqatri.cloud.api.node.service.screen;

import java.io.Serializable;

public interface IScreenLine extends Serializable {

    long getTime();
    String getLine();
    void print();

}
