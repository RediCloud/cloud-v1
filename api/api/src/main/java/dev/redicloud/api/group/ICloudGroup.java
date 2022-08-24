package dev.redicloud.api.group;

import dev.redicloud.api.player.ICloudPlayer;
import dev.redicloud.api.redis.bucket.IRBucketObject;
import dev.redicloud.api.redis.bucket.fetch.IRBucketFetchAble;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.api.service.ServiceState;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.api.service.version.ICloudServiceVersion;
import dev.redicloud.api.template.ICloudServiceTemplate;
import dev.redicloud.api.node.ICloudNode;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudGroup extends IRBucketFetchAble {

    UUID getUniqueId();

    ServiceEnvironment getServiceEnvironment();

    String getName();

    int getMaxPlayers();
    void setMaxPlayers(int maxPlayers);

    int getPercentToStartNewService();
    void setPercentToStartNewService(int percentToStartNewService);

    String[] getProcessParameters();

    void setProcessParameters(String[] processParameters);

    String[] getJvmArguments();

    void setJvmArguments(String[] jvmArguments);

    int getMinServices();

    void setMinServices(int minServices);

    int getMaxServices();

    void setMaxServices(int maxServices);

    FutureAction<ICloudServiceVersion> getServiceVersion();

    void setServiceVersion(ICloudServiceVersion serviceVersion);

    String getServiceVersionName();

    FutureAction<Collection<ICloudServiceTemplate>> getTemplates();

    void setTemplates(Collection<ICloudServiceTemplate> templates);

    Collection<String> getTemplateNames();

    void addTemplate(ICloudServiceTemplate template);

    void removeTemplate(ICloudServiceTemplate template);

    boolean hasTemplate(ICloudServiceTemplate template);

    boolean hasTemplate(String templateName);

    FutureAction<Integer> getOnlineServiceCount();

    FutureAction<Integer> getServicesCount(ServiceState serviceState);

    FutureAction<Collection<ICloudService>> getConnectedServices();

    FutureAction<Collection<ICloudService>> getServices();

    FutureAction<Collection<ICloudService>> getServices(ServiceState serviceState);

    boolean isStatic();

    void setStatic(boolean staticGroup);

    FutureAction<Collection<ICloudNode>> getAssociatedNodes();

    void setAssociatedNodes(Collection<ICloudNode> nodes);

    Collection<UUID> getAssociatedNodeIds();

    void addAssociatedNode(ICloudNode node);

    void removeAssociatedNode(ICloudNode node);

    boolean hasAssociatedNode(ICloudNode node);

    int getStartPort();

    void setStartPort(int startPort);

    boolean isMaintenance();

    void setMaintenance(boolean maintenance);

    int getMaxMemory();

    void setMaxMemory(int maxMemory);

    int getStartPriority();

    void setStartPriority(int startPriority);

    FutureAction<Collection<ICloudPlayer>> getPlayers();

    FutureAction<Integer> getPlayersCount();

    IServiceStartConfiguration createServiceConfiguration();

    boolean isFallback();

    void setFallback(boolean fallback);

}
