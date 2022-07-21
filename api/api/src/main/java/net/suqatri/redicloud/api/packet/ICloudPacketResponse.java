package net.suqatri.redicloud.api.packet;

public interface ICloudPacketResponse extends ICloudPacket{

    String getException();
    String getErrorMessage();
    void setException(Exception exception);

}
