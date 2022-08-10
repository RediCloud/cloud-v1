package dev.redicloud.api.packet;

public interface ICloudPacketResponse extends ICloudPacket {

    String getException();

    void setException(Exception exception);

    String getErrorMessage();

}
