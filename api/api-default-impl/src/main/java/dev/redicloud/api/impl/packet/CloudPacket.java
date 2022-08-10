package dev.redicloud.api.impl.packet;

import dev.redicloud.api.impl.packet.response.CloudPacketResponse;
import dev.redicloud.api.impl.packet.response.SimpleCloudPacketResponse;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.network.INetworkComponentInfo;
import dev.redicloud.api.network.NetworkComponentType;
import dev.redicloud.api.packet.ICloudPacket;

public abstract class CloudPacket implements ICloudPacket {

    private final CloudPacketData packetData = new CloudPacketData();

    @Override
    public CloudPacketData getPacketData() {
        return this.packetData;
    }

    public void simplePacketResponseAsync() {
        SimpleCloudPacketResponse packet = new SimpleCloudPacketResponse();
        packet.getPacketData().setResponseTargetData(this.packetData);
        packet.getPacketData().addReceiver(this.packetData.getSender());
        packet.publishAsync();
    }

    public void simplePacketResponse() {
        SimpleCloudPacketResponse packet = new SimpleCloudPacketResponse();
        packet.getPacketData().setResponseTargetData(this.packetData);
        packet.getPacketData().addReceiver(this.packetData.getSender());
        packet.publish();
    }

    public void simplePacketResponseAsync(Exception exception) {
        SimpleCloudPacketResponse packet = new SimpleCloudPacketResponse();
        packet.setException(exception);
        packet.getPacketData().setResponseTargetData(this.packetData);
        packet.getPacketData().addReceiver(this.packetData.getSender());
        packet.publishAsync();
    }

    public void simplePacketResponse(Exception exception) {
        SimpleCloudPacketResponse packet = new SimpleCloudPacketResponse();
        packet.setException(exception);
        packet.getPacketData().setResponseTargetData(this.packetData);
        packet.getPacketData().addReceiver(this.packetData.getSender());
        packet.publish();
    }

    public <T extends CloudPacketResponse> void packetResponse(T response) {
        response.getPacketData().addReceiver(this.packetData.getSender());
        response.publish();
    }

    public <T extends CloudPacketResponse> void packetResponseAsync(T response) {
        response.getPacketData().addReceiver(this.packetData.getSender());
        response.publishAsync();
    }

    @Override
    public void publish() {

        if (getPacketData().getReceivers().isEmpty() && getPacketData().getResponseTargetData() == null)
            throw new IllegalStateException("No receivers specified!");

        if (getPacketData().getReceivers().contains(CloudAPI.getInstance().getNetworkComponentInfo())
                && getPacketData().isAllowSenderAsReceiver()) return;

        if (getPacketData().getResponseTargetData() != null) {
            if (!getPacketData().getReceivers().contains(getPacketData().getResponseTargetData().getSender())) {
                getPacketData().getReceivers().add(getPacketData().getResponseTargetData().getSender());
            }
        }

        if (getPacketData().getReceivers().isEmpty()) return;
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        CloudAPI.getInstance().getPacketManager().publish(this);
    }

    @Override
    public void publishAsync() {
        if (getPacketData().getReceivers().isEmpty() && getPacketData().getResponseTargetData() == null)
            throw new IllegalStateException("No receivers specified!");

        if (getPacketData().getReceivers().contains(CloudAPI.getInstance().getNetworkComponentInfo())
                && getPacketData().isAllowSenderAsReceiver()) return;

        if (getPacketData().getResponseTargetData() != null) {
            if (!getPacketData().getReceivers().contains(getPacketData().getResponseTargetData().getSender())) {
                getPacketData().getReceivers().add(getPacketData().getResponseTargetData().getSender());
            }
        }

        if (getPacketData().getReceivers().isEmpty()) return;
        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        CloudAPI.getInstance().getPacketManager().publishAsync(this);
    }

    @Override
    public void publishAll() {

        for (INetworkComponentInfo receiver : CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfo()) {
            if (receiver.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                    && !this.getPacketData().isAllowSenderAsReceiver()) continue;
            getPacketData().addReceiver(receiver);
        }

        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        if (getPacketData().getReceivers().isEmpty()) return;
        CloudAPI.getInstance().getPacketManager().publish(this);
    }

    @Override
    public void publishAllAsync(NetworkComponentType type) {

        if (getPacketData().getResponseTargetData() != null) {
            if (!getPacketData().getReceivers().contains(getPacketData().getResponseTargetData().getSender())) {
                getPacketData().getReceivers().add(getPacketData().getResponseTargetData().getSender());
            }
        }

        CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfoAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get all component info of type " + type + ". Unable to send " + this.getClass().getName(), e))
                .onSuccess(componentInfos -> {

                    for (INetworkComponentInfo componentInfo : componentInfos) {
                        if (componentInfo.getType() != type) continue;
                        if (componentInfo.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                                && !this.getPacketData().isAllowSenderAsReceiver()) continue;
                        getPacketData().addReceiver(componentInfo);
                    }

                    getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
                    if (getPacketData().getReceivers().isEmpty()) return;
                    CloudAPI.getInstance().getPacketManager().publishAsync(this);
                });
    }

    @Override
    public void publishAllAsync() {
        CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfoAsync()
                .onFailure(e -> CloudAPI.getInstance().getConsole().error("Failed to get all component info. Unable to send " + this.getClass().getName(), e))
                .onSuccess(componentInfos -> {

                    for (INetworkComponentInfo componentInfo : componentInfos) {
                        if (componentInfo.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                                && !this.getPacketData().isAllowSenderAsReceiver()) continue;
                        getPacketData().addReceiver(componentInfo);
                    }

                    getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
                    if (getPacketData().getReceivers().isEmpty()) return;
                    CloudAPI.getInstance().getPacketManager().publishAsync(this);
                });
    }

    @Override
    public void publishAll(NetworkComponentType type) {
        if (getPacketData().getResponseTargetData() != null) {
            if (!getPacketData().getReceivers().contains(getPacketData().getResponseTargetData().getSender())) {
                getPacketData().getReceivers().add(getPacketData().getResponseTargetData().getSender());
            }
        }

        for (INetworkComponentInfo componentInfo : CloudAPI.getInstance().getNetworkComponentManager().getAllComponentInfo()) {
            if (componentInfo.getType() != type) continue;
            if (componentInfo.equals(CloudAPI.getInstance().getNetworkComponentInfo())
                    && !this.getPacketData().isAllowSenderAsReceiver()) continue;
            getPacketData().addReceiver(componentInfo);
        }

        getPacketData().setSender(CloudAPI.getInstance().getNetworkComponentInfo());
        if (getPacketData().getReceivers().isEmpty()) return;
        CloudAPI.getInstance().getPacketManager().publishAsync(this);
    }
}
