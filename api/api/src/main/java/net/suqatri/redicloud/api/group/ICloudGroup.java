package net.suqatri.redicloud.api.group;

import net.suqatri.redicloud.api.node.ICloudNode;
import net.suqatri.redicloud.api.player.ICloudPlayer;
import net.suqatri.redicloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.redicloud.api.redis.bucket.IRBucketObject;
import net.suqatri.redicloud.api.service.ICloudService;
import net.suqatri.redicloud.api.service.ServiceEnvironment;
import net.suqatri.redicloud.api.service.ServiceState;
import net.suqatri.redicloud.api.service.configuration.IServiceStartConfiguration;
import net.suqatri.redicloud.api.service.version.ICloudServiceVersion;
import net.suqatri.redicloud.api.template.ICloudServiceTemplate;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.util.Collection;
import java.util.UUID;

public interface ICloudGroup extends IRBucketObject {

    UUID getUniqueId();
    ServiceEnvironment getServiceEnvironment();
    String getName();

    String[] getProcessParameters();
    void setProcessParameters(String[] processParameters);

    String[] getJvmArguments();
    void setJvmArguments(String[] jvmArguments);

    int getMinServices();
    void setMinServices(int minServices);

    int getMaxServices();
    void setMaxServices(int maxServices);

    FutureAction<IRBucketHolder<ICloudServiceVersion>> getServiceVersion();
    void setServiceVersion(IRBucketHolder<ICloudServiceVersion> serviceVersion);
    String getServiceVersionName();

    FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> getTemplates();
    Collection<String> getTemplateNames();
    void setTemplates(Collection<ICloudServiceTemplate> templates);
    void addTemplate(ICloudServiceTemplate template);
    void removeTemplate(ICloudServiceTemplate template);
    boolean hasTemplate(ICloudServiceTemplate template);

    FutureAction<Integer> getOnlineServiceCount();
    FutureAction<Integer> getServicesCount(ServiceState serviceState);

    FutureAction<Collection<IRBucketHolder<ICloudService>>> getOnlineServices();
    FutureAction<Collection<IRBucketHolder<ICloudService>>> getOnlineServices(ServiceState serviceState);

    boolean isStatic();
    void setStatic(boolean staticGroup);

    FutureAction<Collection<IRBucketHolder<ICloudNode>>> getAssociatedNodes();
    Collection<UUID> getAssociatedNodeIds();
    void addAssociatedNode(ICloudNode node);
    void removeAssociatedNode(ICloudNode node);
    boolean hasAssociatedNode(ICloudNode node);
    void setAssociatedNodes(Collection<ICloudNode> nodes);

    int getStartPort();
    void setStartPort(int startPort);

    boolean isMaintenance();
    void setMaintenance(boolean maintenance);

    int getMaxMemory();
    void setMaxMemory(int maxMemory);

    int getStartPriority();
    void setStartPriority(int startPriority);

    FutureAction<Collection<IRBucketHolder<ICloudPlayer>>> getPlayers();
    FutureAction<Integer> getPlayersCount();

    IServiceStartConfiguration createServiceConfiguration();

    boolean isFallback();
    void setFallback(boolean fallback);

}
