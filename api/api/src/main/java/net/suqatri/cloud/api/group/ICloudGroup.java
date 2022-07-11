package net.suqatri.cloud.api.group;

import net.suqatri.cloud.api.node.ICloudNode;
import net.suqatri.cloud.api.player.ICloudPlayer;
import net.suqatri.cloud.api.redis.bucket.IRBucketHolder;
import net.suqatri.cloud.api.service.ICloudService;
import net.suqatri.cloud.api.service.IServiceStartConfiguration;
import net.suqatri.cloud.api.service.ServiceState;
import net.suqatri.cloud.api.template.ICloudServiceTemplate;
import net.suqatri.cloud.commons.function.future.FutureAction;

import java.io.Serializable;
import java.util.Collection;

public interface ICloudGroup extends Serializable {

    String getName();

    String[] getProcessParameters();
    void setProcessParameters(String[] processParameters);

    String[] getJvmArguments();
    void setJvmArguments(String[] jvmArguments);

    String getJavaCommand();
    void setJavaCommand(String javaCommand);

    int getMinServices();
    void setMinServices(int minServices);

    int getMaxServices();
    void setMaxServices(int maxServices);

    FutureAction<Collection<IRBucketHolder<ICloudServiceTemplate>>> getTemplates();
    void setTemplates(Collection<ICloudServiceTemplate> templates);
    void addTemplate(ICloudServiceTemplate template);
    void removeTemplate(ICloudServiceTemplate template);
    boolean hasTemplate(ICloudServiceTemplate template);

    int getOnlineServiceCount();
    int getServicesCount(ServiceState serviceState);

    Collection<ICloudService> getOnlineServices();
    Collection<ICloudService> getOnlineServices(ServiceState serviceState);

    boolean isStatic();
    void setStatic(boolean staticGroup);

    FutureAction<Collection<IRBucketHolder<ICloudNode>>> getAssociatedNodes();
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

    Collection<ICloudPlayer> getPlayers();
    int getPlayersCount();

    IServiceStartConfiguration createServiceConfiguration();

}
