package net.suqatri.cloud.api.packet;

public interface ICloudPacketResponse extends ICloudPacket{

    Exception getException();
    void setException(Exception exception);

}
