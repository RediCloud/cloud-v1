package net.suqatri.redicloud.api.service.configuration;

import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IServiceStartConfiguration {

    ServiceEnvironment getEnvironment();

    UUID getNodeId();

    void setNodeId(UUID nodeId);

    String getName();

    UUID getUniqueId();
    void setUniqueId(UUID uniqueId);

    boolean isFallback();

    void setFallback(boolean fallback);

    String getServiceVersionName();

    void setServiceVersionName(String serviceVersionName);

    int getId();

    void setId(int id);

    int getMaxMemory();

    default int getStartPort() {
        switch (getEnvironment()){
            case BUNGEECORD:
            case VELOCITY:
                return 25565;
            case MINECRAFT:
            case LIMBO:
                return 49152;
        }
        return -1;
    }

    Collection<UUID> getPossibleNodeIds();

    int getStartPriority();

    boolean isStatic();

    Collection<String> getTemplateNames();

    String getGroupName();

    boolean isGroupBased();

    List<String> getProcessParameters();

    List<String> getJvmArguments();

    FutureAction<ICloudService> getStartListener();

    void listenToStart();

    int getPercentToStartNewService();
    void setPercentToStartNewService(int percentToStartNewService);

}
