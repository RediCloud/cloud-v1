package dev.redicloud.api.impl.service.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import dev.redicloud.api.CloudAPI;
import dev.redicloud.api.service.ICloudService;
import dev.redicloud.api.service.ServiceEnvironment;
import dev.redicloud.api.service.configuration.IServiceStartConfiguration;
import dev.redicloud.commons.function.future.FutureAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class DefaultServiceStartConfiguration implements IServiceStartConfiguration {

    private ServiceEnvironment environment;
    private String name;
    private UUID uniqueId;
    private int id;
    private boolean fallback;
    private int maxMemory;
    private Collection<UUID> possibleNodeIds;
    private int startPriority;
    private boolean isStatic;
    private Collection<String> templateNames = new ArrayList<>();
    private String groupName;
    private List<String> processParameters;
    private List<String> jvmArguments;
    private boolean hasGroup;
    private int startPort = 0;
    private String serviceVersionName;
    private UUID nodeId;
    @JsonIgnore
    private FutureAction<ICloudService> startListener;
    private int percentToStartNewService;

    @Override
    public int getStartPort() {
        if(this.startPort == 0){
            switch (getEnvironment()){
                case BUNGEECORD:
                case VELOCITY:
                    return 25565;
                case MINECRAFT:
                case LIMBO:
                    return 49152;
            }
        }
        return this.startPort;
    }

    public static FutureAction<DefaultServiceStartConfiguration> fromInterface(IServiceStartConfiguration interfaceConfig) {
        FutureAction<DefaultServiceStartConfiguration> futureAction = new FutureAction<>();
        DefaultServiceStartConfiguration configuration = new DefaultServiceStartConfiguration();
        configuration.setEnvironment(interfaceConfig.getEnvironment());
        configuration.setName(interfaceConfig.getName());
        configuration.setUniqueId(interfaceConfig.getUniqueId());
        configuration.setId(interfaceConfig.getId());
        configuration.setMaxMemory(interfaceConfig.getMaxMemory());
        configuration.setPossibleNodeIds(interfaceConfig.getPossibleNodeIds());
        configuration.setStartPriority(interfaceConfig.getStartPriority());
        configuration.setStatic(interfaceConfig.isStatic());
        configuration.setTemplateNames(interfaceConfig.getTemplateNames());
        configuration.setProcessParameters(interfaceConfig.getProcessParameters());
        configuration.setJvmArguments(interfaceConfig.getJvmArguments());
        configuration.setTemplateNames(interfaceConfig.getTemplateNames());
        configuration.setServiceVersionName(interfaceConfig.getServiceVersionName());
        configuration.setPercentToStartNewService(interfaceConfig.getPercentToStartNewService());

        if (interfaceConfig.getGroupName() != null) {
            configuration.setHasGroup(true);
            configuration.setGroupName(interfaceConfig.getGroupName());
            CloudAPI.getInstance().getGroupManager().getGroupAsync(interfaceConfig.getGroupName())
                    .onFailure(futureAction)
                    .onSuccess(group -> {
                        configuration.getTemplateNames().addAll(group.getTemplateNames());
                        configuration.setFallback(group.isFallback());
                        configuration.setPercentToStartNewService(group.getPercentToStartNewService());
                        futureAction.complete(configuration);
                    });
        } else {
            configuration.setHasGroup(false);
            configuration.setFallback(false);
            futureAction.complete(configuration);
        }

        return futureAction;
    }

    @Override
    public void listenToStart() {
        if (this.startListener != null) return;
        this.startListener = new FutureAction<>();
    }

    @Override
    public boolean isGroupBased() {
        return this.hasGroup;
    }
}
